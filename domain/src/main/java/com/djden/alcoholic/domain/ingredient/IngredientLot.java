package com.djden.alcoholic.domain.ingredient;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.ingredient.SolidInputView;
import com.djden.alcoholic.api.property.LiquidProperty;
import com.djden.alcoholic.domain.liquid.PropertyBag;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A counted solid offering, including optional agricultural lot properties.
 */
public record IngredientLot(
        ResourceId item,
        int count,
        PropertyBag properties
) implements SolidInputView {
    public IngredientLot {
        Objects.requireNonNull(item, "item");
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0");
        }
        properties = properties == null ? PropertyBag.empty() : properties;
    }

    public static IngredientLot of(ResourceId item, int count) {
        return new IngredientLot(item, count, PropertyBag.empty());
    }

    @Override
    public Set<ResourceId> propertyIds() {
        return properties.ids();
    }

    @Override
    public Optional<Object> property(ResourceId id) {
        return properties.get(id);
    }

    @Override
    public <T> Optional<T> property(LiquidProperty<T> property) {
        return properties.get(property);
    }
}
