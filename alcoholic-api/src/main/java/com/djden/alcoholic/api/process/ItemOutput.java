package com.djden.alcoholic.api.process;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;

import java.util.Objects;

@PublicApi
public record ItemOutput(ResourceId item, int amount) {
    public ItemOutput {
        Objects.requireNonNull(item, "item");
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
    }
}
