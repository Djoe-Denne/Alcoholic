package com.djden.alcoholic.integration.vinery;

import com.djden.alcoholic.application.viticulture.VineVarieties;
import com.djden.alcoholic.api.ResourceId;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VineryGrapeProviderTest {
    @Test
    void exposesExactVinery1192Ids() {
        assertEquals(
                ResourceId.parse("vinery:red_grape_seeds"),
                VineryIntegration.RED_GRAPE_SEEDS
        );
        assertEquals(
                ResourceId.parse("vinery:white_grape_seeds"),
                VineryIntegration.WHITE_GRAPE_SEEDS
        );
        assertEquals(ResourceId.parse("vinery:red_grape"), VineryIntegration.RED_GRAPE);
        assertEquals(ResourceId.parse("vinery:white_grape"), VineryIntegration.WHITE_GRAPE);
    }

    @Test
    void mapsBothVarietiesToTheirPlantingMaterialAndHarvestItem() {
        VineryGrapeProvider provider = new VineryGrapeProvider(id -> true);

        assertEquals(
                VineryIntegration.RED_GRAPE_SEEDS,
                provider.getPlantingMaterial(VineVarieties.RED_GRAPE)
        );
        assertEquals(
                VineryIntegration.RED_GRAPE,
                provider.getHarvestItem(VineVarieties.RED_GRAPE)
        );
        assertEquals(
                VineryIntegration.WHITE_GRAPE_SEEDS,
                provider.getPlantingMaterial(VineVarieties.WHITE_GRAPE)
        );
        assertEquals(
                VineryIntegration.WHITE_GRAPE,
                provider.getHarvestItem(VineVarieties.WHITE_GRAPE)
        );
    }

    @Test
    void requiresBothResourcesForEachVariety() {
        Set<ResourceId> redResources = Set.of(
                VineryIntegration.RED_GRAPE_SEEDS,
                VineryIntegration.RED_GRAPE
        );
        VineryGrapeProvider redOnly = new VineryGrapeProvider(redResources::contains);
        VineryGrapeProvider missingHarvest = new VineryGrapeProvider(
                VineryIntegration.RED_GRAPE_SEEDS::equals
        );

        assertTrue(redOnly.isAvailable(VineVarieties.RED_GRAPE));
        assertFalse(redOnly.isAvailable(VineVarieties.WHITE_GRAPE));
        assertFalse(missingHarvest.isAvailable(VineVarieties.RED_GRAPE));
    }
}
