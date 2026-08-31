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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Mid-column hop projection. Clicks and bone meal route to the cep.
 */
public final class HopStemBlock extends Block
        implements BonemealableBlock, ClimbingColumnStem {
    public static final IntegerProperty AGE = HopBineBlock.AGE;
    public static final BooleanProperty TRAINED = HopBineBlock.TRAINED;

    private static final VoxelShape TRUNK_SHAPE =
            Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
    private static final VoxelShape TRAINED_SHAPE =
            Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    private final Supplier<? extends Block> rootBlock;

    public HopStemBlock(Properties properties, Supplier<? extends Block> rootBlock) {
        super(properties);
        this.rootBlock = Objects.requireNonNull(rootBlock, "rootBlock");
        registerDefaultState(
                stateDefinition.any()
                        .setValue(AGE, 0)
                        .setValue(TRAINED, true)
        );
    }

    @Override
    public boolean belongsTo(ClimbingColumnRoot root) {
        return root != null && root.stemBlock() == this;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos position) {
        BlockState below = level.getBlockState(position.below());
        if (!(below.getBlock() instanceof HopBineBlock root) || !belongsTo(root)) {
            return false;
        }
        return TrellisDetector.shared().boundedWireHeightAbove(level, position.below())
                == HopColumn.MAX_WIRE_OFFSET;
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
        return !state.canSurvive(level, position)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighbor, level, position, neighborPosition);
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
            level.removeBlock(position, false);
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
        return state.getValue(TRAINED) ? TRAINED_SHAPE : TRUNK_SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, TRAINED);
    }
}
