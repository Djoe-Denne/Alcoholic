package com.djden.alcoholic.domain.viticulture;

public record PruningProfile(
        double yieldMultiplier,
        double qualityMultiplier
) {
    public PruningProfile {
        requireMultiplier(yieldMultiplier, "yieldMultiplier");
        requireMultiplier(qualityMultiplier, "qualityMultiplier");
    }

    private static void requireMultiplier(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0 || value > 2.0) {
            throw new IllegalArgumentException(name + " must be between 0 and 2");
        }
    }
}
