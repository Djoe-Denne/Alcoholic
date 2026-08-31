package com.djden.alcoholic.minecraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Cep of a three-block climbing column. Domain flavor (pruning, lot,
 * growth timing) stays on the concrete plant.
 */
public interface ClimbingColumnRoot {
    Block stemBlock();

    Block canopyBlock();

    InteractionResult useAsRoot(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    );
}
