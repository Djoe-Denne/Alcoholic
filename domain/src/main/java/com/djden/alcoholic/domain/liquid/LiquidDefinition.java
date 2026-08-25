package com.djden.alcoholic.domain.liquid;

import com.djden.alcoholic.api.ResourceId;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Kind of liquid, independent of any particular stored batch.
 */
public record LiquidDefinition(
        ResourceId id,
        Map<ResourceId, Object> defaults
) {
    public LiquidDefinition {
        Objects.requireNonNull(id, "id");
        Map<ResourceId, Object> copy = new LinkedHashMap<>();
        Objects.requireNonNull(defaults, "defaults").forEach((key, value) -> copy.put(
                Objects.requireNonNull(key, "property"),
                Objects.requireNonNull(value, "value")
        ));
        defaults = Map.copyOf(copy);
    }
}
