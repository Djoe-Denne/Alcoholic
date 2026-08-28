package com.djden.alcoholic.minecraft.debug;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortAuditPlacerTest {
    @Test
    void fluidBrokenAllFaceNorth() {
        assertTrue(PortAuditPlacer.fluidBroken().stream().allMatch(slot -> slot.facing() == Direction.NORTH));
        assertEquals(6, PortAuditPlacer.fluidBroken().size());
    }

    @Test
    void fluidAlignedTunKettleShareAnEastWestFace() {
        PortAuditPlacer.Slot tun = PortAuditPlacer.fluidAligned().get(0);
        PortAuditPlacer.Slot kettle = PortAuditPlacer.fluidAligned().get(1);
        assertEquals("mash_tun", tun.id());
        assertEquals("brewing_kettle", kettle.id());
        assertEquals(1, kettle.dx() - tun.dx());
        assertEquals(0, kettle.dz() - tun.dz());
        assertEquals(Direction.NORTH, tun.facing());
        assertEquals(Direction.WEST, kettle.facing());
    }

    @Test
    void energyAlignedMotorFacesMillAxle() {
        PortAuditPlacer.Slot mill = PortAuditPlacer.energyAligned().get(0);
        PortAuditPlacer.Slot motor = PortAuditPlacer.energyAligned().get(1);
        assertEquals(1, motor.dx() - mill.dx());
        assertEquals(Direction.NORTH, mill.facing());
        assertEquals(Direction.WEST, motor.facing());
    }

    @Test
    void energyBrokenMillSitsOnEngineGrate() {
        PortAuditPlacer.Slot engine = PortAuditPlacer.energyBroken().get(2);
        PortAuditPlacer.Slot mill = PortAuditPlacer.energyBroken().get(3);
        assertEquals("engine", engine.id());
        assertEquals("malt_mill", mill.id());
        assertEquals(engine.dx(), mill.dx());
        assertEquals(-1, mill.dz() - engine.dz());
        assertEquals(Direction.NORTH, engine.facing());
    }

    @Test
    void energyAlignedMillSitsOnEngineShaft() {
        PortAuditPlacer.Slot engine = PortAuditPlacer.energyAligned().get(2);
        PortAuditPlacer.Slot mill = PortAuditPlacer.energyAligned().get(3);
        assertEquals("engine", engine.id());
        assertEquals("malt_mill", mill.id());
        assertEquals(1, mill.dx() - engine.dx());
        assertEquals(0, mill.dz() - engine.dz());
        assertEquals(Direction.NORTH, engine.facing());
    }
}
