package com.djden.alcoholic.minecraft.menu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineLayoutTest {
    @Test
    void slotAndTankCountsMatchWidgetArrays() {
        for (MachineLayout layout : MachineLayout.values()) {
            assertEquals(layout.machineSlotCount(), layout.slots().length, layout.name());
            if (layout.energyGauge()) {
                assertEquals(0, layout.tankCount(), layout.name());
                assertEquals(1, layout.gauges().length, layout.name());
            } else {
                assertEquals(layout.tankCount(), layout.gauges().length, layout.name());
            }
        }
    }

    @Test
    void plannedFamiliesKeepExpectedSizes() {
        assertEquals(2, MachineLayout.TWO_SLOTS.machineSlotCount());
        assertEquals(0, MachineLayout.TWO_SLOTS.tankCount());
        assertTrue(MachineLayout.TWO_SLOTS.progressArrow());

        assertEquals(2, MachineLayout.TWO_SLOTS_ONE_TANK.machineSlotCount());
        assertEquals(1, MachineLayout.TWO_SLOTS_ONE_TANK.tankCount());

        assertEquals(2, MachineLayout.TWO_SLOTS_TWO_TANKS.machineSlotCount());
        assertEquals(2, MachineLayout.TWO_SLOTS_TWO_TANKS.tankCount());

        assertEquals(1, MachineLayout.ONE_SLOT_ONE_TANK.machineSlotCount());
        assertEquals(1, MachineLayout.ONE_SLOT_ONE_TANK.tankCount());

        assertEquals(0, MachineLayout.ONE_TANK.machineSlotCount());
        assertEquals(1, MachineLayout.ONE_TANK.tankCount());
        assertFalse(MachineLayout.ONE_TANK.progressArrow());

        assertEquals(0, MachineLayout.TWO_TANKS.machineSlotCount());
        assertEquals(2, MachineLayout.TWO_TANKS.tankCount());

        assertEquals(1, MachineLayout.FUEL.machineSlotCount());
        assertTrue(MachineLayout.FUEL.fuelBar());

        assertEquals(0, MachineLayout.ENERGY.machineSlotCount());
        assertTrue(MachineLayout.ENERGY.energyGauge());
        assertEquals(12, MachineContainerData.SIZE);
    }
}
