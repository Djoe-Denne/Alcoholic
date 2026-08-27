package com.djden.alcoholic.forge.client;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElectricMotorRendererTest {
    @Test
    void stoppedMotorKeepsItsCurrentAngle() {
        assertEquals(37.0F, ElectricMotorRenderer.advanceAngle(37.0F, 1.0, false));
    }

    @Test
    void runningMotorAdvancesEightDegreesPerTickAndClampsFrameGaps() {
        assertEquals(4.0F, ElectricMotorRenderer.advanceAngle(0.0F, 0.5, true));
        assertEquals(8.0F, ElectricMotorRenderer.advanceAngle(0.0F, 3.0, true));
    }

    @Test
    void facingAnglesMatchTheGeneratedBlockstate() {
        assertEquals(0.0F, ElectricMotorRenderer.facingDegrees(Direction.NORTH));
        assertEquals(90.0F, ElectricMotorRenderer.facingDegrees(Direction.EAST));
        assertEquals(180.0F, ElectricMotorRenderer.facingDegrees(Direction.SOUTH));
        assertEquals(270.0F, ElectricMotorRenderer.facingDegrees(Direction.WEST));
    }
}
