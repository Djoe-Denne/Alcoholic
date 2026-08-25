package com.djden.alcoholic.domain.multiblock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CapacityCalculatorTest {
    @Test
    void scalesWithInteriorVolumeNotShell() {
        assertEquals(16_000, CapacityCalculator.millibuckets(2, 8_000));
        assertEquals(216_000, CapacityCalculator.millibuckets(27, 8_000));
        assertEquals(4_000, CapacityCalculator.millibuckets(2, 2_000));
        assertEquals(0, CapacityCalculator.millibuckets(-1, 8_000));
        assertEquals(0, CapacityCalculator.millibuckets(2, 0));
    }
}
