package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.domain.multiblock.PartRole;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

public final class FluidPortBlock extends BaseEntityBlock implements MultiblockPart {
    private final Supplier<? extends BlockEntityType<?>> type;

    public FluidPortBlock(Properties properties, Supplier<? extends BlockEntityType<?>> type) {
        super(properties);
        this.type = type;
        registerDefaultState(PortBlocks.defaultState(this));
    }

    @Override
    public PartRole role() {
        return PartRole.FLUID_PORT;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new FluidPortBlockEntity(type.get(), position, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return PortBlocks.placement(this, context);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return PortBlocks.rotate(state, rotation);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return PortBlocks.mirror(state, mirror);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        PortBlocks.states(builder);
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        return PortBlocks.use(level, position, state, player, hand, hit);
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
