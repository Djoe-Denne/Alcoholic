package com.djden.alcoholic.minecraft.machine;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.application.machine.LoadMachineCatalogUseCase;
import com.djden.alcoholic.application.machine.MachineCatalog;
import com.djden.alcoholic.application.machine.MachineCatalogStore;
import com.djden.alcoholic.minecraft.beverage.GsonDataNodes;
import com.djden.alcoholic.minecraft.multiblock.IndustrialRuntime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Datapack overlay for hollow-cuboid machine families.
 * Directory: {@code data/<ns>/alcoholic/machines/*.json}.
 */
public final class MachineDataReloadListener extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private static final LoadMachineCatalogUseCase LOADER = new LoadMachineCatalogUseCase();

    private final MachineCatalogStore store;

    public MachineDataReloadListener() {
        this(IndustrialRuntime.shared().store());
    }

    public MachineDataReloadListener(MachineCatalogStore store) {
        super(GSON, "alcoholic/machines");
        this.store = Objects.requireNonNull(store, "store");
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> resources,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        try {
            Map<ResourceId, DataNode> nodes = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
                ResourceLocation source = entry.getKey();
                nodes.put(new ResourceId(source.getNamespace(), source.getPath()), GsonDataNodes.from(entry.getValue()));
            }
            MachineCatalog catalog = LOADER.load(nodes);
            store.replace(catalog);
            LOGGER.info("Loaded {} industrial machine definition(s)", catalog.machines().size());
        } catch (RuntimeException exception) {
            LOGGER.error("Machine data reload rejected; keeping the previous snapshot", exception);
        }
    }
}
