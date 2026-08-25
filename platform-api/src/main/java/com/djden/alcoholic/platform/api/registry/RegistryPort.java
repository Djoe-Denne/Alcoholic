package com.djden.alcoholic.platform.api.registry;

import com.djden.alcoholic.api.ResourceId;

import java.util.function.Supplier;

/**
 * A typed registration capability. The adapter binds an instance to one registry.
 */
@FunctionalInterface
public interface RegistryPort<T> {
    RegistryRef<T> register(ResourceId id, Supplier<? extends T> factory);
}
