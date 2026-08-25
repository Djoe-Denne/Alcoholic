package com.djden.alcoholic.minecraft.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class PortBlocks {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<ConfiguredPortMode> MODE =
            EnumProperty.create("mode", ConfiguredPortMode.class);

    private PortBlocks() {
    }

    public static BlockState defaultState(Block block) {
        return block.defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(MODE, ConfiguredPortMode.BOTH);
    }

    public static BlockState placement(Block block, BlockPlaceContext context) {
        return block.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(MODE, ConfiguredPortMode.BOTH);
    }

    public static void states(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MODE);
    }

    public static BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    public static BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    public static InteractionResult cycleMode(
            Level level,
            BlockPos position,
            BlockState state,
            Player player,
            InteractionHand hand
    ) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ItemStack held = player.getItemInHand(hand);
        boolean wrench = "create:wrench".equals(Registry.ITEM.getKey(held.getItem()).toString());
        boolean sneakEmpty = player.isShiftKeyDown() && held.isEmpty();
        if (!wrench && !sneakEmpty) {
            return InteractionResult.PASS;
        }
        ConfiguredPortMode next = state.getValue(MODE).next();
        level.setBlock(position, state.setValue(MODE, next), Block.UPDATE_ALL);
        player.displayClientMessage(Component.translatable("message.alcoholic.port.mode", next.name()), true);
        MultiblockNotifier.notifyNearby(level, position);
        return InteractionResult.CONSUME;
    }

    public static InteractionResult use(
            Level level,
            BlockPos position,
            BlockState state,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        InteractionResult cycled = cycleMode(level, position, state, player, hand);
        if (cycled.consumesAction() || cycled == InteractionResult.SUCCESS) {
            return cycled;
        }
        if (level.getBlockEntity(position) instanceof ControllerBound bound) {
            MultiblockControllerBlockEntity controller = bound.controller();
            if (controller != null && !level.isClientSide) {
                player.displayClientMessage(controller.status(), true);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }
}
