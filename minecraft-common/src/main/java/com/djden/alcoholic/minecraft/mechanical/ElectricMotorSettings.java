package com.djden.alcoholic.minecraft.mechanical;

/**
 * Tunable electric-motor conversion. First implementation uses a fixed
 * output speed; FE draw scales with the mechanical load that was actually
 * consumed this tick.
 */
public record ElectricMotorSettings(
        int energyCapacity,
        int maxReceivePerTick,
        double outputSpeed,
        double maxLoad,
        double efficiency,
        int fePerCapacity
) {
    public static final ElectricMotorSettings DEFAULT = new ElectricMotorSettings(
            8_000,
            80,
            32.0,
            8.0,
            0.8,
            20
    );

    public ElectricMotorSettings {
        energyCapacity = Math.max(0, energyCapacity);
        maxReceivePerTick = Math.max(0, maxReceivePerTick);
        if (!Double.isFinite(outputSpeed) || outputSpeed < 0.0) {
            outputSpeed = 0.0;
        }
        if (!Double.isFinite(maxLoad) || maxLoad < 0.0) {
            maxLoad = 0.0;
        }
        if (!Double.isFinite(efficiency) || efficiency <= 0.0) {
            efficiency = 1.0;
        }
        fePerCapacity = Math.max(1, fePerCapacity);
    }

    public int feForLoad(double load) {
        if (!Double.isFinite(load) || load <= 0.0) {
            return 0;
        }
        return (int) Math.ceil(load * fePerCapacity / efficiency);
    }

    public double capacityFromEnergy(int stored) {
        if (stored <= 0) {
            return 0.0;
        }
        return Math.min(maxLoad, stored * efficiency / (double) fePerCapacity);
    }
}
