package com.djden.alcoholic.minecraft.process;

import com.djden.alcoholic.api.ResourceId;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class MachineItemStacks {
    private MachineItemStacks() {
    }

    static ItemStack stack(ResourceId id, int amount) {
        var item = Registry.ITEM.get(ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path()));
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, amount);
    }

    static ItemStack copyCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }
}
