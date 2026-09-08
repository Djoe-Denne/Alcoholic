package com.djden.alcoholic.forge.client;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineTelemetryOverlayTest {
    @Test
    void extraAreaSitsLeftOfTheInventoryWithFixedWidth() {
        Optional<MachineTelemetryOverlay.Area> area = MachineTelemetryOverlay.extraArea(true, 100, 20, 166);
        assertTrue(area.isPresent());
        MachineTelemetryOverlay.Area overlay = area.orElseThrow();
        assertEquals(100 - MachineTelemetryOverlay.WIDTH - MachineTelemetryOverlay.GAP, overlay.x());
        assertTrue(overlay.x() + overlay.width() <= 100);
        assertEquals(20, overlay.y());
        assertEquals(152, overlay.width());
        assertEquals(166, overlay.height());
    }

    @Test
    void extraAreaIsAbsentWithoutTelemetry() {
        assertTrue(MachineTelemetryOverlay.extraArea(false, 100, 20, 166).isEmpty());
    }

    @Test
    void extraAreaContainsClicksOnThePanel() {
        MachineTelemetryOverlay.Area overlay = MachineTelemetryOverlay.extraArea(true, 100, 20, 166).orElseThrow();
        assertTrue(overlay.contains(overlay.x() + 1, overlay.y() + 1));
        assertFalse(overlay.contains(100, 20));
        assertFalse(overlay.contains(overlay.x() - 1, overlay.y()));
    }
}
