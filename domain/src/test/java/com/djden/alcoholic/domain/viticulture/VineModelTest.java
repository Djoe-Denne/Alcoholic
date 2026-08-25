package com.djden.alcoholic.domain.viticulture;

import com.djden.alcoholic.domain.ingredient.GrapeColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VineModelTest {
    private static final VineVariety<String> MERLOT =
            new VineVariety<>("alcoholic:merlot", GrapeColor.RED);

    @Test
    void createsAValidatedGenericVarietyAndBalancedVineByDefault() {
        Vine<String> vine = Vine.planted(MERLOT);

        assertEquals("alcoholic:merlot", vine.variety().id());
        assertEquals(GrapeColor.RED, vine.variety().grapeColor());
        assertEquals(VineGrowthStage.PLANTED, vine.growthStage());
        assertEquals(VineHealth.HEALTHY, vine.health());
        assertEquals(PruningLevel.BALANCED, vine.pruningLevel());
        assertEquals(0, vine.ageCycles());
        assertEquals(0.0, vine.growthProgress());
        assertEquals(Vine.NO_HARVEST, vine.lastHarvest());
    }

    @Test
    void stateChangesReturnValidatedCopies() {
        Vine<String> original = Vine.planted(MERLOT);

        Vine<String> changed = original
                .withHealth(VineHealth.THRIVING)
                .withPruningLevel(PruningLevel.LIGHT);

        assertNotSame(original, changed);
        assertEquals(VineHealth.HEALTHY, original.health());
        assertEquals(PruningLevel.BALANCED, original.pruningLevel());
        assertEquals(VineHealth.THRIVING, changed.health());
        assertEquals(PruningLevel.LIGHT, changed.pruningLevel());
        assertEquals(0.0, changed.growthProgress());
        assertEquals(Vine.NO_HARVEST, changed.lastHarvest());
    }

    @Test
    void rejectsImpossibleLifecycleStates() {
        assertThrows(IllegalArgumentException.class, () -> new Vine<>(
                MERLOT,
                VineGrowthStage.PLANTED,
                1,
                true,
                VineHealth.HEALTHY
        ));
        assertThrows(IllegalArgumentException.class, () -> new Vine<>(
                MERLOT,
                VineGrowthStage.DORMANT,
                0,
                true,
                VineHealth.HEALTHY
        ));
        assertThrows(IllegalArgumentException.class, () -> new Vine<>(
                MERLOT,
                VineGrowthStage.VEGETATIVE,
                1,
                true,
                VineHealth.HEALTHY
        ));
        assertThrows(IllegalArgumentException.class, () -> new Vine<>(
                MERLOT,
                VineGrowthStage.FLOWERING,
                1,
                false,
                VineHealth.HEALTHY
        ));
    }

    @Test
    void validatesGrowthProgressAndHarvestSentinel() {
        assertThrows(IllegalArgumentException.class, () -> new Vine<>(
                MERLOT,
                VineGrowthStage.FLOWERING,
                0,
                false,
                VineHealth.HEALTHY,
                PruningLevel.BALANCED,
                1.0,
                Vine.NO_HARVEST
        ));
        assertThrows(IllegalArgumentException.class, () -> new Vine<>(
                MERLOT,
                VineGrowthStage.FLOWERING,
                0,
                false,
                VineHealth.HEALTHY,
                PruningLevel.BALANCED,
                0.5,
                10L
        ));
        assertThrows(IllegalArgumentException.class, () -> new Vine<>(
                MERLOT,
                VineGrowthStage.FLOWERING,
                1,
                true,
                VineHealth.HEALTHY,
                PruningLevel.BALANCED,
                0.5,
                Vine.NO_HARVEST
        ));
    }

    @Test
    void validatesNormalizedEnvironmentAndHealthValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new VineEnvironment(20.0, 1.1, 0.5)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new VineEnvironment(Double.NaN, 0.5, 0.5)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new VineHealth(2.1, 1.0, 0.0)
        );
    }
}
