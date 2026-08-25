package com.djden.alcoholic.domain.viticulture;

import java.util.Objects;

public record VineGrowthParameters(
        VineEnvironment environment,
        double trellisingMultiplier,
        double roll
) {
    public VineGrowthParameters {
        Objects.requireNonNull(environment, "environment");
        requireBetween(trellisingMultiplier, 0.0, 2.0, "trellisingMultiplier");
        requireBetween(roll, 0.0, 1.0, "roll");
    }

    public VineGrowthParameters(VineEnvironment environment, double roll) {
        this(environment, 1.0, roll);
    }

    private static void requireBetween(
            double value,
            double minimum,
            double maximum,
            String name
    ) {
        if (!Double.isFinite(value) || value < minimum || value > maximum) {
            throw new IllegalArgumentException(
                    name + " must be between " + minimum + " and " + maximum
            );
        }
    }
}
