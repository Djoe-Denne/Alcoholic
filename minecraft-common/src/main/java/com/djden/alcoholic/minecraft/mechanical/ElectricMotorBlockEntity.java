package com.djden.alcoholic.minecraft.mechanical;

import com.djden.alcoholic.domain.mechanical.MechanicalDrivePort;
import com.djden.alcoholic.domain.mechanical.MechanicalDriveState;
import com.djden.alcoholic.minecraft.energy.EnergyBuffer;
import com.djden.alcoholic.minecraft.energy.EnergyHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;

/**
 * Generic Forge-Energy motor. Converts stored FE into a
 * {@link MechanicalDrivePort}. Draw is proportional to mechanical work
 * reported by the connected machine; an idle neighbor does not drain
 * the maximum input rate.
 */
public final class ElectricMotorBlockEntity extends BlockEntity
        implements MechanicalDrivePort, EnergyHolder {
    public static final ElectricMotorSettings SETTINGS = ElectricMotorSettings.DEFAULT;

    private final EnergyBuffer energy = new EnergyBuffer(
            SETTINGS.energyCapacity(),
            SETTINGS.maxReceivePerTick(),
            SETTINGS.energyCapacity()
    );

    public ElectricMotorBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
        super(type, position, state);
    }

    @Override
    public EnergyBuffer energy() {
        return energy;
    }

    @Override
    public MechanicalDriveState driveState() {
        if (energy.isEmpty()) {
            return MechanicalDriveState.idle();
        }
        double capacity = SETTINGS.capacityFromEnergy(energy.stored());
        if (capacity <= 0.0) {
            return MechanicalDriveState.idle();
        }
        return MechanicalDriveState.running(SETTINGS.outputSpeed(), capacity);
    }

    @Override
    public boolean isSource() {
        return true;
    }

    @Override
    public void consumeWork(double load) {
        int cost = SETTINGS.feForLoad(load);
        if (cost <= 0) {
            return;
        }
        energy.extract(cost, false);
        setChanged();
        sync();
    }

    public static void serverTick(
            Level level,
            BlockPos position,
            BlockState state,
            ElectricMotorBlockEntity entity
    ) {
        entity.tick(state);
    }

    private void tick(BlockState state) {
        boolean powered = !energy.isEmpty();
        if (state.hasProperty(ElectricMotorBlock.LIT) && state.getValue(ElectricMotorBlock.LIT) != powered) {
            if (level != null) {
                level.setBlock(worldPosition, state.setValue(ElectricMotorBlock.LIT, powered), Block.UPDATE_ALL);
            }
        }
    }

    public String debugDump() {
        return "electric-motor energy=" + energy.stored() + "/" + energy.capacity()
                + " speed=" + driveState().speed()
                + " capacity=" + driveState().availableCapacity();
    }

    public Component status() {
        return Component.translatable(
                "message.alcoholic.electric_motor.status",
                String.format(Locale.ROOT, "%.0f", driveState().speed()),
                energy.stored(),
                energy.capacity()
        );
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Energy", energy.stored());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.setStored(tag.getInt("Energy"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
