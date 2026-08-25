package com.djden.alcoholic.forge.condition;

import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Datapack condition that is true when a concrete item id is registered.
 */
public final class ItemPresentCondition implements ICondition {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(AlcoholicIds.MOD_ID, "item_present");

    private final ResourceLocation item;

    public ItemPresentCondition(ResourceLocation item) {
        this.item = item;
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        Item value = ForgeRegistries.ITEMS.getValue(item);
        return value != null && value != Items.AIR;
    }

    public static final class Serializer implements IConditionSerializer<ItemPresentCondition> {
        @Override
        public void write(JsonObject json, ItemPresentCondition value) {
            json.addProperty("item", value.item.toString());
        }

        @Override
        public ItemPresentCondition read(JsonObject json) {
            ResourceLocation item = ResourceLocation.tryParse(GsonHelper.getAsString(json, "item"));
            if (item == null) {
                throw new IllegalArgumentException("item_present requires a valid item id");
            }
            return new ItemPresentCondition(item);
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
