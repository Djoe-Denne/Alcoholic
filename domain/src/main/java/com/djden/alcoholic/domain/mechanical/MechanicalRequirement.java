package com.djden.alcoholic.domain.mechanical;

/**
 * What a machine needs from {@link MechanicalDrivePort}. Capacity and speed
 * are Alcoholic units, not Create RPM or stress.
 */
public record MechanicalRequirement(
        double minSpeed,
        double maxSpeed,
        double requiredCapacity,
        boolean required
) {
    public MechanicalRequirement {
        if (!Double.isFinite(minSpeed) || minSpeed < 0.0) {
            minSpeed = 0.0;
        }
        if (!Double.isFinite(maxSpeed) || maxSpeed < minSpeed) {
            maxSpeed = minSpeed;
        }
        if (!Double.isFinite(requiredCapacity) || requiredCapacity < 0.0) {
            requiredCapacity = 0.0;
        }
    }

    public static MechanicalRequirement none() {
        return new MechanicalRequirement(0.0, 0.0, 0.0, false);
    }

    public static MechanicalRequirement maltMill() {
        return new MechanicalRequirement(8.0, 256.0, 1.0, true);
    }

    public static MechanicalRequirement industrialPress() {
        return new MechanicalRequirement(16.0, 256.0, 1.0, true);
    }

    public boolean satisfied(MechanicalDriveState state) {
        if (!required) {
            return true;
        }
        if (state == null || !state.usable()) {
            return false;
        }
        if (state.speed() + 1e-9 < minSpeed) {
            return false;
        }
        if (maxSpeed > 0.0 && state.speed() - 1e-9 > maxSpeed) {
            return false;
        }
        return state.availableCapacity() + 1e-9 >= requiredCapacity;
    }
}
