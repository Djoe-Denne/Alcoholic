package com.djden.alcoholic.minecraft.advancement;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class AdvancementHooks {
    private AdvancementHooks() {
    }

    public static void register() {
        AlcoholicCriteria.register();
    }

    public static void touch(Player player, @Nullable BlockEntity entity) {
        if (player.level.isClientSide || !(entity instanceof AdvancementActor actor)) {
            return;
        }
        actor.advancementState().touch(player);
        entity.setChanged();
    }

    public static void harvest(Player player, ResourceLocation crop) {
        if (player instanceof ServerPlayer server) {
            AlcoholicCriteria.CROP_HARVESTED.trigger(server, crop);
        }
    }

    public static void bottled(Player player, ResourceId liquid) {
        if (player instanceof ServerPlayer server) {
            AlcoholicCriteria.PROCESS_COMPLETED.trigger(
                    server,
                    location(BuiltinRegistrations.BOTTLE),
                    location(liquid)
            );
        }
    }

    public static void processCompleted(BlockEntity entity, ResourceId process, Optional<ResourceId> liquid) {
        if (!(entity instanceof AdvancementActor actor)
                || entity.getLevel() == null
                || entity.getLevel().isClientSide) {
            return;
        }
        actor.advancementState().complete(
                entity.getLevel(),
                location(process),
                liquid.map(AdvancementHooks::location).orElse(null)
        );
    }

    public static void multiblockFormed(BlockEntity entity, ResourceId machine) {
        if (!(entity instanceof AdvancementActor actor)
                || entity.getLevel() == null
                || entity.getLevel().isClientSide) {
            return;
        }
        actor.advancementState().formed(entity.getLevel(), entity.getBlockPos(), location(machine));
    }

    public static Optional<ResourceId> changedIdentity(LiquidBatch before, LiquidBatch after) {
        Optional<ResourceId> next = after.baseLiquid();
        if (next.isPresent() && !next.equals(before.baseLiquid())) {
            return next;
        }
        return Optional.empty();
    }

    public static ResourceLocation location(ResourceId id) {
        return ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path());
    }
}
