package com.djden.alcoholic.api.vessel;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;

import java.util.List;
import java.util.Optional;

/**
 * Compact persistent vessel history. Identifiers are semantic, not drink classes.
 */
@PublicApi
public interface BarrelHistoryView {
    int usageCount();

    List<ResourceId> previousContents();

    default Optional<Integer> toastLevel() {
        return Optional.empty();
    }

    default Optional<Integer> charLevel() {
        return Optional.empty();
    }

    default Optional<Double> woodExtractionRemaining() {
        return Optional.empty();
    }
}
