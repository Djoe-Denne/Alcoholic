package com.djden.alcoholic.minecraft.energy;

/**
 * Block entity that stores Forge-Energy-equivalent units without importing
 * Forge types. The platform attaches {@code IEnergyStorage} over this buffer.
 */
public interface EnergyHolder {
    EnergyBuffer energy();
}
