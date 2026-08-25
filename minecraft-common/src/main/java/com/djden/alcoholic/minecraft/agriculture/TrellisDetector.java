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

    public boolean isTrained(LevelReader level, BlockPos vinePosition) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(vinePosition, "vinePosition");
        return hasBoundedWire(level, vinePosition.above())
                || hasBoundedWire(level, vinePosition.above(2));
    }

    private boolean hasBoundedWire(LevelReader level, BlockPos wirePosition) {
        BlockState wireState = level.getBlockState(wirePosition);
        if (!(wireState.getBlock() instanceof TrellisWireBlock)) {
            return false;
        }

        Direction.Axis axis = wireState.getValue(TrellisWireBlock.AXIS);
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
            if (state.getBlock() instanceof VineyardPostBlock) {
                return distance;
            }
            if (!(state.getBlock() instanceof TrellisWireBlock)
                    || state.getValue(TrellisWireBlock.AXIS) != axis) {
                return -1;
            }
        }
        return -1;
    }
}
