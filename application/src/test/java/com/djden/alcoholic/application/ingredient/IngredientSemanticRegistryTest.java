package com.djden.alcoholic.application.ingredient;

import com.djden.alcoholic.api.ResourceId;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngredientSemanticRegistryTest {
    @Test
    void resolvesOpenCategoriesWithoutClosedEnums() {
        IngredientSemanticRegistry registry = IngredientSemanticRegistry.builtins();
        registry.bind(SemanticTags.APPLES, ResourceId.parse("alcoholic:apple"));
        SemanticIngredientResolver<String> resolver = new SemanticIngredientResolver<>(
                (candidate, tag) -> candidate.equals("pack:apple") && tag.equals(SemanticTags.APPLES),
                registry
        );

        assertEquals(Set.of(ResourceId.parse("alcoholic:apple")), resolver.resolveCategories("pack:apple"));
        assertTrue(resolver.resolve("pack:apple").isEmpty());
    }
}
