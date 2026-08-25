package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.domain.mechanical.MechanicalDriveState;
import com.djden.alcoholic.domain.mechanical.MechanicalRequirement;
import com.djden.alcoholic.minecraft.AlcoholicDebug;
import com.djden.alcoholic.minecraft.mechanical.MechanicalDrives;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Multiblock mechanical face. Stores a debug speed for GameTests and
 * otherwise forwards an adjacent {@link com.djden.alcoholic.domain.mechanical.MechanicalDrivePort}
 * such as the primitive engine. It is a relay, not a power source.
 */
public final class KineticPortBlockEntity extends PartBlockEntity implements KineticSource {
    private double storedSpeed;

    public KineticPortBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
        super(type, position, state);
    }

    @Override
    public double rpm() {
        return driveState().speed();
    }

    @Override
    public void setRpm(double rpm) {
        storedSpeed = Math.max(0.0, rpm);
        setChanged();
    }

    @Override
    public MechanicalDriveState driveState() {
        return driveState(null);
    }

    MechanicalDriveState driveState(MechanicalRequirement requirement) {
        if (storedSpeed > 0.0) {
            return MechanicalDriveState.running(storedSpeed, Double.POSITIVE_INFINITY);
        }
        return requirement == null
                ? MechanicalDrives.forMachine(level, worldPosition)
                : MechanicalDrives.forMachine(level, worldPosition, requirement);
    }

    @Override
    public boolean isSource() {
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (AlcoholicDebug.ENABLED) {
            tag.putDouble("Rpm", storedSpeed);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (AlcoholicDebug.ENABLED) {
            storedSpeed = tag.getDouble("Rpm");
        }
    }
}
