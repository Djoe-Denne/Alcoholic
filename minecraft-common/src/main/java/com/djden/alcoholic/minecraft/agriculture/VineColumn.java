package com.djden.alcoholic.minecraft.agriculture;

import com.djden.alcoholic.domain.viticulture.VineGrowthStage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

/**
 * Physical column of a grapevine. Domain state stays on the root; the stem is
 * a visual/structural projection that can be grown or folded back.
 */
public final class VineColumn {
    public static final int MAX_WIRE_OFFSET = 2;

    private VineColumn() {
    }

    public static boolean canExtend(VineGrowthStage stage) {
        Objects.requireNonNull(stage, "stage");
        return stage != VineGrowthStage.PLANTED
                && stage != VineGrowthStage.ESTABLISHING;
    }

    public static boolean shouldOccupyWire(int wireHeight, VineGrowthStage stage) {
        Objects.requireNonNull(stage, "stage");
        if (wireHeight == 1) {
            return true;
        }
        return wireHeight == MAX_WIRE_OFFSET && canExtend(stage);
    }

    public static boolean isMatchingStem(BlockState state, VineBlock root) {
        return state.getBlock() instanceof VineStemBlock stem && stem.belongsTo(root);
    }

    public static boolean isMatchingCanopy(BlockState state, VineBlock root) {
        return state.getBlock() instanceof VineCanopyBlock canopy && canopy.belongsTo(root);
    }

    public static void removeStem(LevelAccessor level, BlockPos rootPos, VineBlock rootBlock) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(rootPos, "rootPos");
        Objects.requireNonNull(rootBlock, "rootBlock");
        BlockPos stemPos = rootPos.above();
        if (isMatchingStem(level.getBlockState(stemPos), rootBlock)) {
            level.removeBlock(stemPos, false);
        }
    }

    public static void removeProjection(LevelAccessor level, BlockPos rootPos, VineBlock rootBlock) {
        restoreCanopy(level, rootPos.above(), rootBlock);
        restoreCanopy(level, rootPos.above(MAX_WIRE_OFFSET), rootBlock);
        removeStem(level, rootPos, rootBlock);
    }

    public static void sync(
            Level level,
            BlockPos rootPos,
            VineBlock rootBlock,
            VineGrowthStage stage,
            TrellisDetector detector,
            boolean allowGrowth
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(rootPos, "rootPos");
        Objects.requireNonNull(rootBlock, "rootBlock");
        Objects.requireNonNull(stage, "stage");
        Objects.requireNonNull(detector, "detector");
        if (level.isClientSide) {
            return;
        }

        BlockState current = level.getBlockState(rootPos);
        if (current.getBlock() != rootBlock) {
            return;
        }

        int wireHeight = detector.boundedWireHeightAbove(level, rootPos);
        boolean trained = wireHeight > 0;
        boolean wantStem = canExtend(stage) && wireHeight == MAX_WIRE_OFFSET;
        BlockPos stemPos = rootPos.above();
        BlockState above = level.getBlockState(stemPos);
        boolean hasStem = isMatchingStem(above, rootBlock);
        boolean extended = false;

        if (wantStem) {
            if (hasStem) {
                updateStem(level, stemPos, above, stage, true);
                extended = true;
            } else if (allowGrowth && above.isAir()) {
                Block stem = rootBlock.stemBlock();
                if (stem != null) {
                    level.setBlock(
                            stemPos,
                            stem.defaultBlockState()
                                    .setValue(VineStemBlock.STAGE, VineStage.fromDomain(stage))
                                    .setValue(VineStemBlock.TRAINED, true),
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

        boolean wantCanopy = trained && shouldOccupyWire(wireHeight, stage)
                && (wireHeight == 1 || hasStem);
        if (wantCanopy) {
            occupyWire(level, rootPos.above(wireHeight), rootBlock, stage, allowGrowth, hasStem);
        } else {
            restoreCanopy(level, rootPos.above(), rootBlock);
            restoreCanopy(level, rootPos.above(MAX_WIRE_OFFSET), rootBlock);
        }

        BlockState next = current
                .setValue(VineBlock.STAGE, VineStage.fromDomain(stage))
                .setValue(VineBlock.TRAINED, trained)
                .setValue(VineBlock.EXTENDED, extended);
        if (!next.equals(current)) {
            level.setBlock(rootPos, next, Block.UPDATE_CLIENTS);
        }
    }

    private static void occupyWire(
            Level level,
            BlockPos wirePos,
            VineBlock rootBlock,
            VineGrowthStage stage,
            boolean allowGrowth,
            boolean trunk
    ) {
        BlockState current = level.getBlockState(wirePos);
        if (isMatchingCanopy(current, rootBlock)) {
            BlockState next = current
                    .setValue(VineCanopyBlock.STAGE, VineStage.fromDomain(stage))
                    .setValue(VineCanopyBlock.TRUNK, trunk);
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
                        .setValue(VineCanopyBlock.STAGE, VineStage.fromDomain(stage))
                        .setValue(VineCanopyBlock.AXIS, current.getValue(TrellisWireBlock.AXIS))
                        .setValue(VineCanopyBlock.TRUNK, trunk),
                Block.UPDATE_CLIENTS
        );
    }

    private static void restoreCanopy(
            LevelAccessor level,
            BlockPos position,
            VineBlock rootBlock
    ) {
        BlockState current = level.getBlockState(position);
        if (current.getBlock() instanceof VineCanopyBlock canopy && canopy.belongsTo(rootBlock)) {
            level.setBlock(position, canopy.restoredWire(current), Block.UPDATE_CLIENTS);
        }
    }

    private static void updateStem(
            Level level,
            BlockPos stemPos,
            BlockState current,
            VineGrowthStage stage,
            boolean trained
    ) {
        BlockState next = current
                .setValue(VineStemBlock.STAGE, VineStage.fromDomain(stage))
                .setValue(VineStemBlock.TRAINED, trained);
        if (!next.equals(current)) {
            level.setBlock(stemPos, next, Block.UPDATE_CLIENTS);
        }
    }
}
