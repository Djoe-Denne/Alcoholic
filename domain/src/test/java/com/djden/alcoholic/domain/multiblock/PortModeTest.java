package com.djden.alcoholic.domain.multiblock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PortModeTest {
    @Test
    void toggleIoLeavesBothOutOfTheCycle() {
        assertEquals(PortMode.INPUT, PortMode.BOTH.toggleIo());
        assertEquals(PortMode.OUTPUT, PortMode.INPUT.toggleIo());
        assertEquals(PortMode.INPUT, PortMode.OUTPUT.toggleIo());
    }
}
