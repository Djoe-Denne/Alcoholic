package com.djden.alcoholic.api.process;

import com.djden.alcoholic.api.PublicApi;

/**
 * Executor-level quality and scale knobs. They never mutate a process
 * definition; they only scale how a machine applies one.
 */
@PublicApi
public record ExecutorModifiers(
        double yieldModifier,
        double speedModifier,
        double thermalStability,
        int maxBatchUnits
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
    }

    public static ExecutorModifiers identity() {
        return new ExecutorModifiers(1.0, 1.0, 1.0, 1);
    }

    public static ExecutorModifiers industrialPress() {
        return new ExecutorModifiers(1.05, 2.0, 1.0, Integer.MAX_VALUE);
    }

    public static ExecutorModifiers industrialVat() {
        return new ExecutorModifiers(1.0, 1.0, 4.0, 1);
    }

    /**
     * Native Malt Mill: good yield, moderate standalone throughput.
     */
    public static ExecutorModifiers maltMill() {
        return new ExecutorModifiers(1.0, 1.0, 1.0, 1);
    }

    public static ExecutorModifiers industrialMaltHouse() {
        return new ExecutorModifiers(1.0, 2.0, 2.0, Integer.MAX_VALUE);
    }

    public static ExecutorModifiers industrialRollerMill() {
        return new ExecutorModifiers(1.0, 4.0, 1.0, Integer.MAX_VALUE);
    }

    public static ExecutorModifiers industrialMashTun() {
        return new ExecutorModifiers(1.05, 1.5, 6.0, Integer.MAX_VALUE);
    }

    public static ExecutorModifiers industrialBrewingKettle() {
        return new ExecutorModifiers(1.0, 1.5, 3.0, 1);
    }

    public static ExecutorModifiers industrialConditioningVessel() {
        return new ExecutorModifiers(1.0, 1.0, 3.0, 1);
    }
}
