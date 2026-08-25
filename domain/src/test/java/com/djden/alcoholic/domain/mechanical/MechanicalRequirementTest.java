package com.djden.alcoholic.domain.mechanical;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MechanicalRequirementTest {
    @Test
    void noneIsAlwaysSatisfied() {
        assertTrue(MechanicalRequirement.none().satisfied(MechanicalDriveState.idle()));
        assertTrue(MechanicalRequirement.none().satisfied(null));
    }

    @Test
    void maltMillAcceptsPrimitiveEngineOutput() {
        MechanicalDriveState engine = MechanicalDriveState.running(16.0, 4.0);
        assertTrue(MechanicalRequirement.maltMill().satisfied(engine));
        assertTrue(MechanicalRequirement.industrialPress().satisfied(engine));
    }

    @Test
    void rejectsIdleStalledOrUnderpoweredDrive() {
        MechanicalRequirement mill = MechanicalRequirement.maltMill();
        assertFalse(mill.satisfied(MechanicalDriveState.idle()));
        assertFalse(mill.satisfied(MechanicalDriveState.stalled(16.0, 4.0)));
        assertFalse(mill.satisfied(MechanicalDriveState.running(4.0, 4.0)));
        assertFalse(mill.satisfied(MechanicalDriveState.running(16.0, 0.0)));
    }

    @Test
    void strongerPrefersAUsableDrive() {
        MechanicalDriveState idle = MechanicalDriveState.idle();
        MechanicalDriveState running = MechanicalDriveState.running(16.0, 4.0);
        assertTrue(MechanicalDriveState.stronger(idle, running).usable());
        assertTrue(MechanicalDriveState.stronger(running, idle).usable());
    }
}
