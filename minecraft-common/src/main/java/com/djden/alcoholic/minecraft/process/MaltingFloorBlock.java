package com.djden.alcoholic.minecraft.process;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public final class MaltingFloorBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 2, 16);
    private final Supplier<? extends BlockEntityType<?>> blockEntityType;

    public MaltingFloorBlock(Properties properties, Supplier<? extends BlockEntityType<?>> blockEntityType) {
        super(properties);
        this.blockEntityType = blockEntityType;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new MaltingFloorBlockEntity(blockEntityType.get(), position, state);
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
                MaltingFloorBlockEntity::serverTick
        );
    }

    @SuppressWarnings("unchecked")
    private BlockEntityType<MaltingFloorBlockEntity> expectedType() {
        return (BlockEntityType<MaltingFloorBlockEntity>) blockEntityType.get();
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
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (!(level.getBlockEntity(position) instanceof MaltingFloorBlockEntity entity)) {
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
            entity.cycleDefinition();
            player.displayClientMessage(entity.status(), true);
            return InteractionResult.CONSUME;
        }
        if (!held.isEmpty() && entity.insert(held)) {
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
                && level.getBlockEntity(position) instanceof MaltingFloorBlockEntity entity) {
            Containers.dropContents(level, position, entity);
        }
        super.onRemove(state, level, position, newState, moved);
    }
}
