package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.domain.mechanical.MechanicalDrivePort;
import com.djden.alcoholic.domain.mechanical.MechanicalDriveState;

/**
 * Compatibility view over {@link MechanicalDrivePort} for industrial ports
 * that historically exposed a stored speed. New machines should implement
 * {@link MechanicalDrivePort} directly.
 */
public interface KineticSource extends MechanicalDrivePort {
    double rpm();

    void setRpm(double rpm);

    @Override
    default MechanicalDriveState driveState() {
        double speed = rpm();
        return speed > 0.0
                ? MechanicalDriveState.running(speed, Double.POSITIVE_INFINITY)
                : MechanicalDriveState.idle();
    }

    @Override
    default boolean isSource() {
        return false;
    }
}
