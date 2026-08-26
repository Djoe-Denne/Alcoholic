package com.djden.alcoholic.forge.event;

import com.djden.alcoholic.minecraft.content.AlcoholicContent;
import com.djden.alcoholic.minecraft.content.GrainContent;
import com.djden.alcoholic.minecraft.content.IndustrialContent;
import com.djden.alcoholic.minecraft.content.ProcessingContent;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Operator-only inventory kits for quickly exercising each production chain.
 */
public final class ForgeDebugKitCommands {
    private final DebugKit wineAgriculture;
    private final DebugKit beerAgriculture;
    private final DebugKit wineArtisanal;
    private final DebugKit beerArtisanal;
    private final DebugKit wineIndustrial;
    private final DebugKit beerIndustrial;

    public ForgeDebugKitCommands(
            AlcoholicContent content,
            ProcessingContent processing,
            GrainContent grain,
            IndustrialContent industrial
    ) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(processing, "processing");
        Objects.requireNonNull(grain, "grain");
        Objects.requireNonNull(industrial, "industrial");
        wineAgriculture = kit(
                "wine_agriculture",
                entry(content.redGrapeCutting(), 32),
                entry(content.whiteGrapeCutting(), 32),
                entry(content.vineyardPostItem(), 64),
                entry(content.endPostItem(), 16),
                entry(content.trellisSpool(), 2),
                entry(content.pruningShears(), 1),
                entry(Items.DIRT, 64),
                entry(Items.BONE_MEAL, 64),
                entry(Items.WATER_BUCKET, 1),
                entry(Items.IRON_HOE, 1)
        );
        beerAgriculture = kit(
                "beer_agriculture",
                entry(grain.barleySeeds(), 64),
                entry(grain.hopRhizome(), 32),
                entry(content.vineyardPostItem(), 64),
                entry(content.endPostItem(), 16),
                entry(content.trellisSpool(), 2),
                entry(Items.DIRT, 64),
                entry(Items.BONE_MEAL, 64),
                entry(Items.WATER_BUCKET, 1),
                entry(Items.IRON_HOE, 1)
        );
        wineArtisanal = kit(
                "wine_artisanal",
                entry(processing.artisanalPressItem(), 1),
                entry(processing.artisanalFermenterItem(), 2),
                entry(processing.oakBarrelItem(), 4),
                entry(processing.blendingCrockItem(), 1),
                entry(content.redGrapes(), 64),
                entry(content.whiteGrapes(), 64),
                entry(processing.yeast(), 32),
                entry(processing.emptyBottle(), 32),
                entry(Items.BUCKET, 4),
                registered("alcoholic:red_grape_must_bucket", 1),
                registered("alcoholic:white_grape_must_bucket", 1),
                registered("alcoholic:young_red_wine_bucket", 1),
                registered("alcoholic:young_white_wine_bucket", 1)
        );
        beerArtisanal = kit(
                "beer_artisanal",
                concat(
                        List.of(
                                entry(processing.maltingFloorItem(), 2),
                                entry(processing.maltMillItem(), 1),
                                entry(processing.mashTunItem(), 1),
                                entry(processing.brewingKettleItem(), 1),
                                entry(processing.artisanalFermenterItem(), 1),
                                entry(processing.primitiveCombustionEngineItem(), 1),
                                entry(processing.electricMotorItem(), 1),
                                entry(grain.barley(), 64),
                                entry(grain.maltedBarley(), 64),
                                entry(grain.grist(), 64),
                                entry(grain.hops(), 64),
                                entry(processing.yeast(), 32),
                                entry(processing.emptyBottle(), 32),
                                entry(Items.WATER_BUCKET, 4),
                                entry(Items.COAL, 64),
                                entry(Items.MAGMA_BLOCK, 16),
                                entry(Items.CAMPFIRE, 16)
                        ),
                        createDriveEntries()
                )
        );
        wineIndustrial = kit(
                "wine_industrial",
                concat(
                        List.of(
                                entry(industrial.industrialCasingItem(), 192),
                                entry(industrial.machineWindowItem(), 64),
                                entry(industrial.accessHatchItem(), 16),
                                entry(industrial.fluidPortItem(), 16),
                                entry(industrial.itemPortItem(), 16),
                                entry(industrial.kineticPortItem(), 16),
                                entry(industrial.pressControllerItem(), 1),
                                entry(industrial.vatControllerItem(), 2),
                                entry(industrial.tankControllerItem(), 2),
                                entry(processing.primitiveCombustionEngineItem(), 2),
                                entry(processing.electricMotorItem(), 2),
                                entry(processing.oakBarrelItem(), 4),
                                entry(processing.blendingCrockItem(), 1),
                                entry(content.redGrapes(), 64),
                                entry(content.whiteGrapes(), 64),
                                entry(processing.yeast(), 32),
                                entry(processing.emptyBottle(), 32),
                                entry(Items.BUCKET, 4),
                                entry(Items.COAL, 64),
                                registered("alcoholic:red_grape_must_bucket", 1),
                                registered("alcoholic:white_grape_must_bucket", 1),
                                registered("alcoholic:young_red_wine_bucket", 1),
                                registered("alcoholic:young_white_wine_bucket", 1)
                        ),
                        createDriveEntries()
                )
        );
        beerIndustrial = kit(
                "beer_industrial",
                concat(
                        List.of(
                                entry(industrial.industrialCasingItem(), 256),
                                entry(industrial.machineWindowItem(), 64),
                                entry(industrial.accessHatchItem(), 16),
                                entry(industrial.fluidPortItem(), 16),
                                entry(industrial.itemPortItem(), 16),
                                entry(industrial.kineticPortItem(), 16),
                                entry(industrial.maltHouseControllerItem(), 1),
                                entry(industrial.rollerMillControllerItem(), 1),
                                entry(industrial.mashTunControllerItem(), 1),
                                entry(industrial.brewingKettleControllerItem(), 1),
                                entry(industrial.vatControllerItem(), 1),
                                entry(industrial.conditioningVesselControllerItem(), 1),
                                entry(industrial.tankControllerItem(), 2),
                                entry(processing.primitiveCombustionEngineItem(), 2),
                                entry(processing.electricMotorItem(), 2),
                                entry(grain.barley(), 64),
                                entry(grain.maltedBarley(), 64),
                                entry(grain.grist(), 64),
                                entry(grain.hops(), 64),
                                entry(processing.yeast(), 32),
                                entry(processing.emptyBottle(), 32),
                                entry(Items.WATER_BUCKET, 4),
                                entry(Items.COAL, 64),
                                entry(Items.MAGMA_BLOCK, 16),
                                entry(Items.CAMPFIRE, 16),
                                registered("alcoholic:wort_bucket", 1),
                                registered("alcoholic:hopped_wort_bucket", 1),
                                registered("alcoholic:beer_bucket", 1)
                        ),
                        createDriveEntries()
                )
        );
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("alcoholic")
                        .then(Commands.literal("debug")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("kit")
                                        .then(category("wine", wineAgriculture, wineArtisanal, wineIndustrial))
                                        .then(category("beer", beerAgriculture, beerArtisanal, beerIndustrial))))
        );
    }

    private LiteralArgumentBuilder<CommandSourceStack> category(
            String name,
            DebugKit agriculture,
            DebugKit artisanal,
            DebugKit industrial
    ) {
        return Commands.literal(name)
                .then(Commands.literal("agriculture").executes(context -> give(context, agriculture)))
                .then(Commands.literal("artisanal").executes(context -> give(context, artisanal)))
                .then(Commands.literal("industrial").executes(context -> give(context, industrial)));
    }

    private int give(CommandContext<CommandSourceStack> context, DebugKit kit) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.translatable("command.alcoholic.debug.kit.no_player"));
            return 0;
        }
        int delivered = 0;
        int missing = 0;
        for (KitEntry entry : kit.entries()) {
            Item item = entry.item().get();
            if (item == null || item == Items.AIR) {
                missing++;
                continue;
            }
            int remaining = entry.count();
            int stackLimit = Math.max(1, item.getMaxStackSize());
            while (remaining > 0) {
                int count = Math.min(remaining, stackLimit);
                ItemStack stack = new ItemStack(item, count);
                player.getInventory().add(stack);
                if (!stack.isEmpty()) {
                    player.drop(stack, false);
                }
                delivered += count;
                remaining -= count;
            }
        }
        player.containerMenu.broadcastChanges();
        source.sendSuccess(
                Component.translatable(
                        "command.alcoholic.debug.kit.given",
                        delivered,
                        Component.translatable("command.alcoholic.debug.kit." + kit.id()),
                        missing
                ),
                false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static DebugKit kit(String id, KitEntry... entries) {
        return new DebugKit(id, List.of(entries));
    }

    private static DebugKit kit(String id, List<KitEntry> entries) {
        return new DebugKit(id, entries);
    }

    private static KitEntry entry(Supplier<? extends Item> item, int count) {
        return new KitEntry(item, count);
    }

    private static KitEntry entry(Item item, int count) {
        return entry(() -> item, count);
    }

    private static KitEntry registered(String id, int count) {
        return entry(() -> {
            ResourceLocation key = ResourceLocation.tryParse(id);
            return key == null ? Items.AIR : ForgeRegistries.ITEMS.getValue(key);
        }, count);
    }

    private static List<KitEntry> createDriveEntries() {
        return List.of(
                registered("create:creative_motor", 1),
                registered("create:shaft", 16),
                registered("create:wrench", 1)
        );
    }

    @SafeVarargs
    private static List<KitEntry> concat(List<KitEntry>... groups) {
        List<KitEntry> result = new ArrayList<>();
        for (List<KitEntry> group : groups) {
            result.addAll(group);
        }
        return List.copyOf(result);
    }

    private record DebugKit(String id, List<KitEntry> entries) {
        private DebugKit {
            entries = List.copyOf(entries);
        }
    }

    private record KitEntry(Supplier<? extends Item> item, int count) {
        private KitEntry {
            Objects.requireNonNull(item, "item");
            if (count < 1) {
                throw new IllegalArgumentException("kit item count must be positive");
            }
        }
    }
}
