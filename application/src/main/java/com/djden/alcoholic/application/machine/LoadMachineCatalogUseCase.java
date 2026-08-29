package com.djden.alcoholic.application.machine;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.domain.multiblock.MultiblockDefinition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class LoadMachineCatalogUseCase {
    public MachineCatalog load(Map<ResourceId, DataNode> resources) {
        Objects.requireNonNull(resources, "resources");
        Map<ResourceId, MultiblockDefinition> machines = new LinkedHashMap<>(MachineCatalog.builtins().machines());
        resources.forEach((id, node) -> {
            ResourceId fallback = fallbackId(id);
            machines.put(fallback, MachineDefinitionCodec.INSTANCE.decode(node, id.toString(), fallback));
        });
        return new MachineCatalog(machines);
    }

    private static ResourceId fallbackId(ResourceId source) {
        String path = source.path();
        int separator = path.lastIndexOf('/');
        return new ResourceId(source.namespace(), separator >= 0 ? path.substring(separator + 1) : path);
    }
}
