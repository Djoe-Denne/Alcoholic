package com.djden.alcoholic.minecraft.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.ingredient.IngredientSelector;
import com.djden.alcoholic.application.process.SelectorMatcher;
import com.djden.alcoholic.minecraft.beverage.BeverageRuntime;
import com.djden.alcoholic.minecraft.ingredient.MinecraftItemTagMembership;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class MinecraftSelectorMatcher {
    private static final MinecraftItemTagMembership MEMBERSHIP = new MinecraftItemTagMembership();

    private MinecraftSelectorMatcher() {
    }

    public static SelectorMatcher create(BeverageRuntime runtime) {
        return (selector, item) -> matches(runtime, selector, item);
    }

    private static boolean matches(BeverageRuntime runtime, IngredientSelector selector, ResourceId item) {
        ItemStack stack = stack(item);
        if (stack.isEmpty()) {
            return false;
        }
        if (selector instanceof IngredientSelector.Item value) {
            return value.id().equals(item);
        }
        if (selector instanceof IngredientSelector.Tag value) {
            return MEMBERSHIP.isIn(stack, value.id());
        }
        if (selector instanceof IngredientSelector.DefinedIngredient value) {
            if (value.id().equals(item)) {
                return true;
            }
            return runtime.catalog().ingredient(value.id())
                    .map(definition -> definition.tags().stream().anyMatch(tag -> MEMBERSHIP.isIn(stack, tag)))
                    .orElse(false);
        }
        return false;
    }

    private static ItemStack stack(ResourceId item) {
        Item resolved = Registry.ITEM.get(ResourceLocation.fromNamespaceAndPath(item.namespace(), item.path()));
        return resolved == Items.AIR ? ItemStack.EMPTY : new ItemStack(resolved);
    }
}
