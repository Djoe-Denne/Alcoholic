package com.djden.alcoholic.domain.multiblock;

import com.djden.alcoholic.domain.mechanical.MechanicalDriveState;
import com.djden.alcoholic.domain.mechanical.MechanicalRequirement;

/**
 * Compatibility view of a {@link MechanicalRequirement} for existing machine
 * JSON that still uses {@code min_rpm} / {@code max_rpm}. Those numbers are
 * Alcoholic speed units; Create adapters map their own RPM into the same
 * scale. Capacity is also an Alcoholic unit, never a Create SU or Crossroads
 * joule.
 */
public record KineticRequirement(double minRpm, double maxRpm, double requiredCapacity, boolean required) {
    public KineticRequirement {
        if (!Double.isFinite(minRpm) || minRpm < 0.0) {
            minRpm = 0.0;
        }
        if (!Double.isFinite(maxRpm) || maxRpm < minRpm) {
            maxRpm = minRpm;
        }
        if (!Double.isFinite(requiredCapacity) || requiredCapacity < 0.0) {
            requiredCapacity = 0.0;
        }
    }

    public KineticRequirement(double minRpm, double maxRpm, boolean required) {
        this(minRpm, maxRpm, required ? 1.0 : 0.0, required);
    }

    public static KineticRequirement none() {
        return new KineticRequirement(0.0, 0.0, 0.0, false);
    }

    public static KineticRequirement industrialPress() {
        return from(MechanicalRequirement.industrialPress());
    }

    public static KineticRequirement industrialRollerMill() {
        return from(MechanicalRequirement.industrialRollerMill());
    }

    public static KineticRequirement craftMill() {
        return from(MechanicalRequirement.craftMill());
    }

    public static KineticRequirement from(MechanicalRequirement mechanical) {
        MechanicalRequirement value = mechanical == null ? MechanicalRequirement.none() : mechanical;
        return new KineticRequirement(
                value.minSpeed(),
                value.maxSpeed(),
                value.requiredCapacity(),
                value.required()
        );
    }

    public MechanicalRequirement asMechanical() {
        if (!required) {
            return MechanicalRequirement.none();
        }
        return new MechanicalRequirement(minRpm, maxRpm, requiredCapacity, true);
    }

    public boolean satisfied(double rpm) {
        if (!required) {
            return true;
        }
        return asMechanical().satisfied(MechanicalDriveState.running(rpm, Double.POSITIVE_INFINITY));
    }

    public boolean satisfied(MechanicalDriveState state) {
        return asMechanical().satisfied(state);
    }
}
