package com.djden.alcoholic.minecraft.menu;

import com.djden.alcoholic.api.ResourceId;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public final class MachineMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;
    private final MachineLayout layout;
    private final MachineAccess access;

    public static MachineMenu twoSlots(int id, Inventory inventory) {
        return client(MachineLayout.TWO_SLOTS, id, inventory);
    }

    public static MachineMenu twoSlotsOneTank(int id, Inventory inventory) {
        return client(MachineLayout.TWO_SLOTS_ONE_TANK, id, inventory);
    }

    public static MachineMenu twoSlotsTwoTanks(int id, Inventory inventory) {
        return client(MachineLayout.TWO_SLOTS_TWO_TANKS, id, inventory);
    }

    public static MachineMenu oneSlotOneTank(int id, Inventory inventory) {
        return client(MachineLayout.ONE_SLOT_ONE_TANK, id, inventory);
    }

    public static MachineMenu oneTank(int id, Inventory inventory) {
        return client(MachineLayout.ONE_TANK, id, inventory);
    }

    public static MachineMenu twoTanks(int id, Inventory inventory) {
        return client(MachineLayout.TWO_TANKS, id, inventory);
    }

    public static MachineMenu fuel(int id, Inventory inventory) {
        return client(MachineLayout.FUEL, id, inventory);
    }

    public static MachineMenu energy(int id, Inventory inventory) {
        return client(MachineLayout.ENERGY, id, inventory);
    }

    private static MachineMenu client(MachineLayout layout, int id, Inventory inventory) {
        return new MachineMenu(
                MachineMenus.type(layout),
                id,
                inventory,
                new SimpleContainer(layout.machineSlotCount()),
                new SimpleContainerData(MachineContainerData.SIZE),
                layout,
                null
        );
    }

    MachineMenu(
            MenuType<?> type,
            int id,
            Inventory inventory,
            Container container,
            ContainerData data,
            MachineLayout layout,
            MachineAccess access
    ) {
        super(type, id);
        this.container = container;
        this.data = data;
        this.layout = layout;
        this.access = access;
        MachineLayout.SlotPos[] slots = layout.slots();
        for (int index = 0; index < slots.length; index++) {
            addSlot(new Slot(container, index, slots[index].x(), slots[index].y()));
        }
        addPlayerInventory(inventory);
        addDataSlots(data);
        if (access != null) {
            container.startOpen(inventory.player);
        }
    }

    public boolean uses(MachineAccess other) {
        return access == other;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (access != null) {
            container.stopOpen(player);
        }
    }

    public MachineLayout layout() {
        return layout;
    }

    public List<ResourceId> displayedProcessTypes(Level level) {
        if (access != null) {
            return access.displayedProcessTypes();
        }
        if (level == null) {
            return List.of();
        }
        BlockEntity entity = level.getBlockEntity(new BlockPos(
                data.get(MachineContainerData.BE_X),
                data.get(MachineContainerData.BE_Y),
                data.get(MachineContainerData.BE_Z)
        ));
        if (entity instanceof MachineAccess machine) {
            return machine.displayedProcessTypes();
        }
        return List.of();
    }

    public int progress() {
        return data.get(MachineContainerData.PROGRESS);
    }

    public int duration() {
        return Math.max(1, data.get(MachineContainerData.DURATION));
    }

    public int temperatureDeci() {
        return data.get(MachineContainerData.TEMP_DECI);
    }

    public int extra() {
        return data.get(MachineContainerData.EXTRA);
    }

    public int extra2() {
        return data.get(MachineContainerData.EXTRA2);
    }

    public int tankVolume(int tank) {
        return data.get(tank == 0 ? MachineContainerData.TANK0_VOLUME : MachineContainerData.TANK1_VOLUME);
    }

    public int tankCapacity(int tank) {
        return data.get(tank == 0 ? MachineContainerData.TANK0_CAPACITY : MachineContainerData.TANK1_CAPACITY);
    }

    public int tankFluidId(int tank) {
        return data.get(tank == 0 ? MachineContainerData.TANK0_FLUID : MachineContainerData.TANK1_FLUID);
    }

    public int flags() {
        return data.get(MachineContainerData.FLAGS);
    }

    public boolean formed() {
        return (flags() & MachineContainerData.FLAG_FORMED) != 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return access == null || access.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack current = slot.getItem();
        original = current.copy();
        int machineSlots = layout.machineSlotCount();
        if (machineSlots == 0) {
            return ItemStack.EMPTY;
        }
        if (index < machineSlots) {
            if (!moveItemStackTo(current, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(current, 0, machineSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (current.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    private void addPlayerInventory(Inventory inventory) {
        MachineLayout.SlotPos[] playerSlots = MachineLayout.PLAYER_SLOTS;
        for (int index = 0; index < 27; index++) {
            addSlot(new Slot(
                    inventory,
                    index + 9,
                    playerSlots[index].x(),
                    playerSlots[index].y()
            ));
        }
        for (int index = 0; index < 9; index++) {
            addSlot(new Slot(
                    inventory,
                    index,
                    playerSlots[27 + index].x(),
                    playerSlots[27 + index].y()
            ));
        }
    }
}
