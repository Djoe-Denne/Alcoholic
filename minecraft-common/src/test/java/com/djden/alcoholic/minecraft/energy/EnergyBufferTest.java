package com.djden.alcoholic.minecraft.energy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnergyBufferTest {
    @Test
    void receiveAndExtractRespectLimits() {
        EnergyBuffer buffer = new EnergyBuffer(100, 20, 15);
        assertEquals(20, buffer.receive(50, false));
        assertEquals(20, buffer.stored());
        assertEquals(20, buffer.receive(80, true));
        assertEquals(20, buffer.stored());
        assertEquals(15, buffer.extract(40, false));
        assertEquals(5, buffer.stored());
    }

    @Test
    void simulateDoesNotMutate() {
        EnergyBuffer buffer = new EnergyBuffer(50, 50, 50);
        assertEquals(40, buffer.receive(40, true));
        assertTrue(buffer.isEmpty());
        buffer.receive(40, false);
        assertEquals(10, buffer.extract(10, true));
        assertEquals(40, buffer.stored());
    }
}
