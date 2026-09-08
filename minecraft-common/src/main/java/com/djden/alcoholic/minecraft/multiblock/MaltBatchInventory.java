package com.djden.alcoholic.minecraft.multiblock;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/**
 * Multi-slot malt batches. Output is split at the item's vanilla max stack.
 * Slot projections use {@code 0} empty, {@code >0} matching count, {@code <0}
 * incompatible.
 */
final class MaltBatchInventory {
    private MaltBatchInventory() {
    }

    static ItemStack firstInput(NonNullList<ItemStack> items, int inputCount) {
        for (int slot = 0; slot < inputCount; slot++) {
            ItemStack stack = items.get(slot);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    static int countMatching(NonNullList<ItemStack> items, int inputCount, ItemStack sample) {
        if (sample.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (int slot = 0; slot < inputCount; slot++) {
            ItemStack stack = items.get(slot);
            if (ItemStack.isSameItemSameTags(stack, sample)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    static void consumeMatching(NonNullList<ItemStack> items, int inputCount, ItemStack sample, int amount) {
        int[] slots = project(items, 0, inputCount, sample);
        takeMatching(slots, amount);
        apply(items, 0, sample, slots);
    }

    static int outputSpace(NonNullList<ItemStack> items, int outputStart, int outputCount, ItemStack sample) {
        if (sample.isEmpty() || outputCount < 1) {
            return 0;
        }
        int limit = Math.max(1, sample.getMaxStackSize());
        int[] slots = project(items, outputStart, outputCount, sample);
        return space(slots, limit);
    }

    static boolean insertOutputs(NonNullList<ItemStack> items, int outputStart, int outputCount, ItemStack produced) {
        if (produced.isEmpty()) {
            return true;
        }
        int limit = Math.max(1, produced.getMaxStackSize());
        int[] slots = project(items, outputStart, outputCount, produced);
        if (fillMatching(slots, limit, produced.getCount()) != 0) {
            return false;
        }
        apply(items, outputStart, produced, slots);
        return true;
    }

    static int space(int[] slots, int limit) {
        int total = 0;
        for (int slot : slots) {
            if (slot < 0) {
                continue;
            }
            total += slot == 0 ? limit : Math.max(0, limit - slot);
        }
        return total;
    }

    static int fillMatching(int[] slots, int limit, int amount) {
        int remaining = Math.max(0, amount);
        for (int index = 0; index < slots.length && remaining > 0; index++) {
            if (slots[index] <= 0) {
                continue;
            }
            int move = Math.min(remaining, Math.max(0, limit - slots[index]));
            slots[index] += move;
            remaining -= move;
        }
        for (int index = 0; index < slots.length && remaining > 0; index++) {
            if (slots[index] != 0) {
                continue;
            }
            int move = Math.min(remaining, limit);
            slots[index] = move;
            remaining -= move;
        }
        return remaining;
    }

    static int takeMatching(int[] slots, int amount) {
        int remaining = Math.max(0, amount);
        for (int index = 0; index < slots.length && remaining > 0; index++) {
            if (slots[index] <= 0) {
                continue;
            }
            int take = Math.min(remaining, slots[index]);
            slots[index] -= take;
            remaining -= take;
        }
        return remaining;
    }

    private static int[] project(NonNullList<ItemStack> items, int start, int count, ItemStack sample) {
        int[] slots = new int[count];
        for (int index = 0; index < count; index++) {
            ItemStack stack = items.get(start + index);
            if (stack.isEmpty()) {
                slots[index] = 0;
            } else if (ItemStack.isSameItemSameTags(stack, sample)) {
                slots[index] = stack.getCount();
            } else {
                slots[index] = -1;
            }
        }
        return slots;
    }

    private static void apply(NonNullList<ItemStack> items, int start, ItemStack sample, int[] slots) {
        for (int index = 0; index < slots.length; index++) {
            int count = slots[index];
            if (count < 0) {
                continue;
            }
            if (count == 0) {
                items.set(start + index, ItemStack.EMPTY);
                continue;
            }
            ItemStack existing = items.get(start + index);
            if (existing.isEmpty() || !ItemStack.isSameItemSameTags(existing, sample)) {
                ItemStack placed = sample.copy();
                placed.setCount(count);
                items.set(start + index, placed);
            } else {
                existing.setCount(count);
            }
        }
    }
}
