package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.domain.multiblock.CellCoord;
import com.djden.alcoholic.domain.multiblock.IndustrialHullPattern;
import com.djden.alcoholic.domain.multiblock.PartRole;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Objects;

/**
 * Writes {@link IndustrialHullPattern} into the world. Controller is on the
 * −Z face, not the origin corner.
 */
public final class IndustrialHullPlacer {
    private IndustrialHullPlacer() {
    }

    public static BlockPos place(
            Level level,
            BlockPos origin,
            int width,
            int height,
            int depth,
            Block controller,
            Block casing,
            Block window,
            Block hatch,
            Block itemPort,
            Block fluidPort,
            Block kineticPort,
            boolean kinetic
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(controller, "controller");
        Objects.requireNonNull(casing, "casing");
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(hatch, "hatch");
        Objects.requireNonNull(itemPort, "itemPort");
        Objects.requireNonNull(fluidPort, "fluidPort");
        Map<CellCoord, PartRole> shell = IndustrialHullPattern.shell(width, height, depth, kinetic);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    PartRole role = shell.get(new CellCoord(x, y, z));
                    if (role == null) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        continue;
                    }
                    level.setBlock(
                            pos,
                            stateFor(role, controller, casing, window, hatch, itemPort, fluidPort, kineticPort),
                            Block.UPDATE_ALL
                    );
                }
            }
        }
        CellCoord controllerOffset = IndustrialHullPattern.controller(width, height, depth);
        return origin.offset(controllerOffset.x(), controllerOffset.y(), controllerOffset.z());
    }

    private static BlockState stateFor(
            PartRole role,
            Block controller,
            Block casing,
            Block window,
            Block hatch,
            Block itemPort,
            Block fluidPort,
            Block kineticPort
    ) {
        return switch (role) {
            case CONTROLLER -> controller.defaultBlockState();
            case WINDOW -> window.defaultBlockState();
            case HATCH -> hatch.defaultBlockState();
            case ITEM_PORT -> itemPort.defaultBlockState();
            case FLUID_PORT -> fluidPort.defaultBlockState();
            case KINETIC_PORT -> kineticPort.defaultBlockState().setValue(KineticPortBlock.FACING, Direction.EAST);
            case CASING -> casing.defaultBlockState();
        };
    }
}
