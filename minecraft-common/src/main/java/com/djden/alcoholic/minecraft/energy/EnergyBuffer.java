package com.djden.alcoholic.minecraft.energy;

/**
 * Loader-independent FE-like buffer. Forge (or another loader) exposes this
 * through its energy capability; Alcoholic machines never import that API.
 */
public final class EnergyBuffer {
    private final int capacity;
    private final int maxReceive;
    private final int maxExtract;
    private int stored;

    public EnergyBuffer(int capacity, int maxReceive, int maxExtract) {
        this.capacity = Math.max(0, capacity);
        this.maxReceive = Math.max(0, maxReceive);
        this.maxExtract = Math.max(0, maxExtract);
    }

    public int capacity() {
        return capacity;
    }

    public int maxReceive() {
        return maxReceive;
    }

    public int maxExtract() {
        return maxExtract;
    }

    public int stored() {
        return stored;
    }

    public void setStored(int value) {
        stored = clamp(value);
    }

    public int receive(int amount, boolean simulate) {
        if (amount <= 0 || maxReceive <= 0) {
            return 0;
        }
        int accepted = Math.min(amount, Math.min(maxReceive, capacity - stored));
        if (!simulate) {
            stored += accepted;
        }
        return accepted;
    }

    public int extract(int amount, boolean simulate) {
        if (amount <= 0 || maxExtract <= 0) {
            return 0;
        }
        int removed = Math.min(amount, Math.min(maxExtract, stored));
        if (!simulate) {
            stored -= removed;
        }
        return removed;
    }

    public boolean isEmpty() {
        return stored <= 0;
    }

    private int clamp(int value) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, capacity);
    }
}
