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
        long hopCount = solids.stream().mapToLong(SolidInputView::count).sum();
        if (hopCount < config.requiredAdditionItems()) {
            return ProcessResult.rejected("insufficient ingredient addition for boil");
        }
        if (config.temperature().rateFactor(context.temperatureCelsius()) <= 0.0) {
            return ProcessResult.rejected("temperature is outside the boil operating range");
        }
        double utilization = config.temperature().extractionYield(context.temperatureCelsius());
        HopProfile profile = config.hopProfile();
        double bitterness = 0.0;
        double aroma = 0.0;
        int scheduleIndex = 0;
        int scheduleUnits = 0;
        for (SolidInputView solid : solids) {
            double bitternessPotential = solid.property(config.bitternessProperty())
                    .filter(Number.class::isInstance)
                    .map(Number.class::cast)
                    .map(Number::doubleValue)
                    .orElse(profile.bitternessPotential());
            double aromaPotential = solid.property(config.aromaProperty())
                    .filter(Number.class::isInstance)
                    .map(Number.class::cast)
                    .map(Number::doubleValue)
                    .orElse(profile.aromaPotential());
            var roleProperty = solid.property(ResourceId.parse("alcoholic:addition_role"));
            var progressProperty = solid.property(ResourceId.parse("alcoholic:addition_progress"))
                    .filter(Number.class::isInstance)
                    .map(Number.class::cast)
                    .map(Number::doubleValue);
            if (roleProperty.isPresent() || progressProperty.isPresent()) {
                BoilConfig.BoilAddition fallback = scheduledAddition(config, scheduleIndex);
                String role = roleProperty.map(String::valueOf).orElse(fallback.role());
                double at = progressProperty.orElse(fallback.atProgress());
                bitterness += bitternessPotential * solid.count() * bitternessWeight(role, at);
                aroma += aromaPotential * solid.count() * aromaWeight(role, at);
                continue;
            }
            int remaining = solid.count();
            while (remaining > 0) {
                BoilConfig.BoilAddition planned = scheduledAddition(config, scheduleIndex);
                int available = config.additions().isEmpty() || scheduleIndex >= config.additions().size() - 1
                        ? remaining
                        : config.additionAmount() - scheduleUnits;
                int allocated = Math.min(remaining, Math.max(1, available));
                bitterness += bitternessPotential * allocated
                        * bitternessWeight(planned.role(), planned.atProgress());
                aroma += aromaPotential * allocated * aromaWeight(planned.role(), planned.atProgress());
                remaining -= allocated;
                scheduleUnits += allocated;
                if (scheduleUnits >= config.additionAmount()
                        && scheduleIndex < config.additions().size() - 1) {
                    scheduleIndex++;
                    scheduleUnits = 0;
                }
            }
        }
        bitterness *= utilization;
        aroma *= utilization;
        double existingBitterness = batch.number(config.bitternessProperty(), 0.0);
        double existingAroma = batch.number(config.aromaProperty(), 0.0);
        LiquidBatch hopped = batch
                .withBaseLiquid(config.outputLiquid().orElseThrow())
                .withProperty(config.bitternessProperty(), existingBitterness + bitterness)
                .withProperty(config.aromaProperty(), existingAroma + aroma)
                .withProperty(ResourceId.parse("alcoholic:temperature"), context.temperatureCelsius());
        return ProcessResult.success(hopped);
    }

    static double bitternessWeight(String role, double atProgress) {
        double timing = 1.0 - 0.75 * normalizedProgress(atProgress);
        double roleWeight = switch (BoilConfig.BoilAddition.normalizeRole(role)) {
            case "bittering" -> 1.0;
            case "aroma" -> 0.15;
            default -> 1.0;
        };
        return roleWeight * timing;
    }

    static double aromaWeight(String role, double atProgress) {
        double timing = 0.25 + 0.75 * normalizedProgress(atProgress);
        double roleWeight = switch (BoilConfig.BoilAddition.normalizeRole(role)) {
            case "bittering" -> 0.20;
            case "aroma" -> 1.0;
            default -> 1.0;
        };
        return roleWeight * timing;
    }

    private static double normalizedProgress(double progress) {
        if (!Double.isFinite(progress)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, progress));
    }

    private static BoilConfig.BoilAddition scheduledAddition(BoilConfig config, int index) {
        if (config.additions().isEmpty()) {
            return new BoilConfig.BoilAddition(config.inputSelector().orElse(null), 0.0, "dual");
        }
        return config.additions().get(Math.min(Math.max(0, index), config.additions().size() - 1));
    }
}
