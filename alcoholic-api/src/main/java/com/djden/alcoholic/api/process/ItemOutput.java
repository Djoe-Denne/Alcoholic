package com.djden.alcoholic.api.process;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;

import java.util.Map;
import java.util.Objects;

@PublicApi
public record ItemOutput(ResourceId item, int amount, Map<ResourceId, Object> properties) {
    public ItemOutput {
        Objects.requireNonNull(item, "item");
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        properties = properties == null ? Map.of() : Map.copyOf(properties);
    }

    public ItemOutput(ResourceId item, int amount) {
        this(item, amount, Map.of());
    }
}
