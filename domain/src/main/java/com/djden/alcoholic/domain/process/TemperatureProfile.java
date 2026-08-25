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

    /**
     * Aggregate extraction quality for mixed thermal processes such as mash.
     * Too cold is incomplete; preferred is full; too hot is degraded.
     */
    public double extractionYield(double celsius) {
        if (preferred.contains(celsius)) {
            return 1.0;
        }
        if (operating.contains(celsius) && celsius < preferred.min()) {
            return 0.40;
        }
        if (operating.contains(celsius) && celsius > preferred.max()) {
            return 0.55;
        }
        return 0.10;
    }

    public boolean stalled(double celsius) {
        return rateFactor(celsius) <= 0.0;
    }

    public boolean damaging(double celsius) {
        return damaging.contains(celsius)
                && (celsius < operating.min() || celsius > operating.max());
    }

    public boolean stressed(double celsius) {
        return !preferred.contains(celsius);
    }
}
