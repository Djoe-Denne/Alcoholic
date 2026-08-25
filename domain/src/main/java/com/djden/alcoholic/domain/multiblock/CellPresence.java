package com.djden.alcoholic.domain.multiblock;

/**
 * What a validator may observe at one cell without knowing a world type.
 */
public enum CellPresence {
    AIR,
    STRUCTURE,
    OBSTRUCTION,
    UNLOADED
}
