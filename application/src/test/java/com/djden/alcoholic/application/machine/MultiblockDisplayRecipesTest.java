package com.djden.alcoholic.application.machine;

import com.djden.alcoholic.domain.multiblock.PartRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiblockDisplayRecipesTest {
    @Test
    void builtinsExposeIndustrialMinHullsAndCraftMinAndMax() {
        List<MultiblockDisplayRecipe> recipes = MultiblockDisplayRecipes.from(MachineCatalog.builtins());
        assertEquals(19, recipes.size());
        assertEquals(4, recipe(recipes, BuiltinMachines.INDUSTRIAL_PRESS.toString()).layers().size());
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

    @Test
    void craftMaltHouseUsesCraftCasingAtMinAndMax() {
        List<MultiblockDisplayRecipe> recipes = MultiblockDisplayRecipes.from(MachineCatalog.builtins());
        List<MultiblockDisplayRecipe> maltHouse = recipes.stream()
                .filter(recipe -> recipe.definitionId().equals(BuiltinCraftMachines.CRAFT_MALT_HOUSE))
                .toList();
        assertEquals(2, maltHouse.size());
        MultiblockDisplayRecipe min = maltHouse.get(0);
        MultiblockDisplayRecipe max = maltHouse.get(1);
        assertEquals(3, min.width());
        assertEquals(3, min.height());
        assertEquals(3, min.depth());
        assertEquals(5, max.width());
        assertEquals(5, max.height());
        assertEquals(5, max.depth());
        assertTrue(hasBlock(min, "alcoholic:craft_casing"));
        assertTrue(hasBlock(max, "alcoholic:craft_casing"));
        assertFalse(hasBlock(min, "alcoholic:industrial_casing"));
        assertFalse(hasBlock(max, "alcoholic:industrial_casing"));
        assertTrue(hasBlock(recipe(recipes, BuiltinMachines.INDUSTRIAL_PRESS.toString()), "alcoholic:industrial_casing"));
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

    private static boolean hasBlock(MultiblockDisplayRecipe recipe, String blockId) {
        return recipe.ingredients().stream().anyMatch(ingredient -> ingredient.blockId().equals(blockId));
    }
}
