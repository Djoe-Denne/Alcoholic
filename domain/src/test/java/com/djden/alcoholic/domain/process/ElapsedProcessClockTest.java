package com.djden.alcoholic.domain.process;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElapsedProcessClockTest {
    @Test
    void ignoresMissingOrNonIncreasingTimestamps() {
        assertEquals(0.0, ElapsedProcessClock.deltaTicks(0L, 1000L), 1e-9);
        assertEquals(0.0, ElapsedProcessClock.deltaTicks(2000L, 1000L), 1e-9);
    }

    @Test
    void capsCatchUpAtOneMinecraftDay() {
        assertEquals(100.0, ElapsedProcessClock.deltaTicks(10L, 110L), 1e-9);
        assertEquals(24_000.0, ElapsedProcessClock.deltaTicks(1L, 1L + 100_000L), 1e-9);
    }
}
