package com.djden.alcoholic.minecraft.fluid;

import com.djden.alcoholic.platform.api.registry.RegistryRef;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * Platform boundary for registering a complete fluid bundle.
 */
@FunctionalInterface
public interface FluidRegistrationPort {
    RegistryRef<FlowingFluid> register(FluidDefinition definition);
}
