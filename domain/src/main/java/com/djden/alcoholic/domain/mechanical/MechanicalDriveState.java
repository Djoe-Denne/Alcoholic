package com.djden.alcoholic.domain.mechanical;

/**
 * Snapshot of a rotary drive as seen by an Alcoholic machine.
 *
 * <p>Units are Alcoholic's own: {@code speed} is a dimensionless rotation
 * rate and {@code availableCapacity} is a dimensionless load budget. Create
 * RPM or stress units must be translated by an adapter before they appear
 * here.</p>
 */
public record MechanicalDriveState(
        double speed,
        double availableCapacity,
        MechanicalDirection direction,
        boolean running,
        boolean stalled
) {
    public MechanicalDriveState {
        if (!Double.isFinite(speed) || speed < 0.0) {
            speed = 0.0;
        }
        if (Double.isNaN(availableCapacity) || availableCapacity < 0.0) {
            availableCapacity = 0.0;
        }
        direction = direction == null ? MechanicalDirection.NONE : direction;
        if (stalled) {
            running = false;
        }
        if (speed <= 0.0) {
            running = false;
        }
    }

    public static MechanicalDriveState idle() {
        return new MechanicalDriveState(0.0, 0.0, MechanicalDirection.NONE, false, false);
    }

    public static MechanicalDriveState running(double speed, double availableCapacity) {
        return new MechanicalDriveState(speed, availableCapacity, MechanicalDirection.NONE, true, false);
    }

    public static MechanicalDriveState stalled(double speed, double availableCapacity) {
        return new MechanicalDriveState(speed, availableCapacity, MechanicalDirection.NONE, false, true);
    }

    public boolean usable() {
        return running && !stalled && speed > 0.0;
    }

    public static MechanicalDriveState stronger(MechanicalDriveState left, MechanicalDriveState right) {
        MechanicalDriveState a = left == null ? idle() : left;
        MechanicalDriveState b = right == null ? idle() : right;
        if (a.usable() && !b.usable()) {
            return a;
        }
        if (b.usable() && !a.usable()) {
            return b;
        }
        return a.speed() >= b.speed() ? a : b;
    }

    public static MechanicalDriveState stronger(
            MechanicalDriveState left,
            MechanicalDriveState right,
            MechanicalRequirement requirement
    ) {
        MechanicalDriveState a = left == null ? idle() : left;
        MechanicalDriveState b = right == null ? idle() : right;
        if (requirement == null) {
            return stronger(a, b);
        }
        boolean aSatisfied = requirement.satisfied(a);
        boolean bSatisfied = requirement.satisfied(b);
        if (aSatisfied != bSatisfied) {
            return bSatisfied ? b : a;
        }
        return stronger(a, b);
    }
}
