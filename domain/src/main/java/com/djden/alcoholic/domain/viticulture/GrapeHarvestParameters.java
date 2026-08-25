package com.djden.alcoholic.domain.viticulture;

import java.util.Objects;

public record GrapeHarvestParameters(
        VineEnvironment environment,
        double trellisingMultiplier,
        long harvestTime
) {
    public GrapeHarvestParameters {
        Objects.requireNonNull(environment, "environment");
        if (!Double.isFinite(trellisingMultiplier)
                || trellisingMultiplier < 0.0
                || trellisingMultiplier > 2.0) {
            throw new IllegalArgumentException(
                    "trellisingMultiplier must be between 0 and 2"
            );
        }
        if (harvestTime < 0L) {
            throw new IllegalArgumentException("harvestTime must not be negative");
        }
    }

    public GrapeHarvestParameters(
            VineEnvironment environment,
            double trellisingMultiplier
    ) {
        this(environment, trellisingMultiplier, 0L);
    }

    public GrapeHarvestParameters(VineEnvironment environment) {
        this(environment, 1.0, 0L);
    }

    public static GrapeHarvestParameters at(
            VineEnvironment environment,
            long harvestTime
    ) {
        return new GrapeHarvestParameters(environment, 1.0, harvestTime);
    }
}
