package com.djden.alcoholic.minecraft.process;

import com.djden.alcoholic.minecraft.menu.MachineMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public final class MashTunBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 14, 15);
    private final Supplier<? extends BlockEntityType<?>> blockEntityType;

    public MashTunBlock(Properties properties, Supplier<? extends BlockEntityType<?>> blockEntityType) {
        super(properties);
        this.blockEntityType = blockEntityType;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, false));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new MashTunBlockEntity(blockEntityType.get(), position, state);
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
                MashTunBlockEntity::serverTick
        );
    }

    @SuppressWarnings("unchecked")
    private BlockEntityType<MashTunBlockEntity> expectedType() {
        return (BlockEntityType<MashTunBlockEntity>) blockEntityType.get();
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
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
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
        if (!(level.getBlockEntity(position) instanceof MashTunBlockEntity entity)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            ItemStack extracted = entity.extractByproduct();
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
                && level.getBlockEntity(position) instanceof MashTunBlockEntity entity) {
            Containers.dropContents(level, position, entity);
        }
        super.onRemove(state, level, position, newState, moved);
    }
}
