package com.djden.alcoholic.api.vessel;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;

import java.util.Optional;
import java.util.Set;

/**
 * Processing-relevant vessel characteristics. Not a Minecraft block entity.
 */
@PublicApi
public interface VesselProfileView {
    ResourceId id();

    ResourceId material();

    int capacityMillibuckets();

    Set<ResourceId> processCapabilities();

    double permeability();

    double woodExtractionMultiplier();

    double oxidationMultiplier();

    default Optional<BarrelHistoryView> history() {
        return Optional.empty();
    }

    default VesselProfileView withHistory(BarrelHistoryView history) {
        return this;
    }
}
