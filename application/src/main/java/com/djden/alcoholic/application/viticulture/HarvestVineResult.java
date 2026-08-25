package com.djden.alcoholic.application.viticulture;

import com.djden.alcoholic.domain.viticulture.GrapeHarvest;
import com.djden.alcoholic.api.ResourceId;

import java.util.Objects;

public record HarvestVineResult(
        GrapeHarvest<ResourceId> harvest,
        ResourceId harvestItem
) {
    public HarvestVineResult {
        Objects.requireNonNull(harvest, "harvest");
        Objects.requireNonNull(harvestItem, "harvestItem");
    }
}
