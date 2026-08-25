package com.djden.alcoholic.domain.vessel;

import com.djden.alcoholic.api.vessel.EnvironmentProfileView;

public record EnvironmentProfile(double temperature, double stability, boolean sheltered, double humidity)
        implements EnvironmentProfileView {
    public EnvironmentProfile {
        if (!Double.isFinite(temperature)) {
            throw new IllegalArgumentException("temperature must be finite");
        }
        if (!Double.isFinite(stability) || stability < 0.0 || stability > 1.0) {
            throw new IllegalArgumentException("stability must be between 0 and 1");
        }
        if (!Double.isFinite(humidity) || humidity < 0.0 || humidity > 1.0) {
            humidity = 0.5;
        }
    }

    public EnvironmentProfile(double temperature, double stability, boolean sheltered) {
        this(temperature, stability, sheltered, 0.5);
    }

    public static EnvironmentProfile temperateCellar() {
        return new EnvironmentProfile(14.0, 0.85, true, 0.55);
    }

    public static EnvironmentProfile exposed(double temperature) {
        return new EnvironmentProfile(temperature, 0.35, false, 0.35);
    }

    public double agingRateFactor() {
        double stabilityBonus = 0.75 + (stability * 0.35);
        return sheltered ? stabilityBonus : stabilityBonus * 0.85;
    }

    public double oxidationFactor() {
        return sheltered ? 0.4 : 1.2;
    }
}
