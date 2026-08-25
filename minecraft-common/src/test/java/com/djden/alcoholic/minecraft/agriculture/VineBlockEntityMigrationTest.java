package com.djden.alcoholic.minecraft.agriculture;

import com.djden.alcoholic.application.viticulture.VineVarieties;
import com.djden.alcoholic.domain.viticulture.Vine;
import com.djden.alcoholic.domain.viticulture.VineGrowthStage;
import com.djden.alcoholic.api.ResourceId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VineBlockEntityMigrationTest {
    @Test
    void mapsAllLegacyAgesWithoutBreakingDomainInvariants() {
        assertEquals(
                VineGrowthStage.PLANTED,
                migrate(0).growthStage()
        );
        assertEquals(
                VineGrowthStage.ESTABLISHING,
                migrate(1).growthStage()
        );
        assertEquals(
                VineGrowthStage.VEGETATIVE,
                migrate(2).growthStage()
        );
        assertEquals(
                VineGrowthStage.RIPENING,
                migrate(3).growthStage()
        );

        Vine<ResourceId> mature = migrate(4);
        assertEquals(VineGrowthStage.HARVEST_READY, mature.growthStage());
        assertTrue(mature.hasEstablished());
        assertEquals(0, mature.ageCycles());
        assertEquals(Vine.NO_HARVEST, mature.lastHarvest());
        assertFalse(migrate(3).hasEstablished());
    }

    private static Vine<ResourceId> migrate(int age) {
        return VineBlockEntity.migrateLegacy(VineVarieties.RED_GRAPE, age);
    }
}
