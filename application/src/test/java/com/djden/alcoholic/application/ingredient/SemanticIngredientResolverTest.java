package com.djden.alcoholic.application.ingredient;

import com.djden.alcoholic.domain.ingredient.IngredientType;
import com.djden.alcoholic.api.ResourceId;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticIngredientResolverTest {
    private static final String OWN_RED_GRAPES = "alcoholic:red_grapes";
    private static final String VINERY_WHITE_GRAPES = "vinery:taiga_grapes_white";

    private final Map<String, Set<ResourceId>> memberships = Map.of(
            OWN_RED_GRAPES, Set.of(SemanticTags.GRAPES, SemanticTags.RED_GRAPES),
            VINERY_WHITE_GRAPES, Set.of(SemanticTags.GRAPES, SemanticTags.WHITE_GRAPES)
    );
    private final SemanticIngredientResolver<String> resolver =
            new SemanticIngredientResolver<>(
                    (candidate, tag) -> memberships.getOrDefault(candidate, Set.of()).contains(tag)
            );

    @Test
    void resolvesBuiltinGrapesThroughSemanticTags() {
        assertEquals(
                Set.of(IngredientType.GRAPE, IngredientType.RED_GRAPE),
                resolver.resolve(OWN_RED_GRAPES)
        );
    }

    @Test
    void resolvesExternalGrapesWithoutUsingNbtOrHardcodedProcessingRules() {
        assertEquals(
                Set.of(IngredientType.GRAPE, IngredientType.WHITE_GRAPE),
                resolver.resolve(VINERY_WHITE_GRAPES)
        );
    }

    @Test
    void leavesUnknownIngredientsUnclassified() {
        assertTrue(resolver.resolve("other:unknown").isEmpty());
    }
}
