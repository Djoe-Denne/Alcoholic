package com.djden.alcoholic.minecraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Hop canopy that occupies the trellis wire. Clicks route to the cep.
 */
public final class HopCanopyBlock extends Block
        implements BonemealableBlock, ClimbingColumnCanopy {
    public static final IntegerProperty AGE = HopBineBlock.AGE;
    public static final EnumProperty<Direction.Axis> AXIS =
            BlockStateProperties.HORIZONTAL_AXIS;
    public static final BooleanProperty TRUNK = BooleanProperty.create("trunk");

    private static final VoxelShape SHAPE =
            Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    private final Supplier<? extends Block> rootBlock;
    private final Supplier<? extends Block> wireBlock;

    public HopCanopyBlock(
            Properties properties,
            Supplier<? extends Block> rootBlock,
            Supplier<? extends Block> wireBlock
    ) {
        super(properties);
        this.rootBlock = Objects.requireNonNull(rootBlock, "rootBlock");
        this.wireBlock = Objects.requireNonNull(wireBlock, "wireBlock");
        registerDefaultState(
                stateDefinition.any()
                        .setValue(AGE, 0)
                        .setValue(AXIS, Direction.Axis.X)
                        .setValue(TRUNK, false)
        );
    }

    @Override
    public boolean belongsTo(ClimbingColumnRoot root) {
        return root != null && root.canopyBlock() == this;
    }

    @Override
    public Direction.Axis axis(BlockState canopyState) {
        return canopyState.getValue(AXIS);
    }

    @Override
    public BlockState restoredWire(BlockState canopyState) {
        return wireBlock.get().defaultBlockState().setValue(
                TrellisWireBlock.AXIS,
                canopyState.getValue(AXIS)
        );
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos position) {
        BlockPos rootPos = ClimbingColumn.findRoot(level, position);
        if (rootPos == null) {
            return false;
        }
        BlockState rootState = level.getBlockState(rootPos);
        if (!(rootState.getBlock() instanceof HopBineBlock root) || !belongsTo(root)) {
            return false;
        }
        int height = position.getY() - rootPos.getY();
        return TrellisDetector.shared().boundedWireHeightAbove(level, rootPos) == height
                && (height == 1 || ClimbingColumn.isMatchingStem(
                        level.getBlockState(position.below()),
                        root
                ));
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighbor,
            LevelAccessor level,
            BlockPos position,
            BlockPos neighborPosition
    ) {
        return state.canSurvive(level, position)
                ? super.updateShape(
                        state,
                        direction,
                        neighbor,
                        level,
                        position,
                        neighborPosition
                )
                : restoredWire(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos position,
            Block block,
            BlockPos fromPos,
            boolean moving
    ) {
        if (!state.canSurvive(level, position)) {
            level.setBlock(position, restoredWire(state), Block.UPDATE_ALL);
        }
    }

    @Override
    public void playerDestroy(
            Level level,
            Player player,
            BlockPos position,
            BlockState state,
            @Nullable BlockEntity blockEntity,
            ItemStack tool
    ) {
        if (!level.isClientSide) {
            level.setBlock(position, restoredWire(state), Block.UPDATE_ALL);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        return ClimbingColumn.useThroughRoot(level, position, player, hand, hit);
    }

    @Override
    public boolean isValidBonemealTarget(
            BlockGetter level,
            BlockPos position,
            BlockState state,
            boolean client
    ) {
        BlockPos rootPos = ClimbingColumn.findRoot(level, position);
        if (rootPos == null) {
            return false;
        }
        BlockState rootState = level.getBlockState(rootPos);
        return rootState.getBlock() instanceof HopBineBlock root
                && belongsTo(root)
                && root.isValidBonemealTarget(level, rootPos, rootState, client);
    }

    @Override
    public boolean isBonemealSuccess(
            Level level,
            RandomSource random,
            BlockPos position,
            BlockState state
    ) {
        return true;
    }

    @Override
    public void performBonemeal(
            ServerLevel level,
            RandomSource random,
            BlockPos position,
            BlockState state
    ) {
        BlockPos rootPos = ClimbingColumn.findRoot(level, position);
        if (rootPos == null) {
            return;
        }
        BlockState rootState = level.getBlockState(rootPos);
        if (rootState.getBlock() instanceof HopBineBlock root && belongsTo(root)) {
            root.performBonemeal(level, random, rootPos, rootState);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos position, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90, COUNTERCLOCKWISE_90 -> state.setValue(
                    AXIS,
                    state.getValue(AXIS) == Direction.Axis.X
                            ? Direction.Axis.Z
                            : Direction.Axis.X
            );
            default -> state;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, AXIS, TRUNK);
    }
}
