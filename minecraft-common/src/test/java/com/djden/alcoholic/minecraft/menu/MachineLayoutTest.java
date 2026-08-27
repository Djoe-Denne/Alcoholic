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
        assertEquals(15, MachineContainerData.SIZE);
    }

    @Test
    void playerInventoryIsVanillaNineByFourGridInsideThePanel() {
        assertEquals(36, MachineLayout.PLAYER_SLOTS.length);
        assertEquals(new MachineLayout.SlotPos(8, 84), MachineLayout.PLAYER_SLOTS[0]);
        assertEquals(new MachineLayout.SlotPos(152, 120), MachineLayout.PLAYER_SLOTS[26]);
        assertEquals(new MachineLayout.SlotPos(8, 142), MachineLayout.PLAYER_SLOTS[27]);
        assertEquals(new MachineLayout.SlotPos(152, 142), MachineLayout.PLAYER_SLOTS[35]);
        assertEquals(4, MachineLayout.HOTBAR_Y - (MachineLayout.PLAYER_INV_Y + 3 * MachineLayout.SLOT_SIZE));

        boolean[][] occupied = new boolean[MachineLayout.PANEL_WIDTH][MachineLayout.PANEL_HEIGHT];
        for (MachineLayout.SlotPos slot : MachineLayout.PLAYER_SLOTS) {
            int wellX = slot.x() - 1;
            int wellY = slot.y() - 1;
            assertTrue(wellX >= 0 && wellY >= 0, slot.toString());
            assertTrue(wellX + MachineLayout.SLOT_SIZE <= MachineLayout.PANEL_WIDTH, slot.toString());
            assertTrue(wellY + MachineLayout.SLOT_SIZE <= MachineLayout.PANEL_HEIGHT, slot.toString());
            for (int x = wellX; x < wellX + MachineLayout.SLOT_SIZE; x++) {
                for (int y = wellY; y < wellY + MachineLayout.SLOT_SIZE; y++) {
                    assertFalse(occupied[x][y], "overlapping well at " + x + "," + y);
                    occupied[x][y] = true;
                }
            }
        }
    }
}
