package com.djden.alcoholic.minecraft.bottle;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import net.minecraft.world.item.ItemStack;

/** Stable client-model categories for the NBT-backed beverage bottle item. */
public enum BeverageBottleModel {
    FALLBACK(0.0F),
    RED_WINE(1.0F),
    WHITE_WINE(2.0F),
    BEER(3.0F);

    private final float predicateValue;

    BeverageBottleModel(float predicateValue) {
        this.predicateValue = predicateValue;
    }

    public float predicateValue() {
        return predicateValue;
    }

    public static BeverageBottleModel from(ItemStack stack) {
        return BottleSnapshotNbt.read(stack)
                .flatMap(BottleSnapshotNbt::definition)
                .map(BeverageBottleModel::fromDefinition)
                .orElse(FALLBACK);
    }

    public static BeverageBottleModel fromDefinition(ResourceId definition) {
        if (definition.equals(AlcoholicIds.RED_WINE) || definition.equals(AlcoholicIds.YOUNG_RED_WINE)) {
            return RED_WINE;
        }
        if (definition.equals(AlcoholicIds.WHITE_WINE) || definition.equals(AlcoholicIds.YOUNG_WHITE_WINE)) {
            return WHITE_WINE;
        }
        if (definition.equals(AlcoholicIds.BEER)) {
            return BEER;
        }
        return FALLBACK;
    }
}
