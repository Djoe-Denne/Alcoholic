package com.djden.alcoholic.integration.crossroads;

import com.djden.alcoholic.domain.mechanical.MechanicalDirection;
import com.djden.alcoholic.domain.mechanical.MechanicalDriveState;
import com.djden.alcoholic.domain.mechanical.MechanicalRequirement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrossroadsRotaryMappingTest {
    @Test
    void convertsRadiansPerSecondToRpm() {
        double twoPi = 2.0 * Math.PI;
        assertEquals(60.0, CrossroadsRotaryMapping.rpmFromRadPerSecond(twoPi), 1e-9);
        assertEquals(16.0, CrossroadsRotaryMapping.rpmFromRadPerSecond(16.0 * twoPi / 60.0), 1e-9);
    }

    @Test
    void mapsEnergyOntoAlcoholicCapacityAndConsumesJoulesForWork() {
        assertEquals(2.0, CrossroadsRotaryMapping.capacityFromEnergy(40.0), 1e-9);
        assertEquals(20.0, CrossroadsRotaryMapping.joulesForLoad(1.0), 1e-9);
        assertEquals(0.0, CrossroadsRotaryMapping.capacityFromEnergy(0.0), 1e-9);
    }

    @Test
    void spinningWithEnergySatisfiesTheMaltMill() {
        double radPerSecond = 16.0 * 2.0 * Math.PI / 60.0;
        MechanicalDriveState state = CrossroadsRotaryMapping.driveState(radPerSecond, 40.0);
        assertTrue(MechanicalRequirement.maltMill().satisfied(state));
        assertEquals(MechanicalDirection.CLOCKWISE, state.direction());
    }

    @Test
    void spinningWithoutEnergyDoesNotSatisfyTheMaltMill() {
        double radPerSecond = 16.0 * 2.0 * Math.PI / 60.0;
        MechanicalDriveState coasting = CrossroadsRotaryMapping.driveState(radPerSecond, 0.0);
        assertTrue(coasting.stalled());
        assertFalse(MechanicalRequirement.maltMill().satisfied(coasting));
    }

    @Test
    void oppositeSpinMapsToCounterClockwise() {
        assertEquals(
                MechanicalDirection.COUNTER_CLOCKWISE,
                CrossroadsRotaryMapping.directionFrom(-1.0)
        );
    }
}
