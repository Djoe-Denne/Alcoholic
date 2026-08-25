package com.djden.alcoholic.application.viticulture;

import com.djden.alcoholic.domain.viticulture.Vine;
import com.djden.alcoholic.domain.viticulture.VineGrowthParameters;
import com.djden.alcoholic.domain.viticulture.VineyardGrowthService;
import com.djden.alcoholic.api.ResourceId;

import java.util.Objects;

public final class GrowVineUseCase {
    private final VineyardGrowthService growthService;

    public GrowVineUseCase() {
        this(new VineyardGrowthService());
    }

    public GrowVineUseCase(VineyardGrowthService growthService) {
        this.growthService = Objects.requireNonNull(growthService, "growthService");
    }

    public Vine<ResourceId> grow(
            Vine<ResourceId> vine,
            VineGrowthParameters parameters
    ) {
        return growthService.grow(vine, parameters);
    }
}
