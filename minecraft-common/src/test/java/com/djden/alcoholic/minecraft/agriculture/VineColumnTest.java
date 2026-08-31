package com.djden.alcoholic.minecraft.agriculture;

import com.djden.alcoholic.domain.viticulture.VineGrowthStage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VineColumnTest {
    @Test
    void onlyPostEstablishmentStagesCanExtend() {
        assertFalse(VineColumn.canExtend(VineGrowthStage.PLANTED));
        assertFalse(VineColumn.canExtend(VineGrowthStage.ESTABLISHING));
        assertTrue(VineColumn.canExtend(VineGrowthStage.VEGETATIVE));
        assertTrue(VineColumn.canExtend(VineGrowthStage.FLOWERING));
        assertTrue(VineColumn.canExtend(VineGrowthStage.GREEN_FRUIT));
        assertTrue(VineColumn.canExtend(VineGrowthStage.RIPENING));
        assertTrue(VineColumn.canExtend(VineGrowthStage.HARVEST_READY));
        assertTrue(VineColumn.canExtend(VineGrowthStage.DORMANT));
    }

    @Test
    void canopyOccupiesTheWireOnlyWhenThePlantReachesIt() {
        assertTrue(VineColumn.shouldOccupyWire(1, VineGrowthStage.PLANTED));
        assertTrue(VineColumn.shouldOccupyWire(1, VineGrowthStage.VEGETATIVE));
        assertFalse(VineColumn.shouldOccupyWire(2, VineGrowthStage.PLANTED));
        assertFalse(VineColumn.shouldOccupyWire(2, VineGrowthStage.ESTABLISHING));
        assertTrue(VineColumn.shouldOccupyWire(2, VineGrowthStage.VEGETATIVE));
        assertTrue(VineColumn.shouldOccupyWire(2, VineGrowthStage.HARVEST_READY));
        assertFalse(VineColumn.shouldOccupyWire(0, VineGrowthStage.VEGETATIVE));
        assertFalse(VineColumn.shouldOccupyWire(3, VineGrowthStage.VEGETATIVE));
    }

    @Test
    void vineColumnHeightIsTheSharedClimbingColumnHeight() {
        assertEquals(ClimbingColumn.MAX_WIRE_OFFSET, VineColumn.MAX_WIRE_OFFSET);
        assertEquals(2, ClimbingColumn.MAX_WIRE_OFFSET);
    }
}
