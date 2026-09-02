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
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.AGING_VESSEL_ID, 3, 6, 3).isPresent());
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.STORAGE_TANK_ID, 3, 5, 3).isPresent());
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.PRESS_ID, 3, 4, 3).isPresent());
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.CRAFT_MALT_HOUSE_ID, 3, 3, 3).isPresent());
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.CRAFT_MILL_ID, 3, 3, 3).isPresent());
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.CRAFT_MASH_TUN_ID, 3, 3, 3).isPresent());
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.CRAFT_BREWING_KETTLE_ID, 3, 3, 3).isPresent());
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.CRAFT_VAT_ID, 3, 3, 3).isPresent());
    }

    @Test
    void craftMaltHouseKeepsNineSliceWhenTheCubeGrows() {
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.CRAFT_MALT_HOUSE_ID, 4, 4, 4).isEmpty());
        assertTrue(FormedArtSize.overlayMesh(FormedArtSize.CRAFT_MALT_HOUSE_ID, 5, 5, 5).isEmpty());
    }

    @Test
    void catalogCoversEveryArtMachine() {
        assertEquals(14, FormedArtSize.all().size());
    }
}
