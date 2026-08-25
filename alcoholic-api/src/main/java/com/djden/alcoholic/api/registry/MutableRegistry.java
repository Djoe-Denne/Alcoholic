package com.djden.alcoholic.api.registry;

import com.djden.alcoholic.api.ResourceId;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public final class MutableRegistry<T> implements RegistryView<T> {
    private final Map<ResourceId, T> values = new LinkedHashMap<>();
    private final Function<T, ResourceId> idAccessor;
    private boolean frozen;

    public MutableRegistry(Function<T, ResourceId> idAccessor) {
        this.idAccessor = Objects.requireNonNull(idAccessor, "idAccessor");
    }

    public synchronized T register(T value) {
        Objects.requireNonNull(value, "value");
        if (frozen) {
            throw new RegistrationException("Registry is frozen");
        }
        ResourceId id = idAccessor.apply(value);
        T existing = values.putIfAbsent(id, value);
        if (existing != null) {
            throw new RegistrationException("Duplicate registration: " + id);
        }
        return value;
    }

    public synchronized void freeze() {
        frozen = true;
    }

    public synchronized boolean isFrozen() {
        return frozen;
    }

    @Override
    public synchronized Optional<T> get(ResourceId id) {
        return Optional.ofNullable(values.get(id));
    }

    @Override
    public synchronized Set<ResourceId> ids() {
        return Set.copyOf(values.keySet());
    }

    @Override
    public synchronized Collection<T> values() {
        return Collections.unmodifiableCollection(ListCopy.copy(values.values()));
    }

    private static final class ListCopy {
        private static <T> Collection<T> copy(Collection<T> values) {
            return java.util.List.copyOf(values);
        }
    }
}
