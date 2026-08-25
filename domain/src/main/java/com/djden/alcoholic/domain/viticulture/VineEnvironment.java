package com.djden.alcoholic.domain.viticulture;

public record VineEnvironment(
        double temperatureCelsius,
        double humidity,
        double light
) {
    public VineEnvironment {
        requireFinite(temperatureCelsius, "temperatureCelsius");
        requireFinite(humidity, "humidity");
        requireFinite(light, "light");
        if (temperatureCelsius < -100.0 || temperatureCelsius > 100.0) {
            throw new IllegalArgumentException("temperatureCelsius must be between -100 and 100");
        }
        requireUnitInterval(humidity, "humidity");
        requireUnitInterval(light, "light");
    }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }

    private static void requireUnitInterval(double value, String name) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " must be between 0 and 1");
        }
    }
}
