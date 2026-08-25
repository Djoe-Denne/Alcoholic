package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.domain.multiblock.PartRole;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class IndustrialPartBlock extends Block implements MultiblockPart {
    private final PartRole role;

    public IndustrialPartBlock(Properties properties, PartRole role) {
        super(properties);
        this.role = role;
    }

    @Override
    public PartRole role() {
        return role;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos position, BlockState oldState, boolean moved) {
        super.onPlace(state, level, position, oldState, moved);
        MultiblockNotifier.notifyNearby(level, position);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos position, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            MultiblockNotifier.notifyNearby(level, position);
        }
        super.onRemove(state, level, position, newState, moved);
    }
}
