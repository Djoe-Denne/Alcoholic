package com.djden.alcoholic.domain.beverage;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.beverage.BeverageIdentity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record BeverageDefinition(
        ResourceId id,
        Optional<ResourceId> category,
        ProcessGraph graph,
        List<ResourceId> properties,
        Optional<ResourceId> quality
) {
    public BeverageDefinition {
        Objects.requireNonNull(id, "id");
        category = Objects.requireNonNull(category, "category");
        Objects.requireNonNull(graph, "graph");
        properties = List.copyOf(new ArrayList<>(new LinkedHashSet<>(
                Objects.requireNonNull(properties, "properties")
        )));
        quality = quality == null ? Optional.empty() : quality;
    }

    public BeverageDefinition(
            ResourceId id,
            Optional<ResourceId> category,
            ProcessGraph graph,
            List<ResourceId> properties
    ) {
        this(id, category, graph, properties, Optional.empty());
    }

    public BeverageIdentity identity() {
        return new BeverageIdentity(id);
    }

    public Set<ResourceId> propertySet() {
        return Set.copyOf(properties);
    }
}
