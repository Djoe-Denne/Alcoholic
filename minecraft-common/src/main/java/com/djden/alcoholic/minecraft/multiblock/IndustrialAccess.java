package com.djden.alcoholic.minecraft.multiblock;

/**
 * I/O policy while the logical tank stays on the controller.
 */
public enum IndustrialAccess {
    OPEN,
    DRAIN_ONLY,
    CLOSED;

    public boolean canFill() {
        return this == OPEN;
    }

    public boolean canDrain() {
        return this == OPEN || this == DRAIN_ONLY;
    }

    public boolean canProcess() {
        return this == OPEN;
    }
}
