package com.djden.alcoholic.minecraft.agriculture;

import com.djden.alcoholic.minecraft.viticulture.ViticultureRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

/**
 * Runtime trellis query. The domain layer receives only the resulting
 * trained/untrained state.
 */
public final class TrellisDetector {
    private static final TrellisDetector SHARED =
            new TrellisDetector(ViticultureRuntime.shared());

    private final ViticultureRuntime runtime;

    public TrellisDetector(ViticultureRuntime runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
    }

    public static TrellisDetector shared() {
        return SHARED;
    }

    public boolean isTrained(LevelReader level, BlockPos plantPosition) {
        return boundedWireHeightAbove(level, plantPosition) > 0;
    }

    /**
     * Height of the first bounded trellis wire above the plant, or {@code 0}
     * when none is reachable through a clear path.
     */
    public int boundedWireHeightAbove(LevelReader level, BlockPos plantPosition) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(plantPosition, "plantPosition");
        for (int height = 1; height <= ClimbingColumn.MAX_WIRE_OFFSET; height++) {
            if (hasBoundedWire(level, plantPosition.above(height))
                    && pathClearTo(level, plantPosition, height)) {
                return height;
            }
        }
        return 0;
    }

    private static boolean pathClearTo(LevelReader level, BlockPos root, int wireHeight) {
        for (int dy = 1; dy < wireHeight; dy++) {
            if (!ClimbingColumn.isClearPathBlock(level.getBlockState(root.above(dy)))) {
                return false;
            }
        }
        return true;
    }

    public boolean hasOverheadRun(LevelReader level, BlockPos plantPosition, int maximumHeight) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(plantPosition, "plantPosition");
        int height = Math.max(1, maximumHeight);
        for (int dy = 1; dy <= height; dy++) {
            if (hasBoundedWire(level, plantPosition.above(dy))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSpan(BlockState state) {
        return ClimbingColumn.isSpan(state);
    }

    public static Direction.Axis axisOf(BlockState state) {
        return ClimbingColumn.axisOf(state);
    }

    public boolean hasBoundedWire(LevelReader level, BlockPos wirePosition) {
        BlockState wireState = level.getBlockState(wirePosition);
        if (!isSpan(wireState)) {
            return false;
        }

        Direction.Axis axis = axisOf(wireState);
        Direction negative = axis == Direction.Axis.X ? Direction.WEST : Direction.NORTH;
        Direction positive = negative.getOpposite();
        int maximumSpan = runtime.settings().maxWireDistance();

        int negativeDistance = distanceToPost(
                level,
                wirePosition,
                negative,
                axis,
                maximumSpan
        );
        if (negativeDistance < 0) {
            return false;
        }
        int positiveDistance = distanceToPost(
                level,
                wirePosition,
                positive,
                axis,
                maximumSpan - negativeDistance
        );
        return positiveDistance >= 0
                && negativeDistance + positiveDistance <= maximumSpan;
    }

    private static int distanceToPost(
            LevelReader level,
            BlockPos origin,
            Direction direction,
            Direction.Axis axis,
            int maximumDistance
    ) {
        for (int distance = 1; distance <= maximumDistance; distance++) {
            BlockState state = level.getBlockState(origin.relative(direction, distance));
            if (state.getBlock() instanceof CropSupportPost) {
                return distance;
            }
            if (!isSpan(state) || axisOf(state) != axis) {
                return -1;
            }
        }
        return -1;
    }
}
