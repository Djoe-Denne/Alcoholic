package com.djden.alcoholic.minecraft.fluid;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.platform.api.registry.RegistryRef;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class FluidContentRegistrar {
    private FluidContentRegistrar() {
    }

    public static FluidContent register(FluidRegistrationPort port) {
        Objects.requireNonNull(port, "port");
        Map<ResourceId, RegistryRef<FlowingFluid>> sources = new LinkedHashMap<>();
        for (FluidDefinition definition : BuiltinFluidDefinitions.all()) {
            RegistryRef<FlowingFluid> source = Objects.requireNonNull(
                    port.register(definition),
                    () -> "Fluid registrar returned null for " + definition.id()
            );
            RegistryRef<FlowingFluid> previous = sources.putIfAbsent(definition.id(), source);
            if (previous != null) {
                throw new IllegalStateException("Duplicate built-in fluid " + definition.id());
            }
        }
        return new FluidContent(sources);
    }
}
