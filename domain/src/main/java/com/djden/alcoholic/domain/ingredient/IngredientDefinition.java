package com.djden.alcoholic.domain.ingredient;

import com.djden.alcoholic.api.ResourceId;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public record IngredientDefinition(ResourceId id, Set<ResourceId> tags) {
    public IngredientDefinition {
        Objects.requireNonNull(id, "id");
        tags = Set.copyOf(new LinkedHashSet<>(Objects.requireNonNull(tags, "tags")));
    }
}
