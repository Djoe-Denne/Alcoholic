package com.djden.alcoholic.domain.liquid;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.property.LiquidProperty;
import com.djden.alcoholic.api.property.PropertyAggregator;
import com.djden.alcoholic.api.property.PropertyMerge;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public final class PropertyBag {
    private final Map<ResourceId, Object> values;

    public PropertyBag(Map<ResourceId, ?> values) {
        Map<ResourceId, Object> copy = new LinkedHashMap<>();
        Objects.requireNonNull(values, "values").forEach((key, value) -> copy.put(
                Objects.requireNonNull(key, "property"),
                Objects.requireNonNull(value, "value")
        ));
        this.values = Map.copyOf(copy);
    }

    public static PropertyBag empty() {
        return new PropertyBag(Map.of());
    }

    public PropertyBag copy() {
        return new PropertyBag(values);
    }

    public Set<ResourceId> ids() {
        return values.keySet();
    }

    public Map<ResourceId, Object> asMap() {
        return values;
    }

    public Optional<Object> get(ResourceId id) {
        return Optional.ofNullable(values.get(id));
    }

    public <T> Optional<T> get(LiquidProperty<T> property) {
        return get(property.id()).map(property.valueType()::cast);
    }

    public PropertyBag with(ResourceId id, Object value) {
        Map<ResourceId, Object> next = new LinkedHashMap<>(values);
        next.put(id, value);
        return new PropertyBag(next);
    }

    public Optional<PropertyBag> merge(
            PropertyBag other,
            double thisVolume,
            double otherVolume,
            Function<ResourceId, PropertyMerge> strategies
    ) {
        return merge(other, thisVolume, otherVolume, strategies, id -> PropertyAggregator.forStrategy(
                strategies == null ? PropertyMerge.WEIGHTED_AVERAGE : strategies.apply(id)
        ));
    }

    public Optional<PropertyBag> merge(
            PropertyBag other,
            double thisVolume,
            double otherVolume,
            Function<ResourceId, PropertyMerge> strategies,
            Function<ResourceId, PropertyAggregator> aggregators
    ) {
        Objects.requireNonNull(other, "other");
        Objects.requireNonNull(strategies, "strategies");
        Objects.requireNonNull(aggregators, "aggregators");
        double total = thisVolume + otherVolume;
        if (total <= 0.0) {
            return Optional.of(empty());
        }
        Map<ResourceId, Object> merged = new LinkedHashMap<>();
        Set<ResourceId> ids = new java.util.LinkedHashSet<>(values.keySet());
        ids.addAll(other.values.keySet());
        for (ResourceId id : ids) {
            Object left = values.get(id);
            Object right = other.values.get(id);
            if (left == null) {
                merged.put(id, right);
                continue;
            }
            if (right == null) {
                merged.put(id, left);
                continue;
            }
            PropertyAggregator aggregator = aggregators.apply(id);
            if (aggregator == null) {
                aggregator = PropertyAggregator.forStrategy(strategies.apply(id));
            }
            Optional<Object> value = aggregator.merge(left, thisVolume, right, otherVolume);
            if (value.isEmpty()) {
                return Optional.empty();
            }
            merged.put(id, value.get());
        }
        return Optional.of(new PropertyBag(merged));
    }
}
