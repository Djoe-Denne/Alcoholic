package com.djden.alcoholic.minecraft.multiblock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MaltBatchInventoryTest {
    @Test
    void fillMatchingMergesThenUsesEmptySlotsAtVanillaLimit() {
        int[] slots = {32, -1, 0, 0};
        assertEquals(0, MaltBatchInventory.fillMatching(slots, 64, 128));
        assertArrayEquals(new int[]{64, -1, 64, 32}, slots);
    }

    @Test
    void fillMatchingBackpressuresWhenSpaceIsShort() {
        int[] slots = {64, -1};
        assertEquals(10, MaltBatchInventory.fillMatching(slots, 64, 10));
        assertArrayEquals(new int[]{64, -1}, slots);
    }

    @Test
    void takeMatchingSkipsIncompatibleSlots() {
        int[] slots = {64, -1, 32};
        assertEquals(0, MaltBatchInventory.takeMatching(slots, 70));
        assertArrayEquals(new int[]{0, -1, 26}, slots);
    }

    @Test
    void spaceCountsEmptyAndPartialMatchingSlots() {
        assertEquals(96, MaltBatchInventory.space(new int[]{32, -1, 0}, 64));
    }
}
