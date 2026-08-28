package com.djden.alcoholic.minecraft.fluid;

/**
 * Loader-neutral physical properties for a placeable Minecraft fluid.
 */
public record FluidFlowProfile(
        int density,
        int temperature,
        int viscosity,
        int tickRate,
        int slopeFindDistance,
        int levelDecreasePerBlock,
        boolean renewableSources
) {
    public FluidFlowProfile {
        if (density <= 0) {
            throw new IllegalArgumentException("Fluid density must be positive");
        }
        if (temperature <= 0) {
            throw new IllegalArgumentException("Fluid temperature must be positive");
        }
        if (viscosity < 0) {
            throw new IllegalArgumentException("Fluid viscosity cannot be negative");
        }
        if (tickRate <= 0) {
            throw new IllegalArgumentException("Fluid tick rate must be positive");
        }
        if (slopeFindDistance <= 0) {
            throw new IllegalArgumentException("Fluid slope distance must be positive");
        }
        if (levelDecreasePerBlock <= 0) {
            throw new IllegalArgumentException("Fluid level decrease must be positive");
        }
    }
}
