package com.djden.alcoholic.forge.jei;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

final class JeiIngredientGuides {
    private JeiIngredientGuides() {
    }

    record Guide(List<ResourceId> items, List<String> keys) {
    }

    static List<Guide> all() {
        return List.of(
                new Guide(
                        List.of(
                                AlcoholicIds.RED_GRAPES,
                                AlcoholicIds.WHITE_GRAPES,
                                AlcoholicIds.RED_GRAPE_CUTTING,
                                AlcoholicIds.WHITE_GRAPE_CUTTING,
                                AlcoholicIds.PRUNING_SHEARS
                        ),
                        List.of(
                                "jei.alcoholic.info.grapevine.find",
                                "jei.alcoholic.info.grapevine.grow",
                                "jei.alcoholic.info.grapevine.harvest"
                        )
                ),
                new Guide(
                        List.of(AlcoholicIds.HOPS, AlcoholicIds.HOP_RHIZOME),
                        List.of(
                                "jei.alcoholic.info.hops.find",
                                "jei.alcoholic.info.hops.grow",
                                "jei.alcoholic.info.hops.harvest"
                        )
                ),
                new Guide(
                        List.of(AlcoholicIds.BARLEY, AlcoholicIds.BARLEY_SEEDS),
                        List.of(
                                "jei.alcoholic.info.barley.find",
                                "jei.alcoholic.info.barley.grow"
                        )
                ),
                new Guide(
                        List.of(
                                AlcoholicIds.VINEYARD_POST,
                                AlcoholicIds.END_POST,
                                AlcoholicIds.TRELLIS_SPOOL
                        ),
                        List.of("jei.alcoholic.info.trellis")
                )
        );
    }

    static void register(IRecipeRegistration registration) {
        for (Guide guide : all()) {
            List<ItemStack> stacks = new ArrayList<>();
            for (ResourceId id : guide.items()) {
                ItemStack stack = JeiIngredients.stack(id);
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
            if (stacks.isEmpty()) {
                continue;
            }
            Component[] descriptions = new Component[guide.keys().size()];
            for (int i = 0; i < guide.keys().size(); i++) {
                descriptions[i] = Component.translatable(guide.keys().get(i));
            }
            registration.addItemStackInfo(stacks, descriptions);
        }
    }
}
