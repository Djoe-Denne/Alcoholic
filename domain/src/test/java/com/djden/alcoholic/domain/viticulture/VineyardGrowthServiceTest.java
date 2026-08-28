package com.djden.alcoholic.domain.viticulture;

import com.djden.alcoholic.domain.ingredient.GrapeColor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VineyardGrowthServiceTest {
    private static final VineVariety<String> CHARDONNAY =
            new VineVariety<>("alcoholic:chardonnay", GrapeColor.WHITE);
    private static final VineEnvironment IDEAL =
            ClimateProfile.TEMPERATE.idealEnvironment();
    private static final VineGrowthParameters CERTAIN_GROWTH =
            new VineGrowthParameters(IDEAL, 1.0, 0.0);
    private final VineyardGrowthService certainGrowth = new VineyardGrowthService(
            new VineGrowthConfig(1.0, ClimateProfile.TEMPERATE)
    );

    @Test
    void firstCycleTraversesEstablishmentAndVegetativeStages() {
        Vine<String> vine = Vine.planted(CHARDONNAY);

        List<VineGrowthStage> expected = List.of(
                VineGrowthStage.ESTABLISHING,
                VineGrowthStage.VEGETATIVE,
                VineGrowthStage.FLOWERING,
                VineGrowthStage.GREEN_FRUIT,
                VineGrowthStage.RIPENING,
                VineGrowthStage.HARVEST_READY
        );

        for (VineGrowthStage stage : expected) {
            vine = certainGrowth.advance(vine, CERTAIN_GROWTH);
            assertEquals(stage, vine.growthStage());
        }
    }

    @Test
    void successfulEvaluationsPersistProgressUntilTheStageThreshold() {
        VineyardGrowthService service = new VineyardGrowthService(
                new VineGrowthConfig(1.0, ClimateProfile.TEMPERATE, 0.4)
        );
        Vine<String> planted = Vine.planted(CHARDONNAY);

        Vine<String> first = service.advance(planted, CERTAIN_GROWTH);
        Vine<String> failed = service.advance(
                first,
                new VineGrowthParameters(IDEAL, 1.0, 1.0)
        );
        Vine<String> second = service.advance(failed, CERTAIN_GROWTH);
        Vine<String> transitioned = service.advance(second, CERTAIN_GROWTH);

        assertEquals(VineGrowthStage.PLANTED, first.growthStage());
        assertEquals(0.4, first.growthProgress(), 0.000_001);
        assertSame(first, failed);
        assertEquals(0.8, second.growthProgress(), 0.000_001);
        assertEquals(VineGrowthStage.ESTABLISHING, transitioned.growthStage());
        assertEquals(0.0, transitioned.growthProgress());
        assertEquals(Vine.NO_HARVEST, transitioned.lastHarvest());
    }

    @Test
    void dormantEstablishedVineResumesAtFloweringAndNeverReturnsToEarlyStages() {
        Vine<String> dormant = new Vine<>(
                CHARDONNAY,
                VineGrowthStage.DORMANT,
                1,
                true,
                VineHealth.HEALTHY
        );

        Vine<String> flowering = certainGrowth.advance(dormant, CERTAIN_GROWTH);

        assertEquals(VineGrowthStage.FLOWERING, flowering.growthStage());
        assertEquals(0.0, flowering.growthProgress());
        assertTrue(flowering.hasEstablished());
        assertEquals(1, flowering.ageCycles());
        assertEquals(0L, flowering.lastHarvest());
    }

    @Test
    void suppliedRollMakesOccasionalProgressDeterministic() {
        VineyardGrowthService service = new VineyardGrowthService(
                new VineGrowthConfig(0.5, ClimateProfile.TEMPERATE)
        );
        Vine<String> vine = Vine.planted(CHARDONNAY).withHealth(VineHealth.POOR);
        double expectedChance = 0.5 * VineHealth.POOR.growthMultiplier() * 0.5;

        assertEquals(
                expectedChance,
                service.growthChance(vine, IDEAL, 0.5),
                0.000_001
        );
        assertEquals(
                VineGrowthStage.ESTABLISHING,
                service.advance(
                        vine,
                        new VineGrowthParameters(IDEAL, 0.5, expectedChance - 0.001)
                ).growthStage()
        );
        assertSame(
                vine,
                service.advance(
                        vine,
                        new VineGrowthParameters(IDEAL, 0.5, expectedChance)
                )
        );
    }

    @Test
    void speedIsBaseTimesClimateHealthAndTrellising() {
        VineyardGrowthService service = new VineyardGrowthService(
                new VineGrowthConfig(0.8, ClimateProfile.TEMPERATE)
        );
        Vine<String> vine = Vine.planted(CHARDONNAY)
                .withHealth(new VineHealth(0.75, 1.0, 0.0));
        VineEnvironment partlySuitable = new VineEnvironment(31.0, 0.65, 0.75);

        double climate = ClimateProfile.TEMPERATE.suitability(partlySuitable);

        assertEquals(
                0.8 * climate * 0.75 * 1.25,
                service.growthChance(vine, partlySuitable, 1.25),
                0.000_001
        );
    }

    @Test
    void idealClimateAndBetterTrellisingIncreaseGrowthChance() {
        Vine<String> vine = Vine.planted(CHARDONNAY);
        VineEnvironment hostile = new VineEnvironment(-20.0, 0.0, 0.0);

        double hostileChance = certainGrowth.growthChance(vine, hostile, 0.5);
        double idealChance = certainGrowth.growthChance(vine, IDEAL, 1.5);

        assertTrue(idealChance > hostileChance);
        assertEquals(1.0, idealChance);
    }

    @Test
    void harvestReadyVinesDoNotAdvanceThroughGrowth() {
        Vine<String> ready = new Vine<>(
                CHARDONNAY,
                VineGrowthStage.HARVEST_READY,
                0,
                false,
                VineHealth.HEALTHY
        );

        assertEquals(0.0, certainGrowth.growthChance(ready, CERTAIN_GROWTH));
        assertSame(ready, certainGrowth.advance(ready, CERTAIN_GROWTH));
    }

    @Test
    void fertilizeAdvancesOneStageIgnoringClimateAndProgress() {
        VineyardGrowthService service = new VineyardGrowthService(
                new VineGrowthConfig(0.0, ClimateProfile.TEMPERATE, 0.25)
        );
        Vine<String> vine = Vine.planted(CHARDONNAY).withGrowthProgress(0.1);

        Vine<String> fertilized = service.fertilize(vine);

        assertEquals(VineGrowthStage.ESTABLISHING, fertilized.growthStage());
        assertEquals(0.0, fertilized.growthProgress());
    }

    @Test
    void fertilizeDoesNotAdvanceHarvestReady() {
        Vine<String> ready = new Vine<>(
                CHARDONNAY,
                VineGrowthStage.HARVEST_READY,
                0,
                false,
                VineHealth.HEALTHY
        );

        assertSame(ready, certainGrowth.fertilize(ready));
    }

    @Test
    void fertilizeWakesDormantAtFlowering() {
        Vine<String> dormant = new Vine<>(
                CHARDONNAY,
                VineGrowthStage.DORMANT,
                1,
                true,
                VineHealth.HEALTHY
        );

        assertEquals(
                VineGrowthStage.FLOWERING,
                certainGrowth.fertilize(dormant).growthStage()
        );
    }
}
