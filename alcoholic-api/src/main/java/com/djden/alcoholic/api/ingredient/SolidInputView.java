package com.djden.alcoholic.api.ingredient;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.property.LiquidProperty;

import java.util.Optional;
import java.util.Set;

/**
 * A concrete solid input offered to a process executor, including agricultural lot data.
 */
@PublicApi
public interface SolidInputView {
    ResourceId item();

    int count();

    Set<ResourceId> propertyIds();

    Optional<Object> property(ResourceId id);

    default <T> Optional<T> property(LiquidProperty<T> property) {
        return property(property.id()).map(property.valueType()::cast);
    }
}
