package com.djden.alcoholic.domain.viticulture;

public record VineHealth(
        double growthMultiplier,
        double yieldMultiplier,
        double qualityModifier
) {
    public static final VineHealth POOR = new VineHealth(0.55, 0.65, -0.15);
    public static final VineHealth STRESSED = new VineHealth(0.75, 0.80, -0.08);
    public static final VineHealth HEALTHY = new VineHealth(1.00, 1.00, 0.00);
    public static final VineHealth THRIVING = new VineHealth(1.15, 1.10, 0.08);

    public VineHealth {
        requireMultiplier(growthMultiplier, "growthMultiplier");
        requireMultiplier(yieldMultiplier, "yieldMultiplier");
        if (!Double.isFinite(qualityModifier)
                || qualityModifier < -1.0
                || qualityModifier > 1.0) {
            throw new IllegalArgumentException("qualityModifier must be between -1 and 1");
        }
    }

    private static void requireMultiplier(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0 || value > 2.0) {
            throw new IllegalArgumentException(name + " must be between 0 and 2");
        }
    }
}
