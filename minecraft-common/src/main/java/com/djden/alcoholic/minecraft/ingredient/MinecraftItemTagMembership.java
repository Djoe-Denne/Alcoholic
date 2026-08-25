package com.djden.alcoholic.minecraft.ingredient;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.platform.api.TagMembershipPort;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class MinecraftItemTagMembership implements TagMembershipPort<ItemStack> {
    @Override
    public boolean isIn(ItemStack candidate, ResourceId tagId) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(
                tagId.namespace(),
                tagId.path()
        );
        TagKey<Item> tag = TagKey.create(Registry.ITEM_REGISTRY, location);
        return candidate.is(tag);
    }
}
