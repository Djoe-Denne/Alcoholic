package com.djden.alcoholic.minecraft.mechanical;

import com.djden.alcoholic.domain.mechanical.MechanicalDrivePort;
import com.djden.alcoholic.domain.mechanical.MechanicalDriveState;
import com.djden.alcoholic.minecraft.menu.MachineAccess;
import com.djden.alcoholic.minecraft.menu.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;

/**
 * Standalone fuel-burning drive. Fixed low speed and limited capacity; it
 * connects by sitting next to a machine, not through a shaft network.
 */
public final class PrimitiveCombustionEngineBlockEntity extends BlockEntity
        implements WorldlyContainer, MechanicalDrivePort, MachineAccess {
    public static final int FUEL_SLOT = 0;
    public static final double OUTPUT_SPEED = 16.0;
    public static final double OUTPUT_CAPACITY = 4.0;

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private int burnTime;
    private int burnDuration;

    public PrimitiveCombustionEngineBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
        super(type, position, state);
    }

    @Override
    public MechanicalDriveState driveState() {
        return burnTime > 0
                ? MechanicalDriveState.running(OUTPUT_SPEED, OUTPUT_CAPACITY)
                : MechanicalDriveState.idle();
    }

    @Override
    public boolean isSource() {
        return true;
    }

    public int burnTime() {
        return burnTime;
    }

    @Override
    public MachineLayout layout() {
        return MachineLayout.FUEL;
    }

    @Override
    public int progress() {
        return burnTime;
    }

    @Override
    public int duration() {
        return Math.max(1, burnDuration);
    }

    @Override
    public int extra() {
        return burnTime;
    }

    @Override
    public int extra2() {
        return burnDuration;
    }

    public static void serverTick(
            Level level,
            BlockPos position,
            BlockState state,
            PrimitiveCombustionEngineBlockEntity entity
    ) {
        entity.tick(state);
    }

    private void tick(BlockState state) {
        boolean wasBurning = burnTime > 0;
        if (burnTime > 0) {
            burnTime--;
        }
        if (burnTime <= 0) {
            int value = burnValue(items.get(FUEL_SLOT));
            if (value > 0) {
                consumeFuel();
                burnTime = value;
                burnDuration = value;
            } else {
                burnDuration = 0;
            }
        }
        boolean burning = burnTime > 0;
        if (state.hasProperty(PrimitiveCombustionEngineBlock.LIT) && state.getValue(PrimitiveCombustionEngineBlock.LIT) != burning) {
            if (level != null) {
                level.setBlock(worldPosition, state.setValue(PrimitiveCombustionEngineBlock.LIT, burning), Block.UPDATE_ALL);
            }
        }
        if (wasBurning != burning) {
            setChanged();
            sync();
        } else if (burning) {
            setChanged();
        }
    }

    private void consumeFuel() {
        ItemStack fuel = items.get(FUEL_SLOT);
        if (fuel.isEmpty()) {
            return;
        }
        if (fuel.hasCraftingRemainingItem() && fuel.getCount() == 1) {
            items.set(FUEL_SLOT, fuel.getCraftingRemainingItem());
            return;
        }
        fuel.shrink(1);
    }

    public static int burnValue(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        return AbstractFurnaceBlockEntity.getFuel().getOrDefault(stack.getItem(), 0);
    }

    public boolean insertFuel(ItemStack stack) {
        if (burnValue(stack) <= 0) {
            return false;
        }
        ItemStack existing = items.get(FUEL_SLOT);
        if (existing.isEmpty()) {
            items.set(FUEL_SLOT, stack.split(Math.min(stack.getCount(), stack.getMaxStackSize())));
            setChanged();
            sync();
            return true;
        }
        if (ItemStack.isSameItemSameTags(existing, stack) && existing.getCount() < existing.getMaxStackSize()) {
            int move = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
            existing.grow(move);
            stack.shrink(move);
            setChanged();
            sync();
            return true;
        }
        return false;
    }

    public ItemStack extractFuel() {
        ItemStack existing = items.get(FUEL_SLOT);
        items.set(FUEL_SLOT, ItemStack.EMPTY);
        setChanged();
        sync();
        return existing;
    }

    public String debugDump() {
        return "engine burning=" + (burnTime > 0)
                + " burn=" + burnTime + "/" + burnDuration
                + " speed=" + driveState().speed()
                + " capacity=" + driveState().availableCapacity()
                + " fuel=" + items.get(FUEL_SLOT);
    }

    public Component status() {
        return Component.translatable(
                "message.alcoholic.engine.status",
                String.format(Locale.ROOT, "%.0f", driveState().speed()),
                burnTime,
                burnDuration
        );
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnDuration", burnDuration);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        burnTime = tag.getInt("BurnTime");
        burnDuration = tag.getInt("BurnDuration");
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

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[]{FUEL_SLOT};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == FUEL_SLOT && burnValue(stack) > 0;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == FUEL_SLOT && stack.is(Items.BUCKET);
    }
}
