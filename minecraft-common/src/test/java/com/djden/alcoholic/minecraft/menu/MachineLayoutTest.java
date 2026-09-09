package com.djden.alcoholic.minecraft.menu;

import com.djden.alcoholic.domain.multiblock.MachineKind;
import com.djden.alcoholic.domain.multiblock.MachineScale;
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
        assertEquals(31, MachineContainerData.SIZE);

        assertEquals(12, MachineLayout.CRAFT_MALT.machineSlotCount());
        assertEquals(6, MachineLayout.CRAFT_MALT.inputSlotCount());
        assertEquals(6, MachineLayout.CRAFT_MALT.outputSlotCount());
        assertEquals(MachineLayout.PANEL_HEIGHT, MachineLayout.CRAFT_MALT.panelHeight());
        assertTrue(MachineLayout.CRAFT_MALT.progressArrow());

        assertEquals(36, MachineLayout.INDUSTRIAL_MALT.machineSlotCount());
        assertEquals(18, MachineLayout.INDUSTRIAL_MALT.inputSlotCount());
        assertEquals(18, MachineLayout.INDUSTRIAL_MALT.outputSlotCount());
        assertEquals(208, MachineLayout.INDUSTRIAL_MALT.panelHeight());
        assertEquals(126, MachineLayout.INDUSTRIAL_MALT.playerInvY());
    }

    @Test
    void multiblocksOnlyExposeTanksWhenTheirProcessUsesLiquids() {
        assertEquals(MachineLayout.INDUSTRIAL_MALT, MachineLayout.forMultiblock(MachineKind.MALT, true));
        assertEquals(
                MachineLayout.CRAFT_MALT,
                MachineLayout.forMultiblock(MachineKind.MALT, true, MachineScale.CRAFT)
        );
        assertEquals(
                MachineLayout.INDUSTRIAL_MALT,
                MachineLayout.forMultiblock(MachineKind.MALT, true, MachineScale.INDUSTRIAL)
        );
        assertEquals(MachineLayout.TWO_SLOTS, MachineLayout.forMultiblock(MachineKind.MILL, true));
        assertEquals(
                MachineLayout.TWO_SLOTS,
                MachineLayout.forMultiblock(MachineKind.MILL, true, MachineScale.CRAFT)
        );
        assertEquals(MachineLayout.TWO_SLOTS_ONE_TANK, MachineLayout.forMultiblock(MachineKind.PRESS, true));
        assertEquals(MachineLayout.TWO_SLOTS_ONE_TANK, MachineLayout.forMultiblock(MachineKind.MASH, true));
        assertEquals(
                MachineLayout.TWO_SLOTS_ONE_TANK,
                MachineLayout.forMultiblock(MachineKind.MASH, true, MachineScale.CRAFT)
        );
        assertEquals(1, MachineLayout.forMultiblock(MachineKind.MASH, true, MachineScale.CRAFT).tankCount());
        assertEquals(2, MachineLayout.TWO_SLOTS_TWO_TANKS.tankCount());
        assertEquals(MachineLayout.ONE_TANK, MachineLayout.forMultiblock(MachineKind.STORAGE, false));
    }

    @Test
    void processStagesRoundTripForControllerTelemetry() {
        for (MachineProcessStage stage : MachineProcessStage.values()) {
            assertEquals(stage, MachineProcessStage.decode(MachineProcessStage.encode(stage.name())));
        }
        assertEquals(MachineProcessStage.IDLE, MachineProcessStage.decode(-1));
        assertEquals(MachineProcessStage.IDLE.ordinal(), MachineProcessStage.encode("unknown"));
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

    @Test
    void industrialMaltPlayerInventoryFitsTheTallerPanel() {
        MachineLayout layout = MachineLayout.INDUSTRIAL_MALT;
        assertEquals(36, layout.playerSlots().length);
        boolean[][] occupied = new boolean[layout.panelWidth()][layout.panelHeight()];
        for (MachineLayout.SlotPos slot : layout.playerSlots()) {
            int wellX = slot.x() - 1;
            int wellY = slot.y() - 1;
            assertTrue(wellX >= 0 && wellY >= 0, slot.toString());
            assertTrue(wellX + MachineLayout.SLOT_SIZE <= layout.panelWidth(), slot.toString());
            assertTrue(wellY + MachineLayout.SLOT_SIZE <= layout.panelHeight(), slot.toString());
            for (int x = wellX; x < wellX + MachineLayout.SLOT_SIZE; x++) {
                for (int y = wellY; y < wellY + MachineLayout.SLOT_SIZE; y++) {
                    assertFalse(occupied[x][y], "overlapping well at " + x + "," + y);
                    occupied[x][y] = true;
                }
            }
        }
        for (MachineLayout.SlotPos slot : layout.slots()) {
            int wellX = slot.x() - 1;
            int wellY = slot.y() - 1;
            assertTrue(wellY + MachineLayout.SLOT_SIZE <= layout.playerInvY(), slot.toString());
            for (int x = wellX; x < wellX + MachineLayout.SLOT_SIZE; x++) {
                for (int y = wellY; y < wellY + MachineLayout.SLOT_SIZE; y++) {
                    assertFalse(occupied[x][y], "machine well overlaps player at " + x + "," + y);
                }
            }
        }
    }
}
