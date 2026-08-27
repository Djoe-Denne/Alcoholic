package com.djden.alcoholic.forge.client;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaltMillRendererTest {
    @Test
    void stoppedMillKeepsItsCurrentAngle() {
        assertEquals(37.0F, MaltMillRenderer.advanceAngle(37.0F, 1.0, false));
    }

    @Test
    void runningMillAdvancesSixDegreesPerTickAndClampsFrameGaps() {
        assertEquals(3.0F, MaltMillRenderer.advanceAngle(0.0F, 0.5, true));
        assertEquals(6.0F, MaltMillRenderer.advanceAngle(0.0F, 3.0, true));
    }

    @Test
    void rearRollerCounterRotatesWhileAxleFollowsFrontRoller() {
        float angle = 24.0F;
        assertEquals(24.0F, MaltMillRenderer.frontRotation(angle));
        assertEquals(-24.0F, MaltMillRenderer.rearRotation(angle));
        assertEquals(24.0F, MaltMillRenderer.axleRotation(angle));
    }

    @Test
    void facingAnglesMatchTheGeneratedBlockstate() {
        assertEquals(0.0F, MaltMillRenderer.facingDegrees(Direction.NORTH));
        assertEquals(90.0F, MaltMillRenderer.facingDegrees(Direction.EAST));
        assertEquals(180.0F, MaltMillRenderer.facingDegrees(Direction.SOUTH));
        assertEquals(270.0F, MaltMillRenderer.facingDegrees(Direction.WEST));
    }
}
