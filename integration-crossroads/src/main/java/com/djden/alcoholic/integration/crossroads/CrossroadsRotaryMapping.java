package com.djden.alcoholic.integration.crossroads;

import com.djden.alcoholic.domain.mechanical.MechanicalDirection;
import com.djden.alcoholic.domain.mechanical.MechanicalDriveState;

/**
 * Converts Crossroads rotary quantities into Alcoholic mechanical units.
 *
 * <p>Crossroads reports axle speed in radians/second and stores rotary
 * energy in joules on the shared axis network. Alcoholic machines keep
 * using dimensionless speed (RPM-equivalent) and capacity. This class is
 * the only place that mapping lives.
 *
 * <pre>
 *   Alcoholic speed (RPM) = rad/s * 60 / (2π)
 *
 *   1 Alcoholic capacity unit  = 20 J of Crossroads rotary energy
 *   availableCapacity          = axleEnergyJoules / 20
 *   work this tick             = requiredCapacity * 20 J   (removed via addEnergy)
 *
 *   Moment of inertia exposed on the Alcoholic consumer is 1.25 kg·m².
 *   That lets the machine join the axis network; it is not a claim that
 *   Alcoholic simulates Crossroads inertia. Load is expressed as energy
 *   removed when the machine actually works, not as a change to Alcoholic's
 *   MechanicalRequirement model.
 * </pre>
 */
public final class CrossroadsRotaryMapping {
    public static final double JOULES_PER_CAPACITY = 20.0;
    public static final double MOMENT_OF_INERTIA = 1.25;
    public static final double MAX_REPORTED_CAPACITY = 64.0;

    private CrossroadsRotaryMapping() {
    }

    public static double rpmFromRadPerSecond(double radPerSecond) {
        if (!Double.isFinite(radPerSecond)) {
            return 0.0;
        }
        return radPerSecond * 60.0 / (2.0 * Math.PI);
    }

    public static double joulesForLoad(double alcoholicCapacity) {
        if (!Double.isFinite(alcoholicCapacity) || alcoholicCapacity <= 0.0) {
            return 0.0;
        }
        return alcoholicCapacity * JOULES_PER_CAPACITY;
    }

    public static double capacityFromEnergy(double energyJoules) {
        if (!Double.isFinite(energyJoules) || energyJoules <= 0.0) {
            return 0.0;
        }
        return Math.min(MAX_REPORTED_CAPACITY, energyJoules / JOULES_PER_CAPACITY);
    }

    public static MechanicalDirection directionFrom(double signedSpeedRadPerSecond) {
        if (!Double.isFinite(signedSpeedRadPerSecond) || signedSpeedRadPerSecond == 0.0) {
            return MechanicalDirection.NONE;
        }
        return signedSpeedRadPerSecond > 0.0
                ? MechanicalDirection.CLOCKWISE
                : MechanicalDirection.COUNTER_CLOCKWISE;
    }

    public static MechanicalDriveState driveState(double signedRadPerSecond, double energyJoules) {
        double speed = Math.abs(rpmFromRadPerSecond(signedRadPerSecond));
        double capacity = capacityFromEnergy(energyJoules);
        MechanicalDirection direction = directionFrom(signedRadPerSecond);
        if (speed <= 0.0) {
            return MechanicalDriveState.idle();
        }
        if (capacity <= 0.0) {
            return new MechanicalDriveState(speed, 0.0, direction, false, true);
        }
        return new MechanicalDriveState(speed, capacity, direction, true, false);
    }
}
