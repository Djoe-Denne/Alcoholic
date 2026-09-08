package com.djden.alcoholic.minecraft.process;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/** A nine-cell rack or shelf which joins same-style, same-facing neighbours into a chest view. */
public final class BottleStandBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Shapes.block();

    private final BottleStandStyle style;
    private final Supplier<? extends BlockEntityType<?>> blockEntityType;

    public BottleStandBlock(
            BottleStandStyle style,
            Properties properties,
            Supplier<? extends BlockEntityType<?>> blockEntityType
    ) {
        super(properties);
        this.style = style;
        this.blockEntityType = blockEntityType;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public BottleStandStyle style() {
        return style;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new BottleStandBlockEntity(blockEntityType.get(), position, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos position, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public net.minecraft.world.InteractionResult use(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            net.minecraft.world.InteractionHand hand,
            BlockHitResult hit
    ) {
        if (level.isClientSide) {
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        ItemStack held = player.getItemInHand(hand);
        BottleStandNetwork network = BottleStandNetwork.find(level, position, this);
        if (!player.isShiftKeyDown() && BottleStandBlockEntity.isBottle(held) && network.insertOne(held)) {
            return net.minecraft.world.InteractionResult.CONSUME;
        }
        if (player instanceof ServerPlayer server) {
            server.openMenu(menu(network));
            return net.minecraft.world.InteractionResult.CONSUME;
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    private MenuProvider menu(BottleStandNetwork network) {
        Container inventory = network.inventory();
        int rows = network.rows();
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("container.alcoholic." + style.id());
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
                return new ChestMenu(menuType(rows), id, playerInventory, inventory, rows);
            }
        };
    }

    private static MenuType<ChestMenu> menuType(int rows) {
        return switch (rows) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            default -> MenuType.GENERIC_9x6;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos position, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(position) instanceof BottleStandBlockEntity entity) {
            Containers.dropContents(level, position, entity);
        }
        super.onRemove(state, level, position, newState, moved);
    }
}
