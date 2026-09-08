package com.djden.alcoholic.forge.client;

import com.djden.alcoholic.minecraft.bottle.BeverageBottleModel;
import com.djden.alcoholic.minecraft.content.ProcessingContent;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

/** Registers the NBT-driven item-model selector for every beverage bottle. */
public final class BeverageBottleClient {
    private static final ResourceLocation MODEL = new ResourceLocation("alcoholic", "bottle_style");

    private BeverageBottleClient() {
    }

    public static void register(ProcessingContent processing) {
        ItemProperties.register(
                processing.beverageBottle().get(),
                MODEL,
                (stack, level, entity, seed) -> BeverageBottleModel.from(stack).predicateValue()
        );
    }
}
