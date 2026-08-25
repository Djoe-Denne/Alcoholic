package com.djden.alcoholic.domain.vessel;

import com.djden.alcoholic.api.vessel.EnvironmentProfileView;

public record EnvironmentProfile(double temperature, double stability, boolean sheltered)
        implements EnvironmentProfileView {
    public EnvironmentProfile {
        if (!Double.isFinite(temperature)) {
            throw new IllegalArgumentException("temperature must be finite");
        }
        if (!Double.isFinite(stability) || stability < 0.0 || stability > 1.0) {
            throw new IllegalArgumentException("stability must be between 0 and 1");
        }
    }

    public static EnvironmentProfile temperateCellar() {
        return new EnvironmentProfile(14.0, 0.85, true);
    }

    public static EnvironmentProfile exposed(double temperature) {
        return new EnvironmentProfile(temperature, 0.35, false);
    }

    public double agingRateFactor() {
        double stabilityBonus = 0.75 + (stability * 0.35);
        return sheltered ? stabilityBonus : stabilityBonus * 0.85;
    }

    public double oxidationFactor() {
        return sheltered ? 0.4 : 1.2;
    }
}
