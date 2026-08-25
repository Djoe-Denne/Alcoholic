package com.djden.alcoholic.domain.viticulture;

import com.djden.alcoholic.domain.ingredient.GrapeColor;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VineyardHarvestServiceTest {
    private static final VineVariety<String> SYRAH =
            new VineVariety<>("alcoholic:syrah", GrapeColor.RED);
    private static final VineEnvironment IDEAL =
            ClimateProfile.TEMPERATE.idealEnvironment();
    private final VineyardHarvestService service = new VineyardHarvestService();

    @Test
    void harvestIsNonDestructiveAndStartsDormancy() {
        Vine<String> ready = new Vine<>(
                SYRAH,
                VineGrowthStage.HARVEST_READY,
                0,
                false,
                VineHealth.HEALTHY,
                PruningLevel.BALANCED,
                0.75,
                Vine.NO_HARVEST
        );

        GrapeHarvest<String> harvest = service.harvest(ready, IDEAL, 1_234L);

        assertEquals(VineGrowthStage.HARVEST_READY, ready.growthStage());
        assertFalse(ready.hasEstablished());
        assertEquals(0, ready.ageCycles());
        assertEquals(0.75, ready.growthProgress());
        assertEquals(Vine.NO_HARVEST, ready.lastHarvest());
        assertNotSame(ready, harvest.vine());
        assertSame(ready.variety(), harvest.vine().variety());
        assertSame(harvest.vine(), harvest.postHarvestVine());
        assertEquals(VineGrowthStage.DORMANT, harvest.vine().growthStage());
        assertEquals(0.0, harvest.vine().growthProgress());
        assertEquals(1_234L, harvest.vine().lastHarvest());
        assertTrue(harvest.vine().hasEstablished());
        assertEquals(1, harvest.vine().ageCycles());
    }

    @Test
    void repeatedHarvestsIncrementAgeWithoutRepeatingEstablishment() {
        GrapeHarvest<String> first = service.harvest(
                readyVine(PruningLevel.BALANCED, VineHealth.HEALTHY),
                IDEAL,
                100L
        );
        VineyardGrowthService growth = new VineyardGrowthService(
                new VineGrowthConfig(1.0, ClimateProfile.TEMPERATE)
        );
        VineGrowthParameters certainGrowth = new VineGrowthParameters(IDEAL, 0.0);
        Vine<String> vine = first.vine();

        vine = growth.advance(vine, certainGrowth);
        assertEquals(VineGrowthStage.FLOWERING, vine.growthStage());
        vine = growth.advance(vine, certainGrowth);
        vine = growth.advance(vine, certainGrowth);
        vine = growth.advance(vine, certainGrowth);
        assertEquals(VineGrowthStage.HARVEST_READY, vine.growthStage());

        GrapeHarvest<String> second = service.harvest(vine, IDEAL, 200L);

        assertEquals(VineGrowthStage.DORMANT, second.vine().growthStage());
        assertEquals(2, second.vine().ageCycles());
        assertTrue(second.vine().hasEstablished());
        assertEquals(0.0, second.vine().growthProgress());
        assertEquals(200L, second.vine().lastHarvest());
    }

    @Test
    void rejectsHarvestBeforeFruitIsReady() {
        assertThrows(
                IllegalStateException.class,
                () -> service.harvest(Vine.planted(SYRAH), IDEAL)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new GrapeHarvestParameters(IDEAL, 1.0, -1L)
        );
    }

    @Test
    void warmClimateRaisesSugarAndLowersAcidity() {
        Vine<String> vine = readyVine(PruningLevel.BALANCED, VineHealth.HEALTHY);
        VineEnvironment cool = new VineEnvironment(10.0, 0.65, 0.75);
        VineEnvironment warm = new VineEnvironment(34.0, 0.65, 0.75);

        GrapeHarvest<String> coolHarvest = service.harvest(vine, cool);
        GrapeHarvest<String> warmHarvest = service.harvest(vine, warm);

        assertTrue(warmHarvest.sugar() > coolHarvest.sugar());
        assertTrue(warmHarvest.acidity() < coolHarvest.acidity());
    }

    @Test
    void idealSuitabilityImprovesQuality() {
        Vine<String> vine = readyVine(PruningLevel.BALANCED, VineHealth.HEALTHY);
        VineEnvironment unsuitable = new VineEnvironment(-20.0, 0.0, 0.0);

        GrapeHarvest<String> ideal = service.harvest(vine, IDEAL);
        GrapeHarvest<String> poor = service.harvest(vine, unsuitable);

        assertTrue(ideal.quality() > poor.quality());
    }

    @Test
    void differentClimateProfilesChangeVarietySuitability() {
        Vine<String> vine = readyVine(PruningLevel.BALANCED, VineHealth.HEALTHY);
        VineEnvironment coolEnvironment = ClimateProfile.COOL.idealEnvironment();
        VineyardHarvestService coolClimate = new VineyardHarvestService(
                configWith(ClimateProfile.COOL, defaultPruningProfiles())
        );
        VineyardHarvestService warmClimate = new VineyardHarvestService(
                configWith(ClimateProfile.WARM, defaultPruningProfiles())
        );

        assertTrue(
                coolClimate.harvest(vine, coolEnvironment).quality()
                        > warmClimate.harvest(vine, coolEnvironment).quality()
        );
    }

    @Test
    void defaultPruningTradesYieldAgainstQuality() {
        GrapeHarvest<String> light = service.harvest(
                readyVine(PruningLevel.LIGHT, VineHealth.HEALTHY),
                IDEAL
        );
        GrapeHarvest<String> balanced = service.harvest(
                readyVine(PruningLevel.BALANCED, VineHealth.HEALTHY),
                IDEAL
        );
        GrapeHarvest<String> severe = service.harvest(
                readyVine(PruningLevel.SEVERE, VineHealth.HEALTHY),
                IDEAL
        );

        assertTrue(light.quantity() > balanced.quantity());
        assertTrue(balanced.quantity() > severe.quantity());
        assertTrue(light.quality() < balanced.quality());
        assertTrue(balanced.quality() < severe.quality());
    }

    @Test
    void pruningProfilesAreInjectedThroughHarvestConfig() {
        Map<PruningLevel, PruningProfile> profiles = Map.of(
                PruningLevel.LIGHT, new PruningProfile(0.50, 1.50),
                PruningLevel.BALANCED, new PruningProfile(1.00, 1.00),
                PruningLevel.SEVERE, new PruningProfile(1.50, 0.50)
        );
        VineyardHarvestService custom = new VineyardHarvestService(
                configWith(ClimateProfile.TEMPERATE, profiles)
        );

        GrapeHarvest<String> light = custom.harvest(
                readyVine(PruningLevel.LIGHT, VineHealth.HEALTHY),
                IDEAL
        );
        GrapeHarvest<String> severe = custom.harvest(
                readyVine(PruningLevel.SEVERE, VineHealth.HEALTHY),
                IDEAL
        );

        assertTrue(light.quantity() < severe.quantity());
        assertTrue(light.quality() > severe.quality());
    }

    @Test
    void betterTrellisingImprovesYieldAndQuality() {
        Vine<String> vine = readyVine(PruningLevel.BALANCED, VineHealth.HEALTHY);

        GrapeHarvest<String> weak = service.harvest(
                vine,
                new GrapeHarvestParameters(IDEAL, 0.5)
        );
        GrapeHarvest<String> strong = service.harvest(
                vine,
                new GrapeHarvestParameters(IDEAL, 1.5)
        );

        assertTrue(strong.quantity() > weak.quantity());
        assertTrue(strong.quality() > weak.quality());
    }

    @Test
    void allHarvestOutputsAreBounded() {
        PruningProfile extremePruning = new PruningProfile(2.0, 2.0);
        GrapeHarvestConfig extremeConfig = new GrapeHarvestConfig(
                5.0,
                5.0,
                1.0,
                1.0,
                0.0,
                1.0,
                1.0,
                1.0,
                1.0,
                ClimateProfile.TEMPERATE,
                Map.of(
                        PruningLevel.LIGHT, extremePruning,
                        PruningLevel.BALANCED, extremePruning,
                        PruningLevel.SEVERE, extremePruning
                )
        );
        VineyardHarvestService extreme = new VineyardHarvestService(extremeConfig);
        Vine<String> vine = readyVine(
                PruningLevel.SEVERE,
                new VineHealth(2.0, 2.0, 1.0)
        );
        VineEnvironment hot = new VineEnvironment(100.0, 0.65, 0.75);

        GrapeHarvest<String> upper = extreme.harvest(
                vine,
                new GrapeHarvestParameters(hot, 2.0)
        );
        GrapeHarvest<String> zeroYield = extreme.harvest(
                vine,
                new GrapeHarvestParameters(hot, 0.0)
        );

        assertEquals(5.0, upper.quantity());
        assertEquals(1.0, upper.quality());
        assertEquals(1.0, upper.sugar());
        assertEquals(0.0, upper.acidity());
        assertEquals(0.0, zeroYield.quantity());
        assertTrue(upper.quality() >= 0.0 && upper.quality() <= 1.0);
        assertTrue(upper.sugar() >= 0.0 && upper.sugar() <= 1.0);
        assertTrue(upper.acidity() >= 0.0 && upper.acidity() <= 1.0);
    }

    private static Vine<String> readyVine(
            PruningLevel pruningLevel,
            VineHealth health
    ) {
        return new Vine<>(
                SYRAH,
                VineGrowthStage.HARVEST_READY,
                0,
                false,
                health,
                pruningLevel
        );
    }

    private static Map<PruningLevel, PruningProfile> defaultPruningProfiles() {
        return GrapeHarvestConfig.defaults().pruningProfiles();
    }

    private static GrapeHarvestConfig configWith(
            ClimateProfile climateProfile,
            Map<PruningLevel, PruningProfile> pruningProfiles
    ) {
        GrapeHarvestConfig defaults = GrapeHarvestConfig.defaults();
        return new GrapeHarvestConfig(
                defaults.baseQuantity(),
                defaults.maximumQuantity(),
                defaults.baseQuality(),
                defaults.baseSugar(),
                defaults.baseAcidity(),
                defaults.suitabilityQualityBonus(),
                defaults.warmthSugarEffect(),
                defaults.warmthAcidityEffect(),
                defaults.trellisingQualityEffect(),
                climateProfile,
                pruningProfiles
        );
    }
}
