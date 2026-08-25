package com.djden.alcoholic.integration.create.forge;

import com.djden.alcoholic.domain.multiblock.PartRole;
import com.djden.alcoholic.minecraft.multiblock.MultiblockNotifier;
import com.djden.alcoholic.minecraft.multiblock.MultiblockPart;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import java.util.function.Supplier;

/**
 * Create-backed kinetic port. Shafts connect on {@code FACING}.
 * Loaded only when Create is present.
 */
public final class CreateKineticPortBlock extends KineticBlock implements IRotate, EntityBlock, MultiblockPart {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private final Supplier<? extends BlockEntityType<?>> type;

    public CreateKineticPortBlock(Properties properties, Supplier<? extends BlockEntityType<?>> type) {
        super(properties);
        this.type = type;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public PartRole role() {
        return PartRole.KINETIC_PORT;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new CreateKineticPortBlockEntity(type.get(), position, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return (world, position, blockState, entity) -> {
            if (entity instanceof KineticBlockEntity kinetic) {
                kinetic.tick();
            }
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
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
