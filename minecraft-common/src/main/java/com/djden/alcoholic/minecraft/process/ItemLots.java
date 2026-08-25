package com.djden.alcoholic.minecraft.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.ingredient.IngredientLot;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.minecraft.viticulture.HarvestLotNbt;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

public final class ItemLots {
    private static final ResourceId SUGAR = ResourceId.parse("alcoholic:sugar");
    private static final ResourceId ACIDITY = ResourceId.parse("alcoholic:acidity");
    private static final ResourceId QUALITY = ResourceId.parse("alcoholic:quality");
    private static final ResourceId VARIETY = ResourceId.parse("alcoholic:variety");

    private ItemLots() {
    }

    public static ResourceId id(ItemStack stack) {
        ResourceLocation location = Registry.ITEM.getKey(stack.getItem());
        return new ResourceId(location.getNamespace(), location.getPath());
    }

    public static IngredientLot lot(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        PropertyBag properties = PropertyBag.empty();
        Optional<HarvestLotNbt.HarvestLot> harvest = HarvestLotNbt.read(stack);
        if (harvest.isPresent()) {
            HarvestLotNbt.HarvestLot lot = harvest.orElseThrow();
            properties = properties
                    .with(SUGAR, lot.sugar())
                    .with(ACIDITY, lot.acidity())
                    .with(QUALITY, lot.quality())
                    .with(VARIETY, lot.variety().toString());
        }
        Optional<PropertyBag> solid = SolidPropertyNbt.read(stack);
        if (solid.isPresent()) {
            for (ResourceId id : solid.orElseThrow().ids()) {
                properties = properties.with(id, solid.orElseThrow().get(id).orElseThrow());
            }
        }
        return new IngredientLot(id(stack), stack.getCount(), properties);
    }
}
