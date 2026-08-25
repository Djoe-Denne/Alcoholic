package com.djden.alcoholic.minecraft.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface KineticEntityFactory {
    BlockEntity create(BlockEntityType<?> type, BlockPos position, BlockState state);
}
