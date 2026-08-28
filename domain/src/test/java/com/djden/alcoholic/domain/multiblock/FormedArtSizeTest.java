package com.djden.alcoholic.domain.multiblock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormedArtSizeTest {
    @Test
    void overlayIsANoOpWhenSizeIsNotTheArtSize() {
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.MALT_HOUSE_ID, 3, 4, 3).isEmpty());
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.PRESS_ID, 7, 8, 7).isEmpty());
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.STORAGE_TANK_ID, 9, 16, 9).isEmpty());
    }

    @Test
    void overlayMatchesDebugPlaceArtSizes() {
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.MALT_HOUSE_ID, 5, 4, 5).isPresent());
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.ROLLER_MILL_ID, 3, 4, 3).isPresent());
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.MASH_TUN_ID, 5, 5, 5).isPresent());
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.BREWING_KETTLE_ID, 5, 6, 5).isPresent());
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.FERMENTATION_VAT_ID, 3, 5, 3).isPresent());
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.CONDITIONING_VESSEL_ID, 3, 6, 3).isPresent());
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.STORAGE_TANK_ID, 3, 5, 3).isPresent());
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.PRESS_ID, 3, 4, 3).isPresent());
    }

    @Test
    void catalogCoversEveryIndustrialMachine() {
        assertEquals(8, FormedArtSize.all().size());
    }
}
