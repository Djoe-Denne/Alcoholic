package com.djden.alcoholic.minecraft.menu;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.minecraft.energy.EnergyHolder;
import com.djden.alcoholic.minecraft.fluid.LiquidTank;
import com.djden.alcoholic.minecraft.fluid.LiquidVessel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public interface MachineAccess extends MenuProvider {
    MachineLayout layout();

    /**
     * Process types this machine can execute. Recipe viewers must use this
     * list, not the GUI {@link MachineLayout}.
     */
    default List<ResourceId> displayedProcessTypes() {
        return List.of();
    }

    default int blockX() {
        return this instanceof BlockEntity entity ? entity.getBlockPos().getX() : 0;
    }

    default int blockY() {
        return this instanceof BlockEntity entity ? entity.getBlockPos().getY() : 0;
    }

    default int blockZ() {
        return this instanceof BlockEntity entity ? entity.getBlockPos().getZ() : 0;
    }

    default Container items() {
        if (this instanceof Container container) {
            return container;
        }
        return new SimpleContainer(layout().machineSlotCount());
    }

    default int progress() {
        return 0;
    }

    default int duration() {
        return 1;
    }

    default int temperatureDeci() {
        return 0;
    }

    default int extra() {
        if (this instanceof EnergyHolder holder) {
            return holder.energy().stored();
        }
        return 0;
    }

    default int extra2() {
        if (this instanceof EnergyHolder holder) {
            return holder.energy().capacity();
        }
        return 0;
    }

    default int flags() {
        return 0;
    }

    default int tankVolume(int index) {
        return vesselTank(index).contents().map(LiquidBatch::volumeMillibuckets).orElse(0);
    }

    default int tankCapacity(int index) {
        return vesselTank(index).capacity();
    }

    default int tankFluidId(int index) {
        return MachineFluids.id(vesselTank(index));
    }

    private LiquidTank vesselTank(int index) {
        if (this instanceof LiquidVessel vessel && index >= 0 && index < vessel.tankCount()) {
            return vessel.tank(index);
        }
        return LiquidTank.sealed();
    }

    default boolean stillValid(Player player) {
        if (!(this instanceof BlockEntity entity)) {
            return true;
        }
        Level level = entity.getLevel();
        if (level == null || level != player.level || entity.isRemoved()) {
            return false;
        }
        BlockPos pos = entity.getBlockPos();
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    default AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return MachineMenus.create(this, id, inventory);
    }

    @Override
    default Component getDisplayName() {
        if (this instanceof BlockEntity entity) {
            return entity.getBlockState().getBlock().getName();
        }
        return Component.translatable("container.alcoholic.machine");
    }

    static int deci(double celsius) {
        if (!Double.isFinite(celsius)) {
            return 0;
        }
        return (int) Math.round(celsius * 10.0);
    }
}
