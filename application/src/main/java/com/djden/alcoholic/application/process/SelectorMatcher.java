package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.ingredient.IngredientSelector;
import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.domain.ingredient.IngredientDefinition;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

@FunctionalInterface
public interface SelectorMatcher {
    boolean matches(IngredientSelector selector, ResourceId item);

    static SelectorMatcher tags(Function<ResourceId, Set<ResourceId>> tagsOf, BeverageCatalog catalog) {
        Objects.requireNonNull(tagsOf, "tagsOf");
        Objects.requireNonNull(catalog, "catalog");
        return (selector, item) -> {
            if (selector instanceof IngredientSelector.Item value) {
                return value.id().equals(item);
            }
            if (selector instanceof IngredientSelector.Tag value) {
                return tagsOf.apply(item).contains(value.id());
            }
            if (selector instanceof IngredientSelector.DefinedIngredient value) {
                if (value.id().equals(item)) {
                    return true;
                }
                return catalog.ingredient(value.id())
                        .map(IngredientDefinition::tags)
                        .map(tags -> tagsOf.apply(item).stream().anyMatch(tags::contains))
                        .orElse(false);
            }
            return false;
        };
    }
}
