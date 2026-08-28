package com.djden.alcoholic.forge.client;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrimitiveCombustionEngineRendererTest {
    @Test
    void stoppedEngineKeepsItsCurrentAngle() {
        assertEquals(37.0F, PrimitiveCombustionEngineRenderer.advanceAngle(37.0F, 1.0, false));
    }

    @Test
    void runningEngineAdvancesEightDegreesPerTickAndClampsFrameGaps() {
        assertEquals(4.0F, PrimitiveCombustionEngineRenderer.advanceAngle(0.0F, 0.5, true));
        assertEquals(8.0F, PrimitiveCombustionEngineRenderer.advanceAngle(0.0F, 3.0, true));
    }

    @Test
    void facingAnglesMatchTheGeneratedBlockstate() {
        assertEquals(0.0F, PrimitiveCombustionEngineRenderer.facingDegrees(Direction.NORTH));
        assertEquals(90.0F, PrimitiveCombustionEngineRenderer.facingDegrees(Direction.EAST));
        assertEquals(180.0F, PrimitiveCombustionEngineRenderer.facingDegrees(Direction.SOUTH));
        assertEquals(270.0F, PrimitiveCombustionEngineRenderer.facingDegrees(Direction.WEST));
    }
}
