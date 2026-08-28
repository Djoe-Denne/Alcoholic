package com.djden.alcoholic.minecraft.fluid;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.platform.api.registry.RegistryRef;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Registered Minecraft fluids exposed without leaking a loader-specific registry handle.
 */
public final class FluidContent {
    private final Map<ResourceId, RegistryRef<FlowingFluid>> sources;

    FluidContent(Map<ResourceId, RegistryRef<FlowingFluid>> sources) {
        Objects.requireNonNull(sources, "sources");
        this.sources = Map.copyOf(new LinkedHashMap<>(sources));
    }

    public Fluid source(ResourceId id) {
        RegistryRef<FlowingFluid> source = sources.get(id);
        return source == null ? null : source.get();
    }

    public Set<ResourceId> ids() {
        return sources.keySet();
    }
}
