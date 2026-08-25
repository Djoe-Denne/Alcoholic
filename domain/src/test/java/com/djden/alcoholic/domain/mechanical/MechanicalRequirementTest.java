package com.djden.alcoholic.domain.mechanical;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
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
    void maltMillAcceptsElectricMotorOutput() {
        MechanicalDriveState motor = MechanicalDriveState.running(32.0, 8.0);
        assertTrue(MechanicalRequirement.maltMill().satisfied(motor));
        assertTrue(MechanicalRequirement.industrialPress().satisfied(motor));
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
    void industrialRollerMillAcceptsPrimitiveEngineAndElectricMotor() {
        MechanicalRequirement mill = MechanicalRequirement.industrialRollerMill();
        assertTrue(mill.satisfied(MechanicalDriveState.running(16.0, 4.0)));
        assertTrue(mill.satisfied(MechanicalDriveState.running(32.0, 8.0)));
        assertTrue(mill.satisfied(MechanicalDriveState.running(16.0, Double.POSITIVE_INFINITY)));
        assertFalse(mill.satisfied(MechanicalDriveState.idle()));
        assertFalse(mill.satisfied(MechanicalDriveState.running(16.0, 1.0)));
        assertFalse(mill.satisfied(MechanicalDriveState.running(8.0, 4.0)));
        assertTrue(MechanicalRequirement.maltMill().satisfied(MechanicalDriveState.running(8.0, 1.0)));
        assertFalse(mill.satisfied(MechanicalDriveState.running(8.0, 1.0)));
    }

    @Test
    void strongerPrefersAUsableDrive() {
        MechanicalDriveState idle = MechanicalDriveState.idle();
        MechanicalDriveState running = MechanicalDriveState.running(16.0, 4.0);
        assertTrue(MechanicalDriveState.stronger(idle, running).usable());
        assertTrue(MechanicalDriveState.stronger(running, idle).usable());
    }

    @Test
    void requirementAwareSelectionPrefersTheDriveThatCanActuallyRunTheMachine() {
        MechanicalRequirement mill = MechanicalRequirement.industrialRollerMill();
        MechanicalDriveState fastButWeak = MechanicalDriveState.running(32.0, 3.0);
        MechanicalDriveState slowerButSufficient = MechanicalDriveState.running(16.0, 4.0);

        assertSame(
                slowerButSufficient,
                MechanicalDriveState.stronger(fastButWeak, slowerButSufficient, mill)
        );
        assertSame(
                slowerButSufficient,
                MechanicalDriveState.stronger(slowerButSufficient, fastButWeak, mill)
        );
    }
}
