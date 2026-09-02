package com.djden.alcoholic.application.machine;

import com.djden.alcoholic.domain.multiblock.PartRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiblockDisplayRecipesTest {
    @Test
    void builtinsExposeEightMinHullRecipes() {
        List<MultiblockDisplayRecipe> recipes = MultiblockDisplayRecipes.from(MachineCatalog.builtins());
        assertEquals(14, recipes.size());
        assertEquals(4, recipes.get(0).layers().size());
    }

    @Test
    void pressAndMillRequireAKineticPort() {
        List<MultiblockDisplayRecipe> recipes = MultiblockDisplayRecipes.from(MachineCatalog.builtins());
        MultiblockDisplayRecipe press = recipe(recipes, BuiltinMachines.INDUSTRIAL_PRESS.toString());
        MultiblockDisplayRecipe mill = recipe(recipes, BuiltinMachines.INDUSTRIAL_ROLLER_MILL.toString());
        MultiblockDisplayRecipe tank = recipe(recipes, BuiltinMachines.INDUSTRIAL_TANK.toString());
        MultiblockDisplayRecipe craftMill = recipe(recipes, BuiltinCraftMachines.CRAFT_MILL.toString());
        MultiblockDisplayRecipe craftMash = recipe(recipes, BuiltinCraftMachines.CRAFT_MASH_TUN.toString());
        assertTrue(press.kineticRequired());
        assertTrue(mill.kineticRequired());
        assertTrue(craftMill.kineticRequired());
        assertFalse(tank.kineticRequired());
        assertEquals(3, craftMash.layers().size());
        assertTrue(hasRole(press, PartRole.KINETIC_PORT));
        assertTrue(hasRole(mill, PartRole.KINETIC_PORT));
        assertTrue(hasRole(craftMill, PartRole.KINETIC_PORT));
        assertFalse(hasRole(tank, PartRole.KINETIC_PORT));
    }

    private static MultiblockDisplayRecipe recipe(List<MultiblockDisplayRecipe> recipes, String id) {
        return recipes.stream()
                .filter(recipe -> recipe.definitionId().toString().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private static boolean hasRole(MultiblockDisplayRecipe recipe, PartRole role) {
        return recipe.ingredients().stream().anyMatch(ingredient -> ingredient.role() == role);
    }
}
