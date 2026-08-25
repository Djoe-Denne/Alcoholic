package com.djden.alcoholic.api.beverage;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;

import java.util.Objects;
import java.util.Optional;

@PublicApi
public record BeverageIdentity(ResourceId definitionId, Optional<String> variant) {
    public BeverageIdentity {
        Objects.requireNonNull(definitionId, "definitionId");
        variant = Objects.requireNonNull(variant, "variant");
    }

    public BeverageIdentity(ResourceId definitionId) {
        this(definitionId, Optional.empty());
    }
}
