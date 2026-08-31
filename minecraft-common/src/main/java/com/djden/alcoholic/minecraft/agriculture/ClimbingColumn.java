package com.djden.alcoholic.minecraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Shared trellis-column geometry. The root holds state. Stem and canopy
 * are projections. A click on any of the three reaches the root.
 */
public final class ClimbingColumn {
    public static final int MAX_WIRE_OFFSET = 2;

    private ClimbingColumn() {
    }

    public static boolean isSpan(BlockState state) {
        Objects.requireNonNull(state, "state");
        return state.getBlock() instanceof TrellisWireBlock
                || state.getBlock() instanceof ClimbingColumnCanopy;
    }

    public static Direction.Axis axisOf(BlockState state) {
        Objects.requireNonNull(state, "state");
        if (state.getBlock() instanceof ClimbingColumnCanopy canopy) {
            return canopy.axis(state);
        }
        return state.getValue(TrellisWireBlock.AXIS);
    }

    public static boolean isClearPathBlock(BlockState state) {
        Objects.requireNonNull(state, "state");
        return state.isAir() || state.getBlock() instanceof ClimbingColumnStem;
    }

    public static boolean isMatchingStem(BlockState state, ClimbingColumnRoot root) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(root, "root");
        return state.getBlock() instanceof ClimbingColumnStem stem && stem.belongsTo(root);
    }

    public static boolean isMatchingCanopy(BlockState state, ClimbingColumnRoot root) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(root, "root");
        return state.getBlock() instanceof ClimbingColumnCanopy canopy && canopy.belongsTo(root);
    }

    @Nullable
    public static BlockPos findRoot(BlockGetter level, BlockPos position) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(position, "position");
        BlockState here = level.getBlockState(position);
        if (here.getBlock() instanceof ClimbingColumnRoot) {
            return position;
        }
        if (here.getBlock() instanceof ClimbingColumnStem stem) {
            BlockPos below = position.below();
            BlockState belowState = level.getBlockState(below);
            if (belowState.getBlock() instanceof ClimbingColumnRoot root && stem.belongsTo(root)) {
                return below;
            }
            return null;
        }
        if (here.getBlock() instanceof ClimbingColumnCanopy canopy) {
            BlockPos below = position.below();
            BlockState belowState = level.getBlockState(below);
            if (belowState.getBlock() instanceof ClimbingColumnRoot root && canopy.belongsTo(root)) {
                return below;
            }
            if (belowState.getBlock() instanceof ClimbingColumnStem stem) {
                BlockPos rootPos = below.below();
                BlockState rootState = level.getBlockState(rootPos);
                if (rootState.getBlock() instanceof ClimbingColumnRoot root
                        && stem.belongsTo(root)
                        && canopy.belongsTo(root)) {
                    return rootPos;
                }
            }
        }
        return null;
    }

    public static InteractionResult useThroughRoot(
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(hand, "hand");
        Objects.requireNonNull(hit, "hit");
        BlockPos rootPos = findRoot(level, position);
        if (rootPos == null) {
            return InteractionResult.PASS;
        }
        BlockState rootState = level.getBlockState(rootPos);
        if (!(rootState.getBlock() instanceof ClimbingColumnRoot root)) {
            return InteractionResult.PASS;
        }
        return root.useAsRoot(rootState, level, rootPos, player, hand, hit);
    }

    public static void removeStem(
            LevelAccessor level,
            BlockPos rootPos,
            ClimbingColumnRoot root
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(rootPos, "rootPos");
        Objects.requireNonNull(root, "root");
        BlockPos stemPos = rootPos.above();
        if (isMatchingStem(level.getBlockState(stemPos), root)) {
            level.removeBlock(stemPos, false);
        }
    }

    public static void restoreCanopy(
            LevelAccessor level,
            BlockPos position,
            ClimbingColumnRoot root
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(root, "root");
        BlockState current = level.getBlockState(position);
        if (current.getBlock() instanceof ClimbingColumnCanopy canopy && canopy.belongsTo(root)) {
            level.setBlock(position, canopy.restoredWire(current), Block.UPDATE_CLIENTS);
        }
    }

    public static void removeProjection(
            LevelAccessor level,
            BlockPos rootPos,
            ClimbingColumnRoot root
    ) {
        restoreCanopy(level, rootPos.above(), root);
        restoreCanopy(level, rootPos.above(MAX_WIRE_OFFSET), root);
        removeStem(level, rootPos, root);
    }
}
