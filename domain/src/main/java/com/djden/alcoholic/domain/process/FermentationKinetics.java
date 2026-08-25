package com.djden.alcoholic.domain.process;

/**
 * Simplified continuous fermentation kinetics. Units are process-data defined.
 */
public record FermentationKinetics(
        double conversionFactor,
        double baseRatePerTick,
        double completionThreshold,
        double co2PerSugar
) {
    public FermentationKinetics {
        if (!Double.isFinite(conversionFactor) || conversionFactor < 0.0) {
            throw new IllegalArgumentException("conversionFactor must be >= 0");
        }
        if (!Double.isFinite(baseRatePerTick) || baseRatePerTick < 0.0) {
            throw new IllegalArgumentException("baseRatePerTick must be >= 0");
        }
        if (!Double.isFinite(completionThreshold) || completionThreshold < 0.0) {
            throw new IllegalArgumentException("completionThreshold must be >= 0");
        }
        if (!Double.isFinite(co2PerSugar) || co2PerSugar < 0.0) {
            throw new IllegalArgumentException("co2PerSugar must be >= 0");
        }
    }

    public static FermentationKinetics simplified() {
        return new FermentationKinetics(0.47, 1.0 / 12_000.0, 0.02, 0.45);
    }
}
