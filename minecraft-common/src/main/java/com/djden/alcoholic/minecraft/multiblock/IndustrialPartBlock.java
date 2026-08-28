package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.domain.multiblock.FormedHullKit;
import com.djden.alcoholic.domain.multiblock.PartRole;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class IndustrialPartBlock extends Block implements MultiblockPart {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    private final PartRole role;

    public IndustrialPartBlock(Properties properties, PartRole role) {
        super(properties);
        this.role = role;
        if (stateDefinition.getProperties().contains(FORMED)) {
            registerDefaultState(stateDefinition.any().setValue(FORMED, false));
        }
    }

    @Override
    public PartRole role() {
        return role;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        boolean formed = state.hasProperty(FORMED) && state.getValue(FORMED);
        if (FormedHullKit.hideCasingCube(role, formed)) {
            return RenderShape.INVISIBLE;
        }
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos position, BlockState oldState, boolean moved) {
        super.onPlace(state, level, position, oldState, moved);
        if (oldState.is(state.getBlock())) {
            return;
        }
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
