package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.multiblock.PartRole;
import com.djden.alcoholic.minecraft.bottle.Bottling;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

public final class MultiblockControllerBlock extends BaseEntityBlock implements MultiblockPart {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    private final Supplier<? extends BlockEntityType<?>> type;
    private final ResourceId definitionId;

    public MultiblockControllerBlock(
            Properties properties,
            Supplier<? extends BlockEntityType<?>> type,
            ResourceId definitionId
    ) {
        super(properties);
        this.type = type;
        this.definitionId = definitionId;
        registerDefaultState(stateDefinition.any().setValue(FORMED, false));
    }

    public ResourceId definitionId() {
        return definitionId;
    }

    @Override
    public PartRole role() {
        return PartRole.CONTROLLER;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new MultiblockControllerBlockEntity(type.get(), position, state, definitionId);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return createTickerHelper(type, expected(), MultiblockControllerBlockEntity::tick);
    }

    @SuppressWarnings("unchecked")
    private BlockEntityType<MultiblockControllerBlockEntity> expected() {
        return (BlockEntityType<MultiblockControllerBlockEntity>) type.get();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED);
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
        if (!(level.getBlockEntity(position) instanceof MultiblockControllerBlockEntity entity)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && held.isEmpty() && entity.cycleBoundDefinition()) {
            player.displayClientMessage(entity.status(), true);
            return InteractionResult.CONSUME;
        }
        if (Bottling.bottle(player, held, entity.tank())) {
            entity.onTankChanged();
            return InteractionResult.CONSUME;
        }
        if (!held.isEmpty() && entity.insert(held)) {
            return InteractionResult.CONSUME;
        }
        player.displayClientMessage(entity.status(), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos position, BlockState oldState, boolean moved) {
        super.onPlace(state, level, position, oldState, moved);
        if (level.getBlockEntity(position) instanceof MultiblockControllerBlockEntity controller) {
            controller.markStructureDirty();
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos position, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(position) instanceof MultiblockControllerBlockEntity entity) {
            entity.resetProcess();
            Containers.dropContents(level, position, entity);
            entity.clearContent();
            ItemStack stack = new ItemStack(this);
            entity.saveToItem(stack);
            popResource(level, position, stack);
        }
        super.onRemove(state, level, position, newState, moved);
        MultiblockNotifier.notifyNearby(level, position);
    }
}
