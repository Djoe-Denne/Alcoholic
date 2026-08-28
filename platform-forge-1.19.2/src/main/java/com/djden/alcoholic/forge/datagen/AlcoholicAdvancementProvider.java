package com.djden.alcoholic.forge.datagen;

import com.djden.alcoholic.application.progression.ProgressionCatalog;
import com.djden.alcoholic.application.progression.ProgressionCriterion;
import com.djden.alcoholic.application.progression.ProgressionFrame;
import com.djden.alcoholic.application.progression.ProgressionNode;
import com.djden.alcoholic.minecraft.advancement.AdvancementHooks;
import com.djden.alcoholic.minecraft.advancement.CropHarvestedTrigger;
import com.djden.alcoholic.minecraft.advancement.MultiblockFormedTrigger;
import com.djden.alcoholic.minecraft.advancement.ProcessCompletedTrigger;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

final class AlcoholicAdvancementProvider extends AdvancementProvider {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            "minecraft",
            "textures/gui/advancements/backgrounds/stone.png"
    );

    AlcoholicAdvancementProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, existingFileHelper);
    }

    @Override
    protected void registerAdvancements(Consumer<Advancement> saver, ExistingFileHelper files) {
        ProgressionCatalog catalog = ProgressionCatalog.official();
        Map<String, Advancement> built = new LinkedHashMap<>();
        for (ProgressionNode node : catalog.nodes()) {
            Advancement.Builder builder = Advancement.Builder.advancement();
            node.vanillaParentId().ifPresent(parent -> builder.parent(require(built, parent, node.id())));
            builder.display(
                    item(node.icon()),
                    title(node.id()),
                    description(node.id()),
                    node.vanillaParentId().isEmpty() ? BACKGROUND : null,
                    frame(node.frame()),
                    node.toast(),
                    node.announceToChat(),
                    node.hidden()
            );
            for (ProgressionCriterion criterion : node.criteria()) {
                builder.addCriterion(criterion.name(), trigger(node, criterion));
            }
            if (node.criteria().size() > 1) {
                builder.requirements(RequirementsStrategy.OR);
            }
            built.put(node.id(), builder.save(saver, id(node.id()), files));
        }
    }

    @Override
    public String getName() {
        return "Alcoholic Advancements";
    }

    private static Advancement require(Map<String, Advancement> built, String parent, String child) {
        Advancement advancement = built.get(parent);
        if (advancement == null) {
            throw new IllegalStateException("Parent " + parent + " must be declared before " + child);
        }
        return advancement;
    }

    private static CriterionTriggerInstance trigger(ProgressionNode node, ProgressionCriterion criterion) {
        return switch (node.trigger()) {
            case INVENTORY -> InventoryChangeTrigger.TriggerInstance.hasItems(
                    item(criterion.item().orElseThrow().path())
            );
            case HARVEST -> CropHarvestedTrigger.harvested(location(criterion.crop().orElseThrow()));
            case PROCESS -> ProcessCompletedTrigger.completed(
                    location(criterion.process().orElseThrow()),
                    criterion.liquid().map(AlcoholicAdvancementProvider::location).orElse(null)
            );
            case FORMED -> MultiblockFormedTrigger.formed(location(criterion.machine().orElseThrow()));
        };
    }

    private static FrameType frame(ProgressionFrame frame) {
        return switch (frame) {
            case TASK -> FrameType.TASK;
            case GOAL -> FrameType.GOAL;
            case CHALLENGE -> FrameType.CHALLENGE;
        };
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

    private static ResourceLocation location(com.djden.alcoholic.api.ResourceId resource) {
        return AdvancementHooks.location(resource);
    }

    private static Item item(String path) {
        Item resolved = Registry.ITEM.get(id(path));
        if (resolved == net.minecraft.world.item.Items.AIR) {
            throw new IllegalStateException("Missing datagen item alcoholic:" + path);
        }
        return resolved;
    }
}
