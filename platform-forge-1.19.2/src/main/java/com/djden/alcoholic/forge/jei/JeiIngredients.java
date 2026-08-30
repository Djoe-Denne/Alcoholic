package com.djden.alcoholic.forge.jei;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.ingredient.IngredientSelector;
import com.djden.alcoholic.api.process.ProcessDisplaySpec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

final class JeiIngredients {
    private JeiIngredients() {
    }

    static ResourceLocation location(ResourceId id) {
        return ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path());
    }

    static List<ItemStack> items(ProcessDisplaySpec.ItemPart part) {
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : resolve(part.selector())) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack copy = stack.copy();
            copy.setCount(part.count());
            stacks.add(copy);
        }
        return stacks;
    }

    static FluidStack fluid(ProcessDisplaySpec.FluidPart part) {
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(location(part.fluid()));
        if (fluid == null || fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        int amount = part.millibuckets().orElse(1);
        return new FluidStack(fluid, amount);
    }

    static boolean volumeKnown(ProcessDisplaySpec.FluidPart part) {
        return part.millibuckets().isPresent();
    }

    static int tankCapacity(ProcessDisplaySpec.FluidPart part) {
        return part.millibuckets().orElse(1);
    }

    private static List<ItemStack> resolve(IngredientSelector selector) {
        if (selector instanceof IngredientSelector.Item item) {
            return List.of(itemStack(item.id()));
        }
        if (selector instanceof IngredientSelector.DefinedIngredient ingredient) {
            ItemStack stack = itemStack(ingredient.id());
            if (!stack.isEmpty()) {
                return List.of(stack);
            }
            return List.of();
        }
        if (selector instanceof IngredientSelector.Tag tag) {
            return tagStacks(tag.id());
        }
        if (selector instanceof IngredientSelector.Beverage beverage) {
            return List.of(itemStack(beverage.id()));
        }
        return List.of();
    }

    private static List<ItemStack> tagStacks(ResourceId id) {
        TagKey<Item> tag = TagKey.create(Registry.ITEM_REGISTRY, location(id));
        ItemStack[] stacks = Ingredient.of(tag).getItems();
        List<ItemStack> copies = new ArrayList<>(stacks.length);
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                copies.add(stack.copy());
            }
        }
        return copies;
    }

    static ItemStack stack(ResourceId id) {
        return itemStack(id);
    }

    private static ItemStack itemStack(ResourceId id) {
        Item item = ForgeRegistries.ITEMS.getValue(location(id));
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }
}
