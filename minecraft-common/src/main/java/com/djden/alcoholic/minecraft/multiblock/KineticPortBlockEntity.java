package com.djden.alcoholic.minecraft.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stores the last known RPM. Create writes this from the Forge adapter;
 * GameTests may call {@link #setRpm(double)} directly.
 */
public final class KineticPortBlockEntity extends PartBlockEntity implements KineticSource {
    private double rpm;

    public KineticPortBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
        super(type, position, state);
    }

    public double rpm() {
        return rpm;
    }

    public void setRpm(double rpm) {
        this.rpm = Math.max(0.0, rpm);
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("Rpm", rpm);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        rpm = tag.getDouble("Rpm");
    }
}
