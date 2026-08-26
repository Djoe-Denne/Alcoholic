package com.djden.alcoholic.minecraft.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Objects;

/**
 * Places a hollow cuboid shell. Controller sits at the origin corner; an
 * optional extra port occupies the +X, y=0, z=0 shell cell. No beverage names.
 */
public final class HollowCuboidPlacer {
    private HollowCuboidPlacer() {
    }

    public static BlockPos place(
            Level level,
            BlockPos origin,
            int width,
            int height,
            int depth,
            Block controller,
            Block casing,
            Block extraPort
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(controller, "controller");
        Objects.requireNonNull(casing, "casing");
        if (width < 3 || height < 3 || depth < 3) {
            throw new IllegalArgumentException("hollow cuboid must be at least 3x3x3");
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    boolean shell = x == 0 || y == 0 || z == 0
                            || x == width - 1 || y == height - 1 || z == depth - 1;
                    BlockPos pos = origin.offset(x, y, z);
                    if (!shell) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    } else if (x == 0 && y == 0 && z == 0) {
                        level.setBlock(pos, controller.defaultBlockState(), Block.UPDATE_ALL);
                    } else if (extraPort != null && x == width - 1 && y == 0 && z == 0) {
                        level.setBlock(pos, extraPort.defaultBlockState(), Block.UPDATE_ALL);
                    } else {
                        level.setBlock(pos, casing.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
        return origin;
    }

    public static boolean formNow(Level level, BlockPos controllerPos) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(controllerPos, "controllerPos");
        if (!(level.getBlockEntity(controllerPos) instanceof MultiblockControllerBlockEntity entity)) {
            return false;
        }
        entity.markStructureDirty();
        MultiblockControllerBlockEntity.tick(level, controllerPos, entity.getBlockState(), entity);
        return entity.formed();
    }
}
