package com.djden.alcoholic.api.ingredient;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;

import java.util.Objects;

/**
 * External input selector. Graph-internal node ports stay in the domain model.
 */
@PublicApi
public sealed interface IngredientSelector {
    record Item(ResourceId id) implements IngredientSelector {
        public Item {
            Objects.requireNonNull(id, "id");
        }
    }

    record Tag(ResourceId id) implements IngredientSelector {
        public Tag {
            Objects.requireNonNull(id, "id");
        }
    }

    record DefinedIngredient(ResourceId id) implements IngredientSelector {
        public DefinedIngredient {
            Objects.requireNonNull(id, "id");
        }
    }

    record Beverage(ResourceId id) implements IngredientSelector {
        public Beverage {
            Objects.requireNonNull(id, "id");
        }
    }
}
