package com.djden.alcoholic.forge.event;

import com.djden.alcoholic.application.compatibility.CompatibilitySnapshot;
import com.djden.alcoholic.application.compatibility.KnownMod;
import com.djden.alcoholic.integration.vinery.VineryIntegration;
import com.djden.alcoholic.minecraft.agriculture.TrellisDetector;
import com.djden.alcoholic.minecraft.agriculture.VineBlock;
import com.djden.alcoholic.minecraft.agriculture.VineStage;
import com.djden.alcoholic.minecraft.content.AlcoholicContent;
import com.djden.alcoholic.domain.viticulture.PruningLevel;
import com.djden.alcoholic.minecraft.viticulture.HarvestLotNbt;
import com.djden.alcoholic.minecraft.viticulture.ViticultureDataReloadListener;
import com.djden.alcoholic.minecraft.viticulture.ViticultureRuntime;
import com.djden.alcoholic.api.ResourceId;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Objects;

/**
 * Forge-only bridges for optional Vinery input and shared viticulture data.
 *
 * <p>No Vinery classes are referenced: registry IDs remain the integration
 * boundary, so the handler is safe when the optional mod is absent.</p>
 */
public final class ForgeViticultureEvents {
    private final CompatibilitySnapshot compatibility;
    private final AlcoholicContent content;
    private final ViticultureRuntime runtime;

    public ForgeViticultureEvents(
            CompatibilitySnapshot compatibility,
            AlcoholicContent content,
            ViticultureRuntime runtime
    ) {
        this.compatibility = Objects.requireNonNull(compatibility, "compatibility");
        this.content = Objects.requireNonNull(content, "content");
        this.runtime = Objects.requireNonNull(runtime, "runtime");
    }

    @SubscribeEvent
    public void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new ViticultureDataReloadListener(runtime));
    }

    @SubscribeEvent
    public void plantVinerySeed(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide || !compatibility.isPresent(KnownMod.VINERY)) {
            return;
        }

        Block vine = vineFor(event.getItemStack());
        if (vine == null) {
            return;
        }

        BlockPos placementPosition = event.getPos().above();
        if (!level.isEmptyBlock(placementPosition)
                || !level.getBlockState(placementPosition.below()).is(BlockTags.DIRT)
                || !TrellisDetector.shared().isTrained(level, placementPosition)) {
            return;
        }

        BlockState planted = vine.defaultBlockState()
                .setValue(VineBlock.STAGE, VineStage.PLANTED)
                .setValue(VineBlock.TRAINED, true)
                .setValue(VineBlock.AGE, 0);
        if (!level.setBlock(placementPosition, planted, Block.UPDATE_ALL)) {
            return;
        }

        if (!event.getEntity().getAbilities().instabuild) {
            event.getItemStack().shrink(1);
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void addHarvestLotTooltip(ItemTooltipEvent event) {
        HarvestLotNbt.read(event.getItemStack()).ifPresent(lot -> {
            event.getToolTip().add(qualitativeLine(
                    "tooltip.alcoholic.harvest_lot.quality",
                    "tooltip.alcoholic.harvest_lot.quality.",
                    qualityBand(lot.quality())
            ).copy().append(
                    Component.literal(" (" + Math.round(lot.quality() * 100.0) + "%)")
                            .withStyle(ChatFormatting.DARK_GRAY)
            ));
            event.getToolTip().add(qualitativeLine(
                    "tooltip.alcoholic.harvest_lot.sugar",
                    "tooltip.alcoholic.harvest_lot.level.",
                    levelBand(lot.sugar())
            ));
            event.getToolTip().add(qualitativeLine(
                    "tooltip.alcoholic.harvest_lot.acidity",
                    "tooltip.alcoholic.harvest_lot.level.",
                    levelBand(lot.acidity())
            ));
            if (lot.pruningLevel() != null) {
                event.getToolTip().add(
                        Component.translatable(
                                "tooltip.alcoholic.harvest_lot.pruning",
                                Component.translatable(pruningKey(lot.pruningLevel()))
                        ).withStyle(ChatFormatting.GRAY)
                );
            }
        });
    }

    private Block vineFor(ItemStack stack) {
        ResourceLocation id = Registry.ITEM.getKey(stack.getItem());
        if (matches(id, VineryIntegration.RED_GRAPE_SEEDS)) {
            return content.redGrapevine().get();
        }
        if (matches(id, VineryIntegration.WHITE_GRAPE_SEEDS)) {
            return content.whiteGrapevine().get();
        }
        return null;
    }

    private static boolean matches(ResourceLocation actual, ResourceId expected) {
        return actual != null
                && actual.getNamespace().equals(expected.namespace())
                && actual.getPath().equals(expected.path());
    }

    private static Component qualitativeLine(
            String labelKey,
            String valueKeyPrefix,
            String band
    ) {
        return Component.translatable(
                labelKey,
                Component.translatable(valueKeyPrefix + band)
        ).withStyle(ChatFormatting.GRAY);
    }

    private static String qualityBand(double value) {
        if (value >= 0.85) {
            return "exceptional";
        }
        if (value >= 0.65) {
            return "good";
        }
        if (value >= 0.40) {
            return "average";
        }
        return "poor";
    }

    private static String levelBand(double value) {
        if (value >= 0.70) {
            return "high";
        }
        if (value >= 0.40) {
            return "balanced";
        }
        return "low";
    }

    private static String pruningKey(PruningLevel level) {
        return switch (level) {
            case LIGHT -> "message.alcoholic.vine.pruning.light";
            case BALANCED -> "message.alcoholic.vine.pruning.balanced";
            case SEVERE -> "message.alcoholic.vine.pruning.severe";
        };
    }
}
