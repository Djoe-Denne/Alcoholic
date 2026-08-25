package com.djden.alcoholic.domain.process;

/**
 * Inclusive temperature interval in Celsius.
 */
public record TemperatureBand(double min, double max) {
    public TemperatureBand {
        if (!Double.isFinite(min) || !Double.isFinite(max) || max < min) {
            throw new IllegalArgumentException("temperature band is invalid");
        }
    }

    public boolean contains(double celsius) {
        return celsius >= min && celsius <= max;
    }
}
