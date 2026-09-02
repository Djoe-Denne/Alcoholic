package com.djden.alcoholic.api.process;

import com.djden.alcoholic.api.PublicApi;

/**
 * Executor-level scale knobs. They never mutate a process definition; they
 * only scale how a machine applies one. Quality is not a magic score here:
 * {@code processFidelity}, {@code complexityCap}, and {@code purityFloor}
 * constrain the derived {@code QualityProfile}.
 */
@PublicApi
public record ExecutorModifiers(
        double yieldModifier,
        double speedModifier,
        double thermalStability,
        int maxBatchUnits,
        double processFidelity,
        double complexityCap,
        double purityFloor
) {
    public ExecutorModifiers {
        if (!Double.isFinite(yieldModifier) || yieldModifier < 0.0) {
            yieldModifier = 1.0;
        }
        if (!Double.isFinite(speedModifier) || speedModifier <= 0.0) {
            speedModifier = 1.0;
        }
        if (!Double.isFinite(thermalStability) || thermalStability < 1.0) {
            thermalStability = 1.0;
        }
        if (maxBatchUnits < 1) {
            maxBatchUnits = 1;
        }
        if (!Double.isFinite(processFidelity) || processFidelity < 0.0) {
            processFidelity = 1.0;
        }
        processFidelity = Math.min(1.0, processFidelity);
        if (!Double.isFinite(complexityCap) || complexityCap <= 0.0) {
            complexityCap = 1.0;
        }
        complexityCap = Math.min(1.0, complexityCap);
        if (!Double.isFinite(purityFloor) || purityFloor < 0.0) {
            purityFloor = 0.0;
        }
        purityFloor = Math.min(1.0, purityFloor);
    }

    public ExecutorModifiers(
            double yieldModifier,
            double speedModifier,
            double thermalStability,
            int maxBatchUnits
    ) {
        this(yieldModifier, speedModifier, thermalStability, maxBatchUnits, 1.0, 1.0, 0.0);
    }

    public double scaleDelta(double deltaTicks) {
        if (!Double.isFinite(deltaTicks) || deltaTicks <= 0.0) {
            return 0.0;
        }
        return deltaTicks * speedModifier;
    }

    public static ExecutorModifiers identity() {
        return new ExecutorModifiers(1.0, 1.0, 1.0, 1, 1.0, 1.0, 0.0);
    }

    public static ExecutorModifiers artisanal() {
        return new ExecutorModifiers(1.0, 1.0, 1.0, 1, 1.0, 1.0, 0.0);
    }

    public static ExecutorModifiers artisanalPress() {
        return new ExecutorModifiers(1.0, 1.0, 1.0, 1, 1.0, 1.0, 0.0);
    }

    public static ExecutorModifiers industrialPress() {
        return new ExecutorModifiers(1.05, 2.0, 1.0, Integer.MAX_VALUE, 0.70, 0.55, 0.15);
    }

    public static ExecutorModifiers industrialVat() {
        return new ExecutorModifiers(1.0, 1.0, 4.0, 1, 0.70, 0.55, 0.12);
    }

    /**
     * Native Malt Mill: good yield, moderate standalone throughput.
     */
    public static ExecutorModifiers maltMill() {
        return new ExecutorModifiers(1.0, 1.0, 1.0, 1, 1.0, 1.0, 0.0);
    }

    public static ExecutorModifiers industrialMaltHouse() {
        return new ExecutorModifiers(1.0, 2.0, 2.0, Integer.MAX_VALUE, 0.70, 0.55, 0.15);
    }

    public static ExecutorModifiers industrialRollerMill() {
        return new ExecutorModifiers(1.0, 4.0, 1.0, Integer.MAX_VALUE, 0.70, 0.55, 0.15);
    }

    public static ExecutorModifiers industrialMashTun() {
        return new ExecutorModifiers(1.05, 1.5, 6.0, Integer.MAX_VALUE, 0.70, 0.55, 0.12);
    }

    public static ExecutorModifiers industrialBrewingKettle() {
        return new ExecutorModifiers(1.0, 1.5, 3.0, 1, 0.70, 0.55, 0.12);
    }

    public static ExecutorModifiers industrialConditioningVessel() {
        return new ExecutorModifiers(1.0, 1.0, 3.0, 1, 0.70, 0.55, 0.15);
    }

    public static ExecutorModifiers industrialAgingVessel() {
        return new ExecutorModifiers(1.0, 1.0, 3.0, 1, 0.70, 0.55, 0.15);
    }

    public static ExecutorModifiers craftMaltHouse() {
        return new ExecutorModifiers(1.0, 1.25, 1.5, 8, 0.94, 0.82, 0.04);
    }

    public static ExecutorModifiers craftMill() {
        return new ExecutorModifiers(1.0, 2.0, 1.0, 8, 0.94, 0.82, 0.04);
    }

    public static ExecutorModifiers craftMashTun() {
        return new ExecutorModifiers(1.02, 1.25, 3.0, 8, 0.94, 0.82, 0.04);
    }

    public static ExecutorModifiers craftBrewingKettle() {
        return new ExecutorModifiers(1.0, 1.25, 2.0, 1, 0.94, 0.82, 0.04);
    }

    public static ExecutorModifiers craftVat() {
        return new ExecutorModifiers(1.0, 1.25, 2.0, 1, 0.94, 0.82, 0.04);
    }
}
