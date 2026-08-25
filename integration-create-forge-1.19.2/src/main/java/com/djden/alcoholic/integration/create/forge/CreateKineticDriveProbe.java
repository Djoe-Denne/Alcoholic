package com.djden.alcoholic.integration.create.forge;

import com.djden.alcoholic.domain.mechanical.MechanicalDirection;
import com.djden.alcoholic.domain.mechanical.MechanicalDrivePort;
import com.djden.alcoholic.domain.mechanical.MechanicalDriveState;
import com.djden.alcoholic.minecraft.mechanical.MechanicalDrives;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import java.util.Optional;

/**
 * Translates an adjacent Create kinetic block into Alcoholic drive units.
 * Machines keep depending on {@link MechanicalDrives}, not Create types.
 */
public final class CreateKineticDriveProbe {
    private CreateKineticDriveProbe() {
    }

    public static void install() {
        MechanicalDrives.register((level, drivePos, state) -> {
            if (!(level.getBlockEntity(drivePos) instanceof KineticBlockEntity kinetic)) {
                return Optional.empty();
            }
            if (kinetic instanceof MechanicalDrivePort) {
                return Optional.empty();
            }
            double speed = Math.abs(kinetic.getSpeed());
            boolean stalled = kinetic.isOverStressed();
            if (speed <= 0.0 && !stalled) {
                return Optional.empty();
            }
            return Optional.of(new MechanicalDriveState(
                    speed,
                    stalled ? 0.0 : 64.0,
                    MechanicalDirection.NONE,
                    speed > 0.0 && !stalled,
                    stalled
            ));
        });
    }
}
