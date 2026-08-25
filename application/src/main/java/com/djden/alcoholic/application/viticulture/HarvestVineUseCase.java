package com.djden.alcoholic.application.viticulture;

import com.djden.alcoholic.domain.viticulture.GrapeHarvest;
import com.djden.alcoholic.domain.viticulture.GrapeHarvestParameters;
import com.djden.alcoholic.domain.viticulture.Vine;
import com.djden.alcoholic.domain.viticulture.VineyardHarvestService;
import com.djden.alcoholic.api.ResourceId;

import java.util.Objects;

public final class HarvestVineUseCase {
    private final VineyardHarvestService harvestService;
    private final ResolveGrapeProviderUseCase providerResolver;

    public HarvestVineUseCase(ResolveGrapeProviderUseCase providerResolver) {
        this(new VineyardHarvestService(), providerResolver);
    }

    public HarvestVineUseCase(
            VineyardHarvestService harvestService,
            ResolveGrapeProviderUseCase providerResolver
    ) {
        this.harvestService = Objects.requireNonNull(harvestService, "harvestService");
        this.providerResolver = Objects.requireNonNull(providerResolver, "providerResolver");
    }

    public HarvestVineResult harvest(
            Vine<ResourceId> vine,
            GrapeHarvestParameters parameters
    ) {
        GrapeHarvest<ResourceId> harvest = harvestService.harvest(vine, parameters);
        GrapeProviderPort provider = providerResolver.resolve(vine.variety());
        ResourceId harvestItem = provider.getHarvestItem(vine.variety());
        return new HarvestVineResult(harvest, harvestItem);
    }
}
