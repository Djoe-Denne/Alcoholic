package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.ingredient.SolidInputView;
import com.djden.alcoholic.api.liquid.LiquidBatchView;
import com.djden.alcoholic.api.process.ItemOutput;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessHandler;
import com.djden.alcoholic.api.process.ProcessRequest;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.domain.liquid.BatchProvenance;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.LiquidDefinition;
import com.djden.alcoholic.domain.liquid.PropertyBag;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class MashProcessor implements ProcessHandler<MashConfig> {
    private final Supplier<BeverageCatalog> catalog;

    public MashProcessor(Supplier<BeverageCatalog> catalog) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
    }

    @Override
    public ProcessResult apply(ProcessRequest request, MashConfig config, ProcessContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(context, "context");
        if (!config.executable()) {
            return ProcessResult.rejected("mash config is missing output liquid or volume");
        }
        List<SolidInputView> solids = request.solidsOn("grist", "solid", "grain", "input");
        int available = solids.stream().mapToInt(SolidInputView::count).sum();
        if (available < config.inputAmount()) {
            return ProcessResult.rejected("insufficient solid input for mash");
        }
        LiquidBatchView liquidView = request.liquidOn("water", "liquid").orElse(null);
        if (!(liquidView instanceof LiquidBatch liquid)) {
            return ProcessResult.rejected("mash requires a liquid input");
        }
        if (config.inputLiquid().isPresent()
                && liquid.baseLiquid().filter(id -> id.equals(config.inputLiquid().orElseThrow())).isEmpty()) {
            return ProcessResult.rejected("liquid is not accepted by this mash definition");
        }
        if (liquid.volume() + 1e-9 < config.inputLiquidVolume()) {
            return ProcessResult.rejected("insufficient liquid volume for mash");
        }
        int units = Math.min(
                available / config.inputAmount(),
                (int) Math.floor(liquid.volume() / config.inputLiquidVolume())
        );
        units = Math.min(units, context.executorModifiers().maxBatchUnits());
        if (units < 1) {
            return ProcessResult.rejected("insufficient mash inputs");
        }
        double extraction = config.temperature().extractionYield(context.temperatureCelsius());
        double yield = extraction * context.executorModifiers().yieldModifier();
        PropertyBag agricultural = AgriculturalTransfer.combine(solids);
        PropertyBag defaults = catalog.get()
                .liquid(config.outputLiquid().orElseThrow())
                .map(LiquidDefinition::defaults)
                .map(PropertyBag::new)
                .orElse(PropertyBag.empty());
        PropertyBag combined = defaults;
        for (ResourceId id : agricultural.ids()) {
            combined = combined.with(id, agricultural.get(id).orElseThrow());
        }
        double fermentable = agricultural.get(config.sugarProperty())
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(Number::doubleValue)
                .orElse(0.80);
        combined = combined.with(config.sugarProperty(), fermentable * yield);
        double color = agricultural.get(config.colorProperty())
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(Number::doubleValue)
                .orElse(defaults.get(config.colorProperty())
                        .filter(Number.class::isInstance)
                        .map(Number.class::cast)
                        .map(Number::doubleValue)
                        .orElse(0.12));
        combined = combined.with(config.colorProperty(), color);
        combined = combined.with(config.temperatureProperty(), context.temperatureCelsius());
        int batchUnits = units;
        double volume = config.outputVolume() * batchUnits;
        LiquidBatch wort = LiquidBatch.of(
                config.outputLiquid().orElseThrow(),
                volume,
                combined,
                BatchProvenance.empty()
        );
        List<ItemOutput> items = config.byproduct()
                .map(item -> List.of(new ItemOutput(item.item(), item.amount() * batchUnits, item.properties())))
                .orElse(List.of());
        return ProcessResult.success(List.of(wort), items);
    }
}
