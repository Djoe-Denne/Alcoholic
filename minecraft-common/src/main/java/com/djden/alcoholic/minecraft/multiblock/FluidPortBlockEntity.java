package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.api.property.PropertyMerge;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.multiblock.PortMode;
import com.djden.alcoholic.minecraft.fluid.LiquidTank;
import com.djden.alcoholic.minecraft.fluid.LiquidVessel;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class FluidPortBlockEntity extends PartBlockEntity implements LiquidVessel {
    public FluidPortBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
        super(type, position, state);
    }

    public PortMode mode() {
        return getBlockState().hasProperty(PortBlocks.MODE)
                ? getBlockState().getValue(PortBlocks.MODE).domain()
                : PortMode.BOTH;
    }

    @Override
    public LiquidTank tank() {
        MultiblockControllerBlockEntity controller = controller();
        if (controller == null) {
            return new LiquidTank(1, id -> PropertyMerge.WEIGHTED_AVERAGE);
        }
        return new DirectedTank(controller, mode());
    }

    private static final class DirectedTank extends LiquidTank {
        private final MultiblockControllerBlockEntity controller;
        private final PortMode mode;

        private DirectedTank(MultiblockControllerBlockEntity controller, PortMode mode) {
            super(1, id -> PropertyMerge.WEIGHTED_AVERAGE);
            this.controller = controller;
            this.mode = mode;
        }

        @Override
        public int capacity() {
            return controller.tank().capacity();
        }

        @Override
        public Optional<LiquidBatch> contents() {
            return controller.tank().contents();
        }

        @Override
        public int fill(LiquidBatch incoming, boolean simulate) {
            if (!mode.allowsInsert() || !controller.access().canFill()) {
                return 0;
            }
            int filled = controller.tank().fill(incoming, simulate);
            if (!simulate && filled > 0) {
                controller.onTankChanged();
            }
            return filled;
        }

        @Override
        public LiquidBatch drain(int millibuckets, boolean simulate) {
            if (!mode.allowsExtract() || !controller.access().canDrain()) {
                return LiquidBatch.of(0);
            }
            LiquidBatch drained = controller.tank().drain(millibuckets, simulate);
            if (!simulate && drained.volume() > 0.0) {
                controller.onTankChanged();
            }
            return drained;
        }
    }
}
