package com.djden.alcoholic.application.viticulture;

import com.djden.alcoholic.domain.viticulture.VineVariety;
import com.djden.alcoholic.api.ResourceId;

public interface GrapeProviderPort {
    ResourceId getPlantingMaterial(VineVariety<ResourceId> variety);

    ResourceId getHarvestItem(VineVariety<ResourceId> variety);

    boolean isAvailable(VineVariety<ResourceId> variety);
}
