package com.djden.alcoholic.forge.jei;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.process.ProcessDisplayRecipe;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class JeiProcessSpec {
    private static final Map<ResourceId, List<ResourceId>> CATALYSTS = catalysts();
    private static final Map<ResourceId, JeiProcessSpec> SPECS = new ConcurrentHashMap<>();

    private final ResourceId processType;
    private final RecipeType<ProcessDisplayRecipe> recipeType;
    private final List<ResourceId> catalysts;

    private JeiProcessSpec(ResourceId processType, List<ResourceId> catalysts) {
        this.processType = processType;
        this.recipeType = RecipeType.create(
                processType.namespace(),
                processType.path(),
                ProcessDisplayRecipe.class
        );
        this.catalysts = List.copyOf(catalysts);
    }

    static JeiProcessSpec of(ResourceId processType) {
        return SPECS.computeIfAbsent(
                processType,
                type -> new JeiProcessSpec(type, CATALYSTS.getOrDefault(type, List.of()))
        );
    }

    static List<JeiProcessSpec> allKnown(Iterable<ResourceId> processTypes) {
        List<JeiProcessSpec> specs = new ArrayList<>();
        for (ResourceId type : processTypes) {
            specs.add(of(type));
        }
        return List.copyOf(specs);
    }

    ResourceId processType() {
        return processType;
    }

    RecipeType<ProcessDisplayRecipe> recipeType() {
        return recipeType;
    }

    Component title() {
        if (AlcoholicIds.MOD_ID.equals(processType.namespace())) {
            return Component.translatable("jei.alcoholic.category." + processType.path());
        }
        return Component.translatable("jei.alcoholic.category.addon", processType.toString());
    }

    List<ItemStack> catalystStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (ResourceId id : catalysts) {
            ItemLike item = ForgeRegistries.ITEMS.getValue(JeiIngredients.location(id));
            if (item != null && item.asItem() != net.minecraft.world.item.Items.AIR) {
                stacks.add(new ItemStack(item));
            }
        }
        return stacks;
    }

    private static Map<ResourceId, List<ResourceId>> catalysts() {
        Map<ResourceId, List<ResourceId>> catalysts = new LinkedHashMap<>();
        catalysts.put(BuiltinRegistrations.MILL, List.of(
                AlcoholicIds.MALT_MILL,
                AlcoholicIds.INDUSTRIAL_ROLLER_MILL_CONTROLLER
        ));
        catalysts.put(BuiltinRegistrations.MASH, List.of(
                AlcoholicIds.MASH_TUN,
                AlcoholicIds.INDUSTRIAL_MASH_TUN_CONTROLLER
        ));
        catalysts.put(BuiltinRegistrations.BOIL, List.of(
                AlcoholicIds.BREWING_KETTLE,
                AlcoholicIds.INDUSTRIAL_BREWING_KETTLE_CONTROLLER
        ));
        catalysts.put(BuiltinRegistrations.MALT, List.of(
                AlcoholicIds.MALTING_FLOOR,
                AlcoholicIds.INDUSTRIAL_MALT_HOUSE_CONTROLLER
        ));
        catalysts.put(BuiltinRegistrations.PRESS, List.of(
                AlcoholicIds.ARTISANAL_PRESS,
                AlcoholicIds.INDUSTRIAL_PRESS_CONTROLLER
        ));
        catalysts.put(BuiltinRegistrations.FERMENT, List.of(
                AlcoholicIds.ARTISANAL_FERMENTER,
                AlcoholicIds.INDUSTRIAL_VAT_CONTROLLER
        ));
        catalysts.put(BuiltinRegistrations.AGE, List.of(AlcoholicIds.OAK_BARREL));
        catalysts.put(BuiltinRegistrations.BLEND, List.of(AlcoholicIds.ARTISANAL_BLENDING_CROCK));
        catalysts.put(BuiltinRegistrations.CONDITION, List.of(
                AlcoholicIds.INDUSTRIAL_CONDITIONING_VESSEL_CONTROLLER
        ));
        catalysts.put(BuiltinRegistrations.BOTTLE, List.of(AlcoholicIds.EMPTY_BOTTLE));
        return Map.copyOf(catalysts);
    }
}
