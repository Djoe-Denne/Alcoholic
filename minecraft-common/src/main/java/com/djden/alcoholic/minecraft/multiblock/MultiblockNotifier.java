package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.minecraft.advancement.AdvancementHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Event-driven dirty mark. Never scans on a capability request.
 */
public final class MultiblockNotifier {
    public static final int MAX_WIDTH = 9;
    public static final int MAX_HEIGHT = 16;
    public static final int MAX_DEPTH = 9;

    private MultiblockNotifier() {
    }

    public static void notifyNearby(Level level, BlockPos origin) {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockPos.betweenClosed(
                origin.offset(-MAX_WIDTH, -MAX_HEIGHT, -MAX_DEPTH),
                origin.offset(MAX_WIDTH, MAX_HEIGHT, MAX_DEPTH)
        ).forEach(position -> {
            if (!level.hasChunkAt(position)) {
                return;
            }
            BlockEntity entity = level.getBlockEntity(position);
            if (entity instanceof MultiblockControllerBlockEntity controller) {
                controller.markStructureDirty();
            }
        });
    }

    public static void touchNearby(Player player, Level level, BlockPos origin) {
        if (player == null || level == null || level.isClientSide) {
            return;
        }
        BlockPos.betweenClosed(
                origin.offset(-MAX_WIDTH, -MAX_HEIGHT, -MAX_DEPTH),
                origin.offset(MAX_WIDTH, MAX_HEIGHT, MAX_DEPTH)
        ).forEach(position -> {
            if (!level.hasChunkAt(position)) {
                return;
            }
            BlockEntity entity = level.getBlockEntity(position);
            if (entity instanceof MultiblockControllerBlockEntity) {
                AdvancementHooks.touch(player, entity);
            }
        });
    }
}
