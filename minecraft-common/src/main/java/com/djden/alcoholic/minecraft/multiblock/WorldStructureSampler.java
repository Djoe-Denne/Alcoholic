package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.domain.multiblock.CellCoord;
import com.djden.alcoholic.domain.multiblock.PartRole;
import com.djden.alcoholic.domain.multiblock.StructureCell;
import com.djden.alcoholic.domain.multiblock.StructureQuery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class WorldStructureSampler implements StructureQuery {
    private final Level level;

    public WorldStructureSampler(Level level) {
        this.level = Objects.requireNonNull(level, "level");
    }

    @Override
    public StructureCell cell(CellCoord coord) {
        BlockPos position = new BlockPos(coord.x(), coord.y(), coord.z());
        if (!level.hasChunkAt(position)) {
            return StructureCell.unloaded();
        }
        BlockState state = level.getBlockState(position);
        if (state.isAir()) {
            return StructureCell.air();
        }
        Block block = state.getBlock();
        if (!(block instanceof MultiblockPart part)) {
            return StructureCell.obstruction(id(state));
        }
        return StructureCell.structure(part.role(), tags(state), id(state));
    }

    public static Set<String> tags(BlockState state) {
        Set<String> tags = new LinkedHashSet<>();
        add(tags, state, AlcoholicBlockTags.INDUSTRIAL_TANK_CASING, "alcoholic:industrial_tank_casing");
        add(tags, state, AlcoholicBlockTags.FERMENTER_CASING, "alcoholic:fermenter_casing");
        add(tags, state, AlcoholicBlockTags.PRESSURE_SAFE_CASING, "alcoholic:pressure_safe_casing");
        add(tags, state, AlcoholicBlockTags.VALID_MACHINE_WINDOWS, "alcoholic:valid_machine_windows");
        add(tags, state, AlcoholicBlockTags.INDUSTRIAL_PORTS, "alcoholic:industrial_ports");
        return tags;
    }

    private static void add(
            Set<String> tags,
            BlockState state,
            net.minecraft.tags.TagKey<Block> key,
            String id
    ) {
        if (state.is(key)) {
            tags.add(id);
        }
    }

    private static String id(BlockState state) {
        return Registry.BLOCK.getKey(state.getBlock()).toString();
    }

    public static CellCoord coord(BlockPos position) {
        return new CellCoord(position.getX(), position.getY(), position.getZ());
    }

    public static BlockPos pos(CellCoord coord) {
        return new BlockPos(coord.x(), coord.y(), coord.z());
    }
}
