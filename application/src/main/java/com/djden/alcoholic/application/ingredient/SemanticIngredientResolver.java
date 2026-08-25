package com.djden.alcoholic.application.ingredient;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.ingredient.IngredientType;
import com.djden.alcoholic.platform.api.TagMembershipPort;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class SemanticIngredientResolver<T> {
    private static final Map<ResourceId, IngredientType> LEGACY_CATEGORY_TAGS = Map.of(
            SemanticTags.GRAPES, IngredientType.GRAPE,
            SemanticTags.RED_GRAPES, IngredientType.RED_GRAPE,
            SemanticTags.WHITE_GRAPES, IngredientType.WHITE_GRAPE,
            SemanticTags.BARLEY, IngredientType.BARLEY,
            SemanticTags.HOPS, IngredientType.HOPS,
            SemanticTags.YEAST, IngredientType.YEAST
    );

    private final TagMembershipPort<T> membership;
    private final IngredientSemanticRegistry registry;

    public SemanticIngredientResolver(TagMembershipPort<T> membership) {
        this(membership, IngredientSemanticRegistry.builtins());
    }

    public SemanticIngredientResolver(
            TagMembershipPort<T> membership,
            IngredientSemanticRegistry registry
    ) {
        this.membership = Objects.requireNonNull(membership, "membership");
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public Set<ResourceId> resolveCategories(T candidate) {
        Set<ResourceId> resolved = new LinkedHashSet<>();
        registry.snapshot().forEach((tag, category) -> {
            if (membership.isIn(candidate, tag)) {
                resolved.add(category);
            }
        });
        return Set.copyOf(resolved);
    }

    public Set<IngredientType> resolve(T candidate) {
        EnumSet<IngredientType> resolved = EnumSet.noneOf(IngredientType.class);
        LEGACY_CATEGORY_TAGS.forEach((tag, type) -> {
            if (membership.isIn(candidate, tag)) {
                resolved.add(type);
            }
        });
        return Set.copyOf(resolved);
    }

    public boolean is(T candidate, IngredientType type) {
        return resolve(candidate).contains(type);
    }
}
