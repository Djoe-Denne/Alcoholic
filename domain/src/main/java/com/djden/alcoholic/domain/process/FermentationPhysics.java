package com.djden.alcoholic.domain.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.LiquidBatch;

import java.util.Objects;

/**
 * Progressive sugar-to-ethanol conversion. Beverage identity is not consulted.
 * Produced CO₂ is accumulated then vented by the vessel; dissolved carbonation
 * is a separate {@code alcoholic:carbonation} property written by CONDITION.
 */
public final class FermentationPhysics {
    private FermentationPhysics() {
    }

    public static FermentationState step(
            FermentationState state,
            FermentationKinetics kinetics,
            TemperatureProfile profile,
            ResourceId sugarProperty,
            ResourceId ethanolProperty,
            ResourceId stressProperty,
            ResourceId outputLiquid,
            double temperatureCelsius,
            double deltaTicks
    ) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(kinetics, "kinetics");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(sugarProperty, "sugarProperty");
        Objects.requireNonNull(ethanolProperty, "ethanolProperty");
        if (state.complete() || deltaTicks <= 0.0 || !state.yeastPitched()) {
            return state;
        }

        double sugar = state.batch().number(sugarProperty, 0.0);
        double ethanol = state.batch().number(ethanolProperty, 0.0);
        double stress = state.stress();
        double rateFactor = profile.rateFactor(temperatureCelsius);
        if (profile.stressed(temperatureCelsius)) {
            stress += deltaTicks * (profile.stalled(temperatureCelsius) ? 0.0004 : 0.0001);
        }
        double consumed = Math.min(sugar, kinetics.baseRatePerTick() * rateFactor * deltaTicks);
        double remainingSugar = Math.max(0.0, sugar - consumed);
        double nextEthanol = ethanol + consumed * kinetics.conversionFactor();
        double co2 = state.co2Accumulated() + consumed * kinetics.co2PerSugar();
        LiquidBatch batch = state.batch()
                .withProperty(sugarProperty, remainingSugar)
                .withProperty(ethanolProperty, nextEthanol);
        if (stressProperty != null) {
            batch = batch.withProperty(stressProperty, stress);
        }
        boolean complete = remainingSugar <= kinetics.completionThreshold();
        if (complete) {
            batch = batch
                    .withProperty(sugarProperty, 0.0)
                    .withBaseLiquid(outputLiquid);
        }
        return new FermentationState(batch, true, complete, co2, stress);
    }
}
