package com.djden.alcoholic.minecraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

/**
 * Hop-specific growth on top of {@link ClimbingColumn}. Age gates harvest
 * and whether the bine may occupy the upper wire. No pruning, no lot.
 */
public final class HopColumn {
    public static final int MAX_WIRE_OFFSET = ClimbingColumn.MAX_WIRE_OFFSET;

    private HopColumn() {
    }

    public static boolean canExtend(int age) {
        return age >= 1;
    }

    public static boolean shouldOccupyWire(int wireHeight, int age) {
        if (wireHeight == 1) {
            return true;
        }
        return wireHeight == MAX_WIRE_OFFSET && canExtend(age);
    }

    public static int baseYield(boolean stem, boolean canopy) {
        int count = 1;
        if (stem) {
            count++;
        }
        if (canopy) {
            count++;
        }
        return count;
    }

    public static int harvestCount(BlockGetter level, BlockPos rootPos, ClimbingColumnRoot root) {
        return ClimbingColumn.occupiedPlantCount(level, rootPos, root);
    }

    public static void sync(
            Level level,
            BlockPos rootPos,
            HopBineBlock rootBlock,
            int age,
            TrellisDetector detector,
            boolean allowGrowth
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(rootPos, "rootPos");
        Objects.requireNonNull(rootBlock, "rootBlock");
        Objects.requireNonNull(detector, "detector");
        if (level.isClientSide) {
            return;
        }

        BlockState current = level.getBlockState(rootPos);
        if (current.getBlock() != rootBlock) {
            return;
        }
        int boundedAge = HopBineBlock.boundAge(age);

        int wireHeight = detector.boundedWireHeightAbove(level, rootPos);
        boolean trained = wireHeight > 0;
        BlockPos stemPos = rootPos.above();
        BlockState above = level.getBlockState(stemPos);
        boolean hasStem = ClimbingColumn.isMatchingStem(above, rootBlock);
        boolean wantStem = wireHeight == MAX_WIRE_OFFSET
                && (canExtend(boundedAge) || hasStem);
        boolean extended = false;

        if (wantStem) {
            if (hasStem) {
                updateStem(level, stemPos, above, boundedAge, true);
                extended = true;
            } else if (allowGrowth && above.isAir()) {
                Block stem = rootBlock.stemBlock();
                if (stem != null) {
                    level.setBlock(
                            stemPos,
                            stem.defaultBlockState()
                                    .setValue(HopStemBlock.AGE, boundedAge)
                                    .setValue(HopStemBlock.TRAINED, true),
                            Block.UPDATE_CLIENTS
                    );
                    extended = true;
                    hasStem = true;
                }
            } else if (!above.isAir()) {
                trained = false;
            }
        } else if (hasStem) {
            level.removeBlock(stemPos, false);
            hasStem = false;
        }

        boolean alreadyCanopy = wireHeight > 0
                && ClimbingColumn.isMatchingCanopy(
                        level.getBlockState(rootPos.above(wireHeight)),
                        rootBlock
                );
        boolean wantCanopy = trained
                && (shouldOccupyWire(wireHeight, boundedAge) || alreadyCanopy)
                && (wireHeight == 1 || hasStem);
        if (wantCanopy) {
            occupyWire(level, rootPos.above(wireHeight), rootBlock, boundedAge, allowGrowth, hasStem);
        } else {
            ClimbingColumn.restoreCanopy(level, rootPos.above(), rootBlock);
            ClimbingColumn.restoreCanopy(level, rootPos.above(MAX_WIRE_OFFSET), rootBlock);
        }

        BlockState next = current
                .setValue(HopBineBlock.AGE, boundedAge)
                .setValue(HopBineBlock.TRAINED, trained)
                .setValue(HopBineBlock.EXTENDED, extended);
        if (!next.equals(current)) {
            level.setBlock(rootPos, next, Block.UPDATE_CLIENTS);
        }
    }

    private static void occupyWire(
            Level level,
            BlockPos wirePos,
            HopBineBlock rootBlock,
            int age,
            boolean allowGrowth,
            boolean trunk
    ) {
        BlockState current = level.getBlockState(wirePos);
        if (ClimbingColumn.isMatchingCanopy(current, rootBlock)) {
            BlockState next = current
                    .setValue(HopCanopyBlock.AGE, age)
                    .setValue(HopCanopyBlock.TRUNK, trunk);
            if (!next.equals(current)) {
                level.setBlock(wirePos, next, Block.UPDATE_CLIENTS);
            }
            return;
        }
        if (!allowGrowth || !(current.getBlock() instanceof TrellisWireBlock)) {
            return;
        }
        Block canopy = rootBlock.canopyBlock();
        if (canopy == null) {
            return;
        }
        level.setBlock(
                wirePos,
                canopy.defaultBlockState()
                        .setValue(HopCanopyBlock.AGE, age)
                        .setValue(HopCanopyBlock.AXIS, current.getValue(TrellisWireBlock.AXIS))
                        .setValue(HopCanopyBlock.TRUNK, trunk),
                Block.UPDATE_CLIENTS
        );
    }

    private static void updateStem(
            Level level,
            BlockPos stemPos,
            BlockState current,
            int age,
            boolean trained
    ) {
        BlockState next = current
                .setValue(HopStemBlock.AGE, age)
                .setValue(HopStemBlock.TRAINED, trained);
        if (!next.equals(current)) {
            level.setBlock(stemPos, next, Block.UPDATE_CLIENTS);
        }
    }
}
