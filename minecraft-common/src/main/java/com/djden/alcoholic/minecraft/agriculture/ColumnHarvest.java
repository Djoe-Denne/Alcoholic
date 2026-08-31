package com.djden.alcoholic.minecraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Whole-column pick. The plant stays. Items go to the clicking player.
 */
public interface ColumnHarvest {
    boolean harvestColumn(
            Player player,
            Level level,
            BlockPos rootPos,
            BlockState rootState
    );
}
