package com.djden.alcoholic.api.registry;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@PublicApi
public interface RegistryView<T> {
    Optional<T> get(ResourceId id);

    default boolean contains(ResourceId id) {
        return get(id).isPresent();
    }

    Set<ResourceId> ids();

    Collection<T> values();
}
