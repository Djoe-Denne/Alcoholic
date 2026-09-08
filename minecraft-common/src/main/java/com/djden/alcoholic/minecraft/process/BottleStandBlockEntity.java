package com.djden.alcoholic.minecraft.process;

import com.djden.alcoholic.minecraft.bottle.BeverageBottleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Nine one-bottle display cells owned by one physical stand block. */
public final class BottleStandBlockEntity extends BlockEntity implements Container {
    public static final int SLOT_COUNT = 9;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    public BottleStandBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
        super(type, position, state);
    }

    public static boolean isBottle(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BeverageBottleItem;
    }

    public boolean insertOne(ItemStack source) {
        if (!isBottle(source)) {
            return false;
        }
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (!items.get(slot).isEmpty()) {
                continue;
            }
            ItemStack inserted = source.copy();
            inserted.setCount(1);
            items.set(slot, inserted);
            source.shrink(1);
            changedAndSync();
            return true;
        }
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void changedAndSync() {
        setChanged();
        Level currentLevel = level;
        if (currentLevel != null && !currentLevel.isClientSide) {
            currentLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
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
        if (!removed.isEmpty()) {
            changedAndSync();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!stack.isEmpty() && !isBottle(stack)) {
            return;
        }
        ItemStack stored = stack.copy();
        stored.setCount(Math.min(1, stored.getCount()));
        items.set(slot, stored);
        changedAndSync();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return isBottle(stack);
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            items.set(slot, ItemStack.EMPTY);
        }
        changedAndSync();
    }
}
