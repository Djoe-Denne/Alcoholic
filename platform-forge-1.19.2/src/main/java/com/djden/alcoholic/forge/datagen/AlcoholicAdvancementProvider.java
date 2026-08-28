package com.djden.alcoholic.forge.datagen;

import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.minecraft.advancement.AdvancementHooks;
import com.djden.alcoholic.minecraft.advancement.CropHarvestedTrigger;
import com.djden.alcoholic.minecraft.advancement.MultiblockFormedTrigger;
import com.djden.alcoholic.minecraft.advancement.ProcessCompletedTrigger;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

final class AlcoholicAdvancementProvider extends AdvancementProvider {
    AlcoholicAdvancementProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, existingFileHelper);
    }

    @Override
    protected void registerAdvancements(Consumer<Advancement> saver, ExistingFileHelper files) {
        Advancement root = Advancement.Builder.advancement()
                .display(
                        item("red_grape_cutting"),
                        title("root"),
                        description("root"),
                        ResourceLocation.fromNamespaceAndPath(
                                "minecraft",
                                "textures/gui/advancements/backgrounds/stone.png"
                        ),
                        FrameType.TASK,
                        true,
                        false,
                        false
                )
                .addCriterion("has_red_cutting", InventoryChangeTrigger.TriggerInstance.hasItems(item("red_grape_cutting")))
                .addCriterion("has_white_cutting", InventoryChangeTrigger.TriggerInstance.hasItems(item("white_grape_cutting")))
                .addCriterion("has_hops", InventoryChangeTrigger.TriggerInstance.hasItems(item("hops")))
                .requirements(RequirementsStrategy.OR)
                .save(saver, id("root"), files);

        Advancement.Builder.advancement()
                .parent(root)
                .display(item("red_grapes"), title("harvest_grapes"), description("harvest_grapes"),
                        null, FrameType.TASK, true, true, false)
                .addCriterion("harvest_red", CropHarvestedTrigger.harvested(id("red_grapes")))
                .addCriterion("harvest_white", CropHarvestedTrigger.harvested(id("white_grapes")))
                .requirements(RequirementsStrategy.OR)
                .save(saver, id("harvest_grapes"), files);

        Advancement.Builder.advancement()
                .parent(root)
                .display(item("hops"), title("harvest_hops"), description("harvest_hops"),
                        null, FrameType.TASK, true, true, false)
                .addCriterion("harvest_hops", CropHarvestedTrigger.harvested(id("hops")))
                .save(saver, id("harvest_hops"), files);

        Advancement must = Advancement.Builder.advancement()
                .parent(root)
                .display(item("red_grape_must_bucket"), title("produce_must"), description("produce_must"),
                        null, FrameType.TASK, true, true, false)
                .addCriterion(
                        "press_red_must",
                        ProcessCompletedTrigger.completed(
                                AdvancementHooks.location(BuiltinRegistrations.PRESS),
                                id("red_grape_must")
                        )
                )
                .addCriterion(
                        "press_white_must",
                        ProcessCompletedTrigger.completed(
                                AdvancementHooks.location(BuiltinRegistrations.PRESS),
                                id("white_grape_must")
                        )
                )
                .addCriterion(
                        "mash_wort",
                        ProcessCompletedTrigger.completed(
                                AdvancementHooks.location(BuiltinRegistrations.MASH),
                                id("wort")
                        )
                )
                .requirements(RequirementsStrategy.OR)
                .save(saver, id("produce_must"), files);

        Advancement ferment = Advancement.Builder.advancement()
                .parent(must)
                .display(item("artisanal_fermenter"), title("ferment_beverage"), description("ferment_beverage"),
                        null, FrameType.GOAL, true, true, false)
                .addCriterion(
                        "ferment_red",
                        ProcessCompletedTrigger.completed(
                                AdvancementHooks.location(BuiltinRegistrations.FERMENT),
                                id("young_red_wine")
                        )
                )
                .addCriterion(
                        "ferment_white",
                        ProcessCompletedTrigger.completed(
                                AdvancementHooks.location(BuiltinRegistrations.FERMENT),
                                id("young_white_wine")
                        )
                )
                .addCriterion(
                        "ferment_beer",
                        ProcessCompletedTrigger.completed(
                                AdvancementHooks.location(BuiltinRegistrations.FERMENT),
                                id("beer")
                        )
                )
                .requirements(RequirementsStrategy.OR)
                .save(saver, id("ferment_beverage"), files);

        Advancement.Builder.advancement()
                .parent(ferment)
                .display(item("oak_barrel"), title("age_wine"), description("age_wine"),
                        null, FrameType.GOAL, true, true, false)
                .addCriterion(
                        "age_red",
                        ProcessCompletedTrigger.completed(
                                AdvancementHooks.location(BuiltinRegistrations.AGE),
                                id("red_wine")
                        )
                )
                .addCriterion(
                        "age_white",
                        ProcessCompletedTrigger.completed(
                                AdvancementHooks.location(BuiltinRegistrations.AGE),
                                id("white_wine")
                        )
                )
                .requirements(RequirementsStrategy.OR)
                .save(saver, id("age_wine"), files);

        Advancement.Builder.advancement()
                .parent(ferment)
                .display(item("artisanal_blending_crock"), title("blend"), description("blend"),
                        null, FrameType.TASK, true, true, true)
                .addCriterion(
                        "blend",
                        ProcessCompletedTrigger.completed(AdvancementHooks.location(BuiltinRegistrations.BLEND))
                )
                .save(saver, id("blend"), files);

        Advancement.Builder.advancement()
                .parent(ferment)
                .display(item("beverage_bottle"), title("bottle"), description("bottle"),
                        null, FrameType.CHALLENGE, true, true, false)
                .addCriterion(
                        "bottle",
                        ProcessCompletedTrigger.completed(AdvancementHooks.location(BuiltinRegistrations.BOTTLE))
                )
                .save(saver, id("bottle"), files);

        Advancement industrialRoot = Advancement.Builder.advancement()
                .display(
                        item("industrial_casing"),
                        title("industrial_root"),
                        description("industrial_root"),
                        ResourceLocation.fromNamespaceAndPath(
                                "minecraft",
                                "textures/gui/advancements/backgrounds/stone.png"
                        ),
                        FrameType.TASK,
                        true,
                        false,
                        false
                )
                .addCriterion("has_casing", InventoryChangeTrigger.TriggerInstance.hasItems(item("industrial_casing")))
                .addCriterion(
                        "has_press",
                        InventoryChangeTrigger.TriggerInstance.hasItems(item("industrial_press_controller"))
                )
                .addCriterion(
                        "has_vat",
                        InventoryChangeTrigger.TriggerInstance.hasItems(item("industrial_vat_controller"))
                )
                .addCriterion(
                        "has_tank",
                        InventoryChangeTrigger.TriggerInstance.hasItems(item("industrial_tank_controller"))
                )
                .addCriterion(
                        "has_malt_house",
                        InventoryChangeTrigger.TriggerInstance.hasItems(item("industrial_malt_house_controller"))
                )
                .addCriterion(
                        "has_roller_mill",
                        InventoryChangeTrigger.TriggerInstance.hasItems(item("industrial_roller_mill_controller"))
                )
                .addCriterion(
                        "has_mash_tun",
                        InventoryChangeTrigger.TriggerInstance.hasItems(item("industrial_mash_tun_controller"))
                )
                .addCriterion(
                        "has_kettle",
                        InventoryChangeTrigger.TriggerInstance.hasItems(item("industrial_brewing_kettle_controller"))
                )
                .addCriterion(
                        "has_conditioning",
                        InventoryChangeTrigger.TriggerInstance.hasItems(
                                item("industrial_conditioning_vessel_controller")
                        )
                )
                .requirements(RequirementsStrategy.OR)
                .save(saver, id("industrial_root"), files);

        form(industrialRoot, saver, files, "form_industrial_press", "industrial_press_controller", "industrial_press");
        form(industrialRoot, saver, files, "form_industrial_vat", "industrial_vat_controller", "industrial_fermentation_vat");
        form(industrialRoot, saver, files, "form_industrial_tank", "industrial_tank_controller", "industrial_storage_tank");
        form(
                industrialRoot,
                saver,
                files,
                "form_industrial_malt_house",
                "industrial_malt_house_controller",
                "industrial_malt_house"
        );
        form(
                industrialRoot,
                saver,
                files,
                "form_industrial_roller_mill",
                "industrial_roller_mill_controller",
                "industrial_roller_mill"
        );
        form(
                industrialRoot,
                saver,
                files,
                "form_industrial_mash_tun",
                "industrial_mash_tun_controller",
                "industrial_mash_tun"
        );
        form(
                industrialRoot,
                saver,
                files,
                "form_industrial_kettle",
                "industrial_brewing_kettle_controller",
                "industrial_brewing_kettle"
        );
        form(
                industrialRoot,
                saver,
                files,
                "form_industrial_conditioning",
                "industrial_conditioning_vessel_controller",
                "industrial_conditioning_vessel"
        );
    }

    private static void form(
            Advancement parent,
            Consumer<Advancement> saver,
            ExistingFileHelper files,
            String path,
            String icon,
            String machine
    ) {
        Advancement.Builder.advancement()
                .parent(parent)
                .display(item(icon), title(path), description(path), null, FrameType.GOAL, true, true, false)
                .addCriterion("formed", MultiblockFormedTrigger.formed(id(machine)))
                .save(saver, id(path), files);
    }

    @Override
    public String getName() {
        return "Alcoholic Advancements";
    }

    private static Component title(String path) {
        return Component.translatable("advancements.alcoholic." + path + ".title");
    }

    private static Component description(String path) {
        return Component.translatable("advancements.alcoholic." + path + ".description");
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(AlcoholicIds.MOD_ID, path);
    }

    private static Item item(String path) {
        Item resolved = Registry.ITEM.get(id(path));
        if (resolved == net.minecraft.world.item.Items.AIR) {
            throw new IllegalStateException("Missing datagen item alcoholic:" + path);
        }
        return resolved;
    }
}
