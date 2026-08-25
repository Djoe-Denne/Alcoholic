package com.djden.alcoholic.domain.multiblock;

import com.djden.alcoholic.domain.mechanical.MechanicalDriveState;
import com.djden.alcoholic.domain.mechanical.MechanicalRequirement;

/**
 * Compatibility view of a {@link MechanicalRequirement} for existing machine
 * JSON that still uses {@code min_rpm} / {@code max_rpm}. Those numbers are
 * Alcoholic speed units; Create adapters map their own RPM into the same
 * scale.
 */
public record KineticRequirement(double minRpm, double maxRpm, boolean required) {
    public KineticRequirement {
        if (!Double.isFinite(minRpm) || minRpm < 0.0) {
            minRpm = 0.0;
        }
        if (!Double.isFinite(maxRpm) || maxRpm < minRpm) {
            maxRpm = minRpm;
        }
    }

    public static KineticRequirement none() {
        return new KineticRequirement(0.0, 0.0, false);
    }

    public static KineticRequirement industrialPress() {
        return from(MechanicalRequirement.industrialPress());
    }

    public static KineticRequirement from(MechanicalRequirement mechanical) {
        MechanicalRequirement value = mechanical == null ? MechanicalRequirement.none() : mechanical;
        return new KineticRequirement(value.minSpeed(), value.maxSpeed(), value.required());
    }

    public MechanicalRequirement asMechanical() {
        if (!required) {
            return MechanicalRequirement.none();
        }
        return new MechanicalRequirement(minRpm, maxRpm, 1.0, true);
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
