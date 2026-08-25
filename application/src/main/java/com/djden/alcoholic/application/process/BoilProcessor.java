package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.ingredient.SolidInputView;
import com.djden.alcoholic.api.liquid.LiquidBatchView;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessHandler;
import com.djden.alcoholic.api.process.ProcessRequest;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.process.HopProfile;

import java.util.List;
import java.util.Objects;

public final class BoilProcessor implements ProcessHandler<BoilConfig> {
    @Override
    public ProcessResult apply(ProcessRequest request, BoilConfig config, ProcessContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(context, "context");
        if (!config.executable()) {
            return ProcessResult.rejected("boil config is missing input or output liquid");
        }
        LiquidBatchView view = request.liquidOn("wort", "input", "liquid").orElse(null);
        if (!(view instanceof LiquidBatch batch)) {
            return ProcessResult.rejected("boil requires a liquid batch");
        }
        if (config.inputLiquid().isPresent()
                && batch.baseLiquid().filter(id -> id.equals(config.inputLiquid().orElseThrow())).isEmpty()) {
            return ProcessResult.rejected("liquid is not accepted by this boil definition");
        }
        List<SolidInputView> solids = request.solidsOn("hops", "addition");
        int hopCount = solids.stream().mapToInt(SolidInputView::count).sum();
        if (config.additionSelector().isPresent() && hopCount < config.additionAmount()) {
            return ProcessResult.rejected("insufficient ingredient addition for boil");
        }
        if (config.temperature().rateFactor(context.temperatureCelsius()) <= 0.0) {
            return ProcessResult.rejected("temperature is outside the boil operating range");
        }
        double utilization = config.temperature().extractionYield(context.temperatureCelsius());
        HopProfile profile = config.hopProfile();
        double fromLotsBitterness = 0.0;
        double fromLotsAroma = 0.0;
        for (SolidInputView solid : solids) {
            fromLotsBitterness += solid.property(config.bitternessProperty())
                    .filter(Number.class::isInstance)
                    .map(Number.class::cast)
                    .map(Number::doubleValue)
                    .orElse(0.0) * solid.count();
            fromLotsAroma += solid.property(config.aromaProperty())
                    .filter(Number.class::isInstance)
                    .map(Number.class::cast)
                    .map(Number::doubleValue)
                    .orElse(0.0) * solid.count();
        }
        double units = Math.max(1, hopCount);
        double bitterness = fromLotsBitterness > 0.0
                ? fromLotsBitterness / units
                : profile.bitternessPotential() * hopCount;
        double aroma = fromLotsAroma > 0.0
                ? fromLotsAroma / units
                : profile.aromaPotential() * hopCount;
        bitterness = bitterness * utilization;
        aroma = aroma * utilization;
        double existingBitterness = batch.number(config.bitternessProperty(), 0.0);
        double existingAroma = batch.number(config.aromaProperty(), 0.0);
        LiquidBatch hopped = batch
                .withBaseLiquid(config.outputLiquid().orElseThrow())
                .withProperty(config.bitternessProperty(), existingBitterness + bitterness)
                .withProperty(config.aromaProperty(), existingAroma + aroma)
                .withProperty(ResourceId.parse("alcoholic:temperature"), context.temperatureCelsius());
        return ProcessResult.success(hopped);
    }
}
