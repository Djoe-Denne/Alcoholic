package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.ingredient.SolidInputView;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessHandler;
import com.djden.alcoholic.api.process.ProcessRequest;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.domain.liquid.BatchProvenance;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.LiquidDefinition;
import com.djden.alcoholic.domain.liquid.PropertyBag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class PressProcessor implements ProcessHandler<PressConfig> {
    private final Supplier<BeverageCatalog> catalog;

    public PressProcessor(Supplier<BeverageCatalog> catalog) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
    }

    @Override
    public ProcessResult apply(ProcessRequest request, PressConfig config, ProcessContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(config, "config");
        if (!config.executable()) {
            return ProcessResult.rejected("press config is missing output liquid or volume");
        }
        List<SolidInputView> solids = flatten(request.solids());
        int available = solids.stream().mapToInt(SolidInputView::count).sum();
        if (available < config.inputAmount()) {
            return ProcessResult.rejected("insufficient solid input for press");
        }
        int units = Math.min(
                available / config.inputAmount(),
                context.executorModifiers().maxBatchUnits()
        );
        if (units < 1) {
            return ProcessResult.rejected("insufficient solid input for press");
        }
        PropertyBag agricultural = AgriculturalTransfer.combine(solids);
        PropertyBag defaults = catalog.get()
                .liquid(config.outputLiquid().orElseThrow())
                .map(LiquidDefinition::defaults)
                .map(PropertyBag::new)
                .orElse(PropertyBag.empty());
        PropertyBag combined = defaults;
        for (var id : agricultural.ids()) {
            combined = combined.with(id, agricultural.get(id).orElseThrow());
        }
        BatchProvenance provenance = BatchProvenance.empty();
        Object variety = combined.get(ResourceId.parse("alcoholic:variety")).orElse(null);
        if (variety instanceof String text) {
            try {
                provenance = BatchProvenance.ofOrigin(ResourceId.parse(text), 1.0);
            } catch (RuntimeException ignored) {
                // Variety is an opaque label, not a namespaced origin.
            }
        }
        double yield = config.yield() * context.executorModifiers().yieldModifier();
        LiquidBatch batch = LiquidBatch.of(
                config.outputLiquid().orElseThrow(),
                config.outputVolume() * yield * units,
                combined,
                provenance
        );
        return ProcessResult.success(
                List.of(batch),
                config.byproduct().stream()
                        .map(item -> new com.djden.alcoholic.api.process.ItemOutput(
                                item.item(),
                                item.amount() * units
                        ))
                        .toList()
        );
    }

    static List<SolidInputView> flatten(
            java.util.Map<String, List<SolidInputView>> solids
    ) {
        List<SolidInputView> all = new ArrayList<>();
        solids.values().forEach(all::addAll);
        return all;
    }
}
