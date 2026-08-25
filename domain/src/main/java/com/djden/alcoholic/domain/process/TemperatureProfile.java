package com.djden.alcoholic.domain.process;

/**
 * Data-driven temperature behaviour for a time-dependent process.
 */
public record TemperatureProfile(
        TemperatureBand preferred,
        TemperatureBand operating,
        TemperatureBand damaging
) {
    public TemperatureProfile {
        if (preferred == null) {
            preferred = new TemperatureBand(18.0, 24.0);
        }
        if (operating == null) {
            operating = new TemperatureBand(10.0, 30.0);
        }
        if (damaging == null) {
            damaging = new TemperatureBand(-40.0, 80.0);
        }
    }

    public static TemperatureProfile fermentationDefault() {
        return new TemperatureProfile(
                new TemperatureBand(18.0, 24.0),
                new TemperatureBand(10.0, 30.0),
                new TemperatureBand(-20.0, 45.0)
        );
    }

    public double rateFactor(double celsius) {
        if (preferred.contains(celsius)) {
            return 1.0;
        }
        if (operating.contains(celsius)) {
            return 0.35;
        }
        return 0.0;
    }

    public boolean stalled(double celsius) {
        return !operating.contains(celsius);
    }

    public boolean damaging(double celsius) {
        return damaging.contains(celsius)
                && (celsius < operating.min() || celsius > operating.max());
    }

    public boolean stressed(double celsius) {
        return !preferred.contains(celsius);
    }
}
