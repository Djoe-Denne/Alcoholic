package com.djden.alcoholic.api.liquid;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;

import java.util.Map;

/**
 * Compact, flattened history of a liquid batch. Not a per-tick event log.
 */
@PublicApi
public interface BatchProvenanceView {
    int schemaVersion();

    Map<ResourceId, Double> originComposition();

    Map<ResourceId, Double> blendComposition();

    double fermentationStress();

    double totalAgingTime();

    double woodExposure();

    double oxidationExposure();
}
