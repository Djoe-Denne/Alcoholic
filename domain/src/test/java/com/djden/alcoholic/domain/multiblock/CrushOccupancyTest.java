package com.djden.alcoholic.domain.multiblock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrushOccupancyTest {
    private static final Box3 CRUSH = new Box3(1.25, 1.25, 1.25, 1.75, 2.75, 1.75);

    @Test
    void centerInsideDuringCompressionIsLethal() {
        Box3 body = new Box3(1.15, 1.20, 1.15, 1.85, 2.90, 1.85);
        assertTrue(CrushOccupancy.lethal(
                PressStrokeState.COMPRESSING,
                CRUSH,
                1.50,
                2.00,
                1.50,
                body
        ));
    }

    @Test
    void edgeContactIsNotLethal() {
        Box3 body = new Box3(1.70, 1.20, 1.20, 2.40, 3.00, 1.90);
        assertFalse(CrushOccupancy.lethal(
                PressStrokeState.COMPRESSING,
                CRUSH,
                2.05,
                2.10,
                1.55,
                body
        ));
    }

    @Test
    void idleMachineIsNotLethal() {
        Box3 body = new Box3(1.15, 1.20, 1.15, 1.85, 2.90, 1.85);
        assertFalse(CrushOccupancy.lethal(
                PressStrokeState.IDLE,
                CRUSH,
                1.50,
                2.00,
                1.50,
                body
        ));
    }

    @Test
    void nonCompressingStrokeIsNotLethal() {
        Box3 body = new Box3(1.15, 1.20, 1.15, 1.85, 2.90, 1.85);
        assertFalse(CrushOccupancy.lethal(
                PressStrokeState.RETRACTING,
                CRUSH,
                1.50,
                2.00,
                1.50,
                body
        ));
        assertFalse(CrushOccupancy.lethal(
                PressStrokeState.LOADING,
                CRUSH,
                1.50,
                2.00,
                1.50,
                body
        ));
        assertFalse(CrushOccupancy.lethal(
                PressStrokeState.HOLDING,
                CRUSH,
                1.50,
                2.00,
                1.50,
                body
        ));
    }
}
