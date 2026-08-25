package com.djden.alcoholic.application.viticulture;

import com.djden.alcoholic.domain.viticulture.PruningLevel;
import com.djden.alcoholic.domain.viticulture.Vine;
import com.djden.alcoholic.domain.viticulture.VineGrowthStage;
import com.djden.alcoholic.domain.viticulture.VineHealth;
import com.djden.alcoholic.api.ResourceId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PruneVineUseCaseTest {
    private final PruneVineUseCase useCase = new PruneVineUseCase();

    @Test
    void prunesDormantVineWithoutChangingItsOtherState() {
        Vine<ResourceId> dormant = new Vine<>(
                VineVarieties.RED_GRAPE,
                VineGrowthStage.DORMANT,
                2,
                true,
                VineHealth.HEALTHY,
                PruningLevel.BALANCED,
                0.0,
                500L
        );

        Vine<ResourceId> pruned = useCase.prune(dormant, PruningLevel.SEVERE);

        assertEquals(PruningLevel.SEVERE, pruned.pruningLevel());
        assertEquals(dormant.variety(), pruned.variety());
        assertEquals(dormant.growthStage(), pruned.growthStage());
        assertEquals(dormant.ageCycles(), pruned.ageCycles());
        assertEquals(dormant.lastHarvest(), pruned.lastHarvest());
    }

    @Test
    void rejectsEveryNonDormantGrowthStage() {
        for (VineGrowthStage stage : VineGrowthStage.values()) {
            if (stage == VineGrowthStage.DORMANT) {
                continue;
            }
            Vine<ResourceId> vine = new Vine<>(
                    VineVarieties.RED_GRAPE,
                    stage,
                    0,
                    false,
                    VineHealth.HEALTHY
            );

            assertThrows(
                    IllegalStateException.class,
                    () -> useCase.prune(vine, PruningLevel.LIGHT),
                    () -> "expected pruning to reject " + stage
            );
        }
    }
}
