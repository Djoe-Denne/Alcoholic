package com.djden.alcoholic.api.property;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;

import java.util.Objects;

@PublicApi
public interface LiquidProperty<T> {
    ResourceId id();

    Class<T> valueType();

    DataCodec<T> codec();

    default PropertyMerge merge() {
        return PropertyMerge.WEIGHTED_AVERAGE;
    }

    default PropertyAggregator aggregator() {
        return PropertyAggregator.forStrategy(merge());
    }

    static <T> LiquidProperty<T> of(ResourceId id, Class<T> valueType, DataCodec<T> codec) {
        return of(id, valueType, codec, PropertyMerge.WEIGHTED_AVERAGE);
    }

    static <T> LiquidProperty<T> of(
            ResourceId id,
            Class<T> valueType,
            DataCodec<T> codec,
            PropertyMerge merge
    ) {
        return of(id, valueType, codec, merge, PropertyAggregator.forStrategy(merge));
    }

    static <T> LiquidProperty<T> of(
            ResourceId id,
            Class<T> valueType,
            DataCodec<T> codec,
            PropertyMerge merge,
            PropertyAggregator aggregator
    ) {
        return new SimpleLiquidProperty<>(id, valueType, codec, merge, aggregator);
    }

    record SimpleLiquidProperty<T>(
            ResourceId id,
            Class<T> valueType,
            DataCodec<T> codec,
            PropertyMerge merge,
            PropertyAggregator aggregator
    ) implements LiquidProperty<T> {
        public SimpleLiquidProperty {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(valueType, "valueType");
            Objects.requireNonNull(codec, "codec");
            Objects.requireNonNull(merge, "merge");
            Objects.requireNonNull(aggregator, "aggregator");
        }
    }
}
