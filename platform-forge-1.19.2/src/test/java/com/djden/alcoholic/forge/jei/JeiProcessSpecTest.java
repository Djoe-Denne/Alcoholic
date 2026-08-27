package com.djden.alcoholic.forge.jei;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class JeiProcessSpecTest {
    @Test
    void recipeTypeUidKeepsTheFullResourceId() {
        assertEquals(
                "alcoholic",
                JeiProcessSpec.of(BuiltinRegistrations.MILL).recipeType().getUid().getNamespace()
        );
        assertEquals(
                "mill",
                JeiProcessSpec.of(BuiltinRegistrations.MILL).recipeType().getUid().getPath()
        );
        assertNotEquals(
                JeiProcessSpec.of(BuiltinRegistrations.FERMENT).recipeType(),
                JeiProcessSpec.of(ResourceId.parse("mymod:ferment")).recipeType()
        );
    }
}
