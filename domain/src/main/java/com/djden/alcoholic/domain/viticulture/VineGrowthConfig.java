package com.djden.alcoholic.domain.viticulture;

import java.util.Objects;

public record VineGrowthConfig(
        double baseGrowthChance,
        ClimateProfile climateProfile,
        double progressIncrement
) {
    public VineGrowthConfig {
        if (!Double.isFinite(baseGrowthChance)
                || baseGrowthChance < 0.0
                || baseGrowthChance > 1.0) {
            throw new IllegalArgumentException("baseGrowthChance must be between 0 and 1");
        }
        Objects.requireNonNull(climateProfile, "climateProfile");
        if (!Double.isFinite(progressIncrement)
                || progressIncrement <= 0.0
                || progressIncrement > 1.0) {
            throw new IllegalArgumentException("progressIncrement must be between 0 exclusive and 1 inclusive");
        }
    }

    public VineGrowthConfig(
            double baseGrowthChance,
            ClimateProfile climateProfile
    ) {
        this(baseGrowthChance, climateProfile, 1.0);
    }

    public static VineGrowthConfig defaults() {
        return new VineGrowthConfig(0.35, ClimateProfile.TEMPERATE, 0.25);
    }
}
