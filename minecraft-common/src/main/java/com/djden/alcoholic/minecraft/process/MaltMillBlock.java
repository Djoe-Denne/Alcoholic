package com.djden.alcoholic.minecraft.process;

import com.djden.alcoholic.minecraft.menu.MachineMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public final class MaltMillBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 14, 15);
    private final Supplier<? extends BlockEntityType<?>> blockEntityType;

    public MaltMillBlock(Properties properties, Supplier<? extends BlockEntityType<?>> blockEntityType) {
        super(properties);
        this.blockEntityType = blockEntityType;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new MaltMillBlockEntity(blockEntityType.get(), position, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return level.isClientSide ? null : createTickerHelper(
                type,
                expectedType(),
                MaltMillBlockEntity::serverTick
        );
    }

    @SuppressWarnings("unchecked")
    private BlockEntityType<MaltMillBlockEntity> expectedType() {
        return (BlockEntityType<MaltMillBlockEntity>) blockEntityType.get();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos position, RandomSource random) {
        if (!(level.getBlockEntity(position) instanceof MaltMillBlockEntity entity)
                || !entity.visualGrinding()) {
            return;
        }
        ItemStack input = entity.getItem(MaltMillBlockEntity.INPUT_SLOT);
        if (input.isEmpty()) {
            return;
        }

        Direction facing = state.getValue(FACING);
        Direction lateral = facing.getClockWise();
        double lateralOffset = (random.nextDouble() - 0.5) * 0.20;
        double x = position.getX() + 0.5
                + facing.getStepX() * 0.28
                + lateral.getStepX() * lateralOffset;
        double y = position.getY() + 0.40 + random.nextDouble() * 0.04;
        double z = position.getZ() + 0.5
                + facing.getStepZ() * 0.28
                + lateral.getStepZ() * lateralOffset;
        double forwardSpeed = 0.01 + random.nextDouble() * 0.01;
        double lateralSpeed = (random.nextDouble() - 0.5) * 0.01;

        ItemStack particleStack = input.copy();
        particleStack.setCount(1);
        level.addParticle(
                new ItemParticleOption(ParticleTypes.ITEM, particleStack),
                x,
                y,
                z,
                facing.getStepX() * forwardSpeed + lateral.getStepX() * lateralSpeed,
                -0.02 - random.nextDouble() * 0.01,
                facing.getStepZ() * forwardSpeed + lateral.getStepZ() * lateralSpeed
        );
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
        if (!(level.getBlockEntity(position) instanceof MaltMillBlockEntity entity)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            ItemStack extracted = entity.extractOutput();
            if (!extracted.isEmpty()) {
                if (!player.getInventory().add(extracted)) {
                    player.drop(extracted, false);
                }
                return InteractionResult.CONSUME;
            }
        }
        if (!held.isEmpty() && entity.insert(held)) {
            return InteractionResult.CONSUME;
        }
        if (held.isEmpty() && MachineMenus.tryOpen(player, entity)) {
            return InteractionResult.CONSUME;
        }
        player.displayClientMessage(entity.status(), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(
            BlockState state,
            Level level,
            BlockPos position,
            BlockState newState,
            boolean moved
    ) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(position) instanceof MaltMillBlockEntity entity) {
            Containers.dropContents(level, position, entity);
        }
        super.onRemove(state, level, position, newState, moved);
    }
}
