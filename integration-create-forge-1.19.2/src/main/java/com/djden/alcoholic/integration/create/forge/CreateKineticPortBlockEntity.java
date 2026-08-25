package com.djden.alcoholic.integration.create.forge;

import com.djden.alcoholic.domain.mechanical.MechanicalDriveState;
import com.djden.alcoholic.minecraft.AlcoholicDebug;
import com.djden.alcoholic.minecraft.mechanical.MechanicalDrives;
import com.djden.alcoholic.minecraft.multiblock.ControllerBound;
import com.djden.alcoholic.minecraft.multiblock.KineticSource;
import com.djden.alcoholic.minecraft.multiblock.MultiblockControllerBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Create kinetic network member that exposes Alcoholic's mechanical port.
 */
public final class CreateKineticPortBlockEntity extends KineticBlockEntity
        implements ControllerBound, KineticSource {
    private BlockPos controllerPos;
    private Double debugOverride;

    public CreateKineticPortBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
        super(type, position, state);
    }

    @Override
    public double rpm() {
        return driveState().speed();
    }

    @Override
    public void setRpm(double rpm) {
        debugOverride = Math.max(0.0, rpm);
        setChanged();
    }

    @Override
    public MechanicalDriveState driveState() {
        if (debugOverride != null) {
            return MechanicalDriveState.running(debugOverride, Double.POSITIVE_INFINITY);
        }
        double speed = Math.abs(getSpeed());
        boolean stalled = isOverStressed();
        if (stalled) {
            return MechanicalDriveState.stalled(speed, 0.0);
        }
        if (speed <= 0.0) {
            return MechanicalDrives.forMachine(level, worldPosition);
        }
        return MechanicalDriveState.running(speed, 64.0);
    }

    @Override
    public boolean isSource() {
        return debugOverride != null || Math.abs(getSpeed()) > 0.0;
    }

    @Override
    public BlockPos cachedController() {
        return controllerPos;
    }

    @Override
    public void bindController(BlockPos controller) {
        this.controllerPos = controller.immutable();
        setChanged();
    }

    @Override
    public void clearController() {
        this.controllerPos = null;
        setChanged();
    }

    @Override
    public MultiblockControllerBlockEntity controller() {
        if (level == null || controllerPos == null || !level.hasChunkAt(controllerPos)) {
            return null;
        }
        if (level.getBlockEntity(controllerPos) instanceof MultiblockControllerBlockEntity found
                && found.owns(worldPosition)) {
            return found;
        }
        controllerPos = null;
        return null;
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if (controllerPos != null) {
            tag.put("Controller", NbtUtils.writeBlockPos(controllerPos));
        }
        if (AlcoholicDebug.ENABLED && debugOverride != null) {
            tag.putDouble("DebugRpm", debugOverride);
        }
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        controllerPos = tag.contains("Controller") ? NbtUtils.readBlockPos(tag.getCompound("Controller")) : null;
        if (AlcoholicDebug.ENABLED) {
            debugOverride = tag.contains("DebugRpm") ? tag.getDouble("DebugRpm") : null;
        }
    }
}
