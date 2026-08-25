package com.djden.alcoholic.application.ingredient;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.registry.RegistrationException;
import com.djden.alcoholic.domain.ingredient.IngredientType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Open mapping from semantic tags to category identifiers.
 */
public final class IngredientSemanticRegistry {
    private final Map<ResourceId, ResourceId> tagToCategory = new LinkedHashMap<>();
    private boolean frozen;

    public static IngredientSemanticRegistry builtins() {
        IngredientSemanticRegistry registry = new IngredientSemanticRegistry();
        registry.bind(SemanticTags.GRAPES, IngredientType.GRAPE.categoryId());
        registry.bind(SemanticTags.RED_GRAPES, IngredientType.RED_GRAPE.categoryId());
        registry.bind(SemanticTags.WHITE_GRAPES, IngredientType.WHITE_GRAPE.categoryId());
        registry.bind(SemanticTags.BARLEY, IngredientType.BARLEY.categoryId());
        registry.bind(SemanticTags.HOPS, IngredientType.HOPS.categoryId());
        registry.bind(SemanticTags.YEAST, IngredientType.YEAST.categoryId());
        registry.bind(SemanticTags.MALTED_GRAIN, new ResourceId("alcoholic", "malted_grain"));
        registry.bind(SemanticTags.MALTED_BARLEY, new ResourceId("alcoholic", "malted_barley"));
        registry.bind(SemanticTags.GRIST, new ResourceId("alcoholic", "grist"));
        return registry;
    }

    public synchronized void bind(ResourceId tag, ResourceId category) {
        Objects.requireNonNull(tag, "tag");
        Objects.requireNonNull(category, "category");
        if (frozen) {
            throw new RegistrationException("Ingredient semantic registry is frozen");
        }
        ResourceId existing = tagToCategory.putIfAbsent(tag, category);
        if (existing != null && !existing.equals(category)) {
            throw new RegistrationException("Tag " + tag + " already bound to " + existing);
        }
    }

    public synchronized void freeze() {
        frozen = true;
    }

    public synchronized Optional<ResourceId> categoryOf(ResourceId tag) {
        return Optional.ofNullable(tagToCategory.get(tag));
    }

    public synchronized Set<ResourceId> tags() {
        return Set.copyOf(tagToCategory.keySet());
    }

    public synchronized Map<ResourceId, ResourceId> snapshot() {
        return Map.copyOf(tagToCategory);
    }
}
