package com.djden.alcoholic.minecraft.multiblock;

import com.djden.alcoholic.domain.multiblock.PortMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class ItemPortBlockEntity extends PartBlockEntity implements WorldlyContainer {
    public ItemPortBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
        super(type, position, state);
    }

    public PortMode mode() {
        return getBlockState().hasProperty(PortBlocks.MODE)
                ? getBlockState().getValue(PortBlocks.MODE).domain()
                : PortMode.BOTH;
    }

    private WorldlyContainer delegate() {
        MultiblockControllerBlockEntity controller = controller();
        return controller == null || !controller.access().canProcess() && !controller.access().canDrain()
                ? EmptyContainer.INSTANCE
                : controller;
    }

    @Override
    public int getContainerSize() {
        return delegate().getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return delegate().getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (!mode().allowsExtract()) {
            return ItemStack.EMPTY;
        }
        return delegate().removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (!mode().allowsExtract()) {
            return ItemStack.EMPTY;
        }
        return delegate().removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (mode().allowsInsert() && controller() != null && controller().access().canFill()) {
            delegate().setItem(slot, stack);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        delegate().clearContent();
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return delegate().getSlotsForFace(direction);
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return mode().allowsInsert()
                && controller() != null
                && controller().access().canFill()
                && delegate().canPlaceItemThroughFace(slot, stack, direction);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return mode().allowsExtract() && delegate().canTakeItemThroughFace(slot, stack, direction);
    }

    @Override
    public int getMaxStackSize() {
        return 512;
    }

    private enum EmptyContainer implements WorldlyContainer {
        INSTANCE;

        @Override
        public int getContainerSize() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
        }

        @Override
        public void setChanged() {
        }

        @Override
        public int[] getSlotsForFace(Direction direction) {
            return new int[0];
        }

        @Override
        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
            return false;
        }

        @Override
        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
            return false;
        }
    }
}
