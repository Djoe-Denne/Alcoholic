package com.djden.alcoholic.domain.process;

/**
 * Damps ambient temperature toward a closed-vessel baseline.
 * {@code stability == 1} is fully exposed; larger values reduce fluctuation.
 */
public final class ThermalStability {
    private ThermalStability() {
    }

    public static double effectiveCelsius(double ambient, double baseline, double stability) {
        double factor = !Double.isFinite(stability) || stability < 1.0 ? 1.0 : stability;
        if (!Double.isFinite(ambient)) {
            return baseline;
        }
        return baseline + (ambient - baseline) / factor;
    }
}
