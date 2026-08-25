package com.djden.alcoholic.domain.beverage;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.ingredient.IngredientSelector;

import java.util.Objects;
import java.util.Optional;

public sealed interface InputReference {
    record ItemInput(ResourceId item) implements InputReference {
        public ItemInput {
            Objects.requireNonNull(item, "item");
        }
    }

    record TagInput(ResourceId tag) implements InputReference {
        public TagInput {
            Objects.requireNonNull(tag, "tag");
        }
    }

    record IngredientInput(ResourceId ingredient) implements InputReference {
        public IngredientInput {
            Objects.requireNonNull(ingredient, "ingredient");
        }
    }

    record BeverageInput(ResourceId beverage) implements InputReference {
        public BeverageInput {
            Objects.requireNonNull(beverage, "beverage");
        }
    }

    record NodeOutputInput(String nodeId, String port) implements InputReference {
        public NodeOutputInput {
            Objects.requireNonNull(nodeId, "nodeId");
            Objects.requireNonNull(port, "port");
            if (nodeId.isBlank()) {
                throw new IllegalArgumentException("nodeId is blank");
            }
            if (port.isBlank()) {
                throw new IllegalArgumentException("port is blank");
            }
        }
    }

    default Optional<IngredientSelector> toSelector() {
        if (this instanceof ItemInput input) {
            return Optional.of(new IngredientSelector.Item(input.item()));
        }
        if (this instanceof TagInput input) {
            return Optional.of(new IngredientSelector.Tag(input.tag()));
        }
        if (this instanceof IngredientInput input) {
            return Optional.of(new IngredientSelector.DefinedIngredient(input.ingredient()));
        }
        if (this instanceof BeverageInput input) {
            return Optional.of(new IngredientSelector.Beverage(input.beverage()));
        }
        return Optional.empty();
    }
}
