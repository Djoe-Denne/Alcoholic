package com.djden.alcoholic.application.viticulture;

import com.djden.alcoholic.domain.viticulture.PruningLevel;
import com.djden.alcoholic.domain.viticulture.Vine;
import com.djden.alcoholic.domain.viticulture.VineGrowthStage;
import com.djden.alcoholic.api.ResourceId;

import java.util.Objects;

public final class PruneVineUseCase {
    public Vine<ResourceId> prune(
            Vine<ResourceId> vine,
            PruningLevel pruningLevel
    ) {
        Objects.requireNonNull(vine, "vine");
        Objects.requireNonNull(pruningLevel, "pruningLevel");
        if (vine.growthStage() != VineGrowthStage.DORMANT) {
            throw new IllegalStateException("only a dormant vine can be pruned");
        }
        return vine.withPruningLevel(pruningLevel);
    }
}
