package com.djden.alcoholic.minecraft.agriculture;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClimbingColumnTest {
    @Test
    void columnIsThreeBlocksFromRootToWire() {
        assertEquals(2, ClimbingColumn.MAX_WIRE_OFFSET);
        assertEquals(ClimbingColumn.MAX_WIRE_OFFSET, VineColumn.MAX_WIRE_OFFSET);
        assertEquals(ClimbingColumn.MAX_WIRE_OFFSET, HopColumn.MAX_WIRE_OFFSET);
    }
}
