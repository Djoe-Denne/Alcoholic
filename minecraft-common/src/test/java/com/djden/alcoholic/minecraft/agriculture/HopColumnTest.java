package com.djden.alcoholic.minecraft.agriculture;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HopColumnTest {
    @Test
    void onlyGrowingBinesCanExtend() {
        assertFalse(HopColumn.canExtend(0));
        assertTrue(HopColumn.canExtend(1));
        assertTrue(HopColumn.canExtend(2));
    }

    @Test
    void canopyOccupiesTheWireOnlyWhenTheBineReachesIt() {
        assertTrue(HopColumn.shouldOccupyWire(1, 0));
        assertTrue(HopColumn.shouldOccupyWire(1, 2));
        assertFalse(HopColumn.shouldOccupyWire(2, 0));
        assertTrue(HopColumn.shouldOccupyWire(2, 1));
        assertTrue(HopColumn.shouldOccupyWire(2, 2));
        assertFalse(HopColumn.shouldOccupyWire(0, 2));
        assertFalse(HopColumn.shouldOccupyWire(3, 2));
    }

    @Test
    void hopColumnHeightIsTheSharedClimbingColumnHeight() {
        assertEquals(ClimbingColumn.MAX_WIRE_OFFSET, HopColumn.MAX_WIRE_OFFSET);
        assertEquals(2, HopColumn.MAX_WIRE_OFFSET);
    }
}
