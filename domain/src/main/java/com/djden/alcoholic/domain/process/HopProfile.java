package com.djden.alcoholic.domain.process;

/**
 * Data-driven hop extract potentials. Addons vary values in process JSON.
 */
public record HopProfile(double bitternessPotential, double aromaPotential) {
    public HopProfile {
        bitternessPotential = clamp(bitternessPotential);
        aromaPotential = clamp(aromaPotential);
    }

    public static HopProfile generic() {
        return new HopProfile(0.55, 0.40);
    }

    private static double clamp(double value) {
        if (!Double.isFinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}
