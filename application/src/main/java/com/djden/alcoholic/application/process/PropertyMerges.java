package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.property.PropertyAggregator;
import com.djden.alcoholic.api.property.PropertyMerge;

import java.util.function.Function;

public final class PropertyMerges {
    private PropertyMerges() {
    }

    public static Function<ResourceId, PropertyMerge> from(AlcoholicApi api) {
        return id -> api.properties()
                .get(id)
                .map(property -> property.merge())
                .orElse(PropertyMerge.WEIGHTED_AVERAGE);
    }

    public static Function<ResourceId, PropertyAggregator> aggregators(AlcoholicApi api) {
        return id -> api.properties()
                .get(id)
                .map(property -> property.aggregator())
                .orElse(PropertyAggregator.forStrategy(PropertyMerge.WEIGHTED_AVERAGE));
    }
}
