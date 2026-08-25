package com.djden.alcoholic.domain.process;

public record AgingKinetics(
        double maturityRatePerTick,
        double woodRatePerTick,
        double oxidationRatePerTick,
        double completionThreshold
) {
    public AgingKinetics {
        if (!Double.isFinite(maturityRatePerTick) || maturityRatePerTick < 0.0) {
            maturityRatePerTick = 1.0 / 12_000.0;
        }
        if (!Double.isFinite(woodRatePerTick) || woodRatePerTick < 0.0) {
            woodRatePerTick = maturityRatePerTick;
        }
        if (!Double.isFinite(oxidationRatePerTick) || oxidationRatePerTick < 0.0) {
            oxidationRatePerTick = maturityRatePerTick * 0.15;
        }
        if (!Double.isFinite(completionThreshold) || completionThreshold <= 0.0) {
            completionThreshold = 1.0;
        }
    }

    public static AgingKinetics simplified() {
        return new AgingKinetics(1.0 / 12_000.0, 1.0 / 12_000.0, 1.0 / 80_000.0, 1.0);
    }
}
