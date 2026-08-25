package com.djden.alcoholic.domain.viticulture;

import java.util.Objects;

public record ClimateProfile(
        VineEnvironment idealEnvironment,
        double temperatureTolerance,
        double humidityTolerance,
        double lightTolerance
) {
    public static final ClimateProfile COOL = new ClimateProfile(
            new VineEnvironment(16.0, 0.70, 0.65),
            14.0,
            0.35,
            0.45
    );
    public static final ClimateProfile TEMPERATE = new ClimateProfile(
            new VineEnvironment(22.0, 0.65, 0.75),
            18.0,
            0.35,
            0.50
    );
    public static final ClimateProfile WARM = new ClimateProfile(
            new VineEnvironment(28.0, 0.55, 0.85),
            18.0,
            0.40,
            0.50
    );

    public ClimateProfile {
        Objects.requireNonNull(idealEnvironment, "idealEnvironment");
        requirePositiveFinite(temperatureTolerance, "temperatureTolerance");
        requirePositiveFinite(humidityTolerance, "humidityTolerance");
        requirePositiveFinite(lightTolerance, "lightTolerance");
    }

    public double suitability(VineEnvironment environment) {
        Objects.requireNonNull(environment, "environment");
        double temperatureSuitability = suitabilityFor(
                environment.temperatureCelsius(),
                idealEnvironment.temperatureCelsius(),
                temperatureTolerance
        );
        double humiditySuitability = suitabilityFor(
                environment.humidity(),
                idealEnvironment.humidity(),
                humidityTolerance
        );
        double lightSuitability = suitabilityFor(
                environment.light(),
                idealEnvironment.light(),
                lightTolerance
        );
        return (temperatureSuitability + humiditySuitability + lightSuitability) / 3.0;
    }

    public double warmth(VineEnvironment environment) {
        Objects.requireNonNull(environment, "environment");
        return clamp(
                (environment.temperatureCelsius() - idealEnvironment.temperatureCelsius())
                        / temperatureTolerance,
                -1.0,
                1.0
        );
    }

    private static double suitabilityFor(double actual, double ideal, double tolerance) {
        return clamp(1.0 - Math.abs(actual - ideal) / tolerance, 0.0, 1.0);
    }

    private static void requirePositiveFinite(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be positive and finite");
        }
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
