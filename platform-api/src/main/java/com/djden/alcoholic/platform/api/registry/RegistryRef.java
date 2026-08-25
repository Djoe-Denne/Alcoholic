package com.djden.alcoholic.platform.api.registry;

import com.djden.alcoholic.api.ResourceId;

import java.util.function.Supplier;

public interface RegistryRef<T> extends Supplier<T> {
    ResourceId id();
}
