package com.djden.alcoholic.minecraft.mechanical;

import com.djden.alcoholic.domain.mechanical.MechanicalDriveState;
import com.djden.alcoholic.domain.mechanical.MechanicalRequirement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElectricMotorSettingsTest {
    @Test
    void idleLoadConsumesNoEnergy() {
        assertEquals(0, ElectricMotorSettings.DEFAULT.feForLoad(0.0));
    }

    @Test
    void millLoadConsumesProportionalFe() {
        ElectricMotorSettings settings = ElectricMotorSettings.DEFAULT;
        int millCost = settings.feForLoad(MechanicalRequirement.maltMill().requiredCapacity());
        assertEquals(25, millCost);
        assertTrue(millCost < settings.maxReceivePerTick());
    }

    @Test
    void defaultOutputSatisfiesTheMaltMillWhenBufferHasEnergy() {
        ElectricMotorSettings settings = ElectricMotorSettings.DEFAULT;
        double capacity = settings.capacityFromEnergy(settings.feForLoad(1.0));
        MechanicalDriveState state = MechanicalDriveState.running(settings.outputSpeed(), capacity);
        assertTrue(MechanicalRequirement.maltMill().satisfied(state));
        assertFalse(MechanicalRequirement.maltMill().satisfied(
                MechanicalDriveState.running(settings.outputSpeed(), settings.capacityFromEnergy(0))
        ));
    }
}
