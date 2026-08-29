package com.djden.alcoholic.application.machine;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.multiblock.MultiblockDefinition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class MachineCatalog {
    private final Map<ResourceId, MultiblockDefinition> machines;

    public MachineCatalog(Map<ResourceId, MultiblockDefinition> machines) {
        this.machines = Map.copyOf(Objects.requireNonNull(machines, "machines"));
    }

    public static MachineCatalog builtins() {
        Map<ResourceId, MultiblockDefinition> machines = new LinkedHashMap<>(BuiltinMachines.all());
        machines.putAll(BuiltinCraftMachines.all());
        return new MachineCatalog(machines);
    }

    public Optional<MultiblockDefinition> get(ResourceId id) {
        return Optional.ofNullable(machines.get(id));
    }

    public Map<ResourceId, MultiblockDefinition> machines() {
        return machines;
    }
}
