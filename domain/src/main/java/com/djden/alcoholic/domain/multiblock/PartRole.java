package com.djden.alcoholic.domain.multiblock;

/**
 * Generic multiblock part vocabulary. Roles are machine-agnostic.
 */
public enum PartRole {
    CASING,
    CONTROLLER,
    FLUID_PORT,
    ITEM_PORT,
    KINETIC_PORT,
    WINDOW,
    HATCH;

    public boolean countsAsShell() {
        return true;
    }

    public boolean isPort() {
        return this == FLUID_PORT || this == ITEM_PORT || this == KINETIC_PORT;
    }
}
