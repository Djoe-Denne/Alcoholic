package com.djden.alcoholic.minecraft.agriculture;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HopBineBlockTest {
    @Test
    void columnSegmentsTrackPlantNeighbors() {
        assertEquals(HopBineBlock.Segment.SINGLE, HopBineBlock.Segment.fromNeighbors(false, false));
        assertEquals(HopBineBlock.Segment.BOTTOM, HopBineBlock.Segment.fromNeighbors(false, true));
        assertEquals(HopBineBlock.Segment.TOP, HopBineBlock.Segment.fromNeighbors(true, false));
        assertEquals(HopBineBlock.Segment.MIDDLE, HopBineBlock.Segment.fromNeighbors(true, true));
    }
}
