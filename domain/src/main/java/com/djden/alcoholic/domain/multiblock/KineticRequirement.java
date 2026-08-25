package com.djden.alcoholic.domain.multiblock;

/**
 * Rotational acceptance window. Units are Create RPM when a kinetic network
 * is present; tests may inject the same numbers.
 */
public record KineticRequirement(double minRpm, double maxRpm, boolean required) {
    public KineticRequirement {
        if (!Double.isFinite(minRpm) || minRpm < 0.0) {
            minRpm = 0.0;
        }
        if (!Double.isFinite(maxRpm) || maxRpm < minRpm) {
            maxRpm = minRpm;
        }
    }

    public static KineticRequirement none() {
        return new KineticRequirement(0.0, 0.0, false);
    }

    public static KineticRequirement industrialPress() {
        return new KineticRequirement(16.0, 256.0, true);
    }

    public boolean satisfied(double rpm) {
        if (!required) {
            return true;
        }
        return Double.isFinite(rpm) && rpm >= minRpm && rpm <= maxRpm;
    }
}
