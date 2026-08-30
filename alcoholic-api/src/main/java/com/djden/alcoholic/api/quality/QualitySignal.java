package com.djden.alcoholic.api.quality;

import com.djden.alcoholic.api.PublicApi;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Named 0–1 ports produced by one quality-graph node.
 */
@PublicApi
public record QualitySignal(Map<String, Double> ports) {
    public QualitySignal {
        Map<String, Double> copy = new LinkedHashMap<>();
        Objects.requireNonNull(ports, "ports").forEach((name, value) -> copy.put(
                Objects.requireNonNull(name, "port"),
                clamp01(value)
        ));
        ports = Map.copyOf(copy);
    }

    public static QualitySignal empty() {
        return new QualitySignal(Map.of());
    }

    public static QualitySignal of(String port, double value) {
        return new QualitySignal(Map.of(port, value));
    }

    public static QualitySignal value(double value) {
        return of("value", value);
    }

    public double get(String port, double fallback) {
        Double value = ports.get(port);
        return value == null ? fallback : value;
    }

    public double getPortOrValue(String port, double fallback) {
        Double value = ports.get(port);
        if (value != null) {
            return value;
        }
        Double scalar = ports.get("value");
        return scalar == null ? fallback : scalar;
    }

    public QualitySignal with(String port, double value) {
        Map<String, Double> next = new LinkedHashMap<>(ports);
        next.put(port, value);
        return new QualitySignal(next);
    }

    private static double clamp01(double value) {
        if (!Double.isFinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}
