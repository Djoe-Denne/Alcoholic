package com.djden.alcoholic.api.quality;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;

import java.util.List;

/**
 * Property ids an operator config may read from the batch. Implement this on
 * addon configs so the catalog can reject {@code alcoholic:ethanol}.
 */
@PublicApi
public interface QualityPropertyRefs {
    List<ResourceId> propertyIds();
}
