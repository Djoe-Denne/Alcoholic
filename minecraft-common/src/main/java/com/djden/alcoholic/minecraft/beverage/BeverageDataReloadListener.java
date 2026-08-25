package com.djden.alcoholic.minecraft.beverage;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.application.beverage.BeverageCatalogStore;
import com.djden.alcoholic.application.beverage.LoadBeverageCatalogUseCase;
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
 * Vanilla data-pack listener for JSON below the {@code alcoholic} data directory.
 */
public final class BeverageDataReloadListener extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private static final LoadBeverageCatalogUseCase LOADER = new LoadBeverageCatalogUseCase();

    private final BeverageCatalogStore store;
    private final AlcoholicApi api;

    public BeverageDataReloadListener(BeverageRuntime runtime) {
        this(
                Objects.requireNonNull(runtime, "runtime").store(),
                runtime.api()
        );
    }

    public BeverageDataReloadListener(BeverageCatalogStore store, AlcoholicApi api) {
        super(GSON, "alcoholic");
        this.store = Objects.requireNonNull(store, "store");
        this.api = Objects.requireNonNull(api, "api");
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> resources,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        try {
            BeverageCatalog replacement = parseSnapshot(resources, api);
            store.replace(replacement);
            api.notifyCatalogReloaded();
            LOGGER.info(
                    "Loaded {} beverage definition(s), {} process definition(s), {} ingredient definition(s), {} liquid definition(s)",
                    replacement.beverages().size(),
                    replacement.processes().size(),
                    replacement.ingredients().size(),
                    replacement.liquids().size()
            );
        } catch (RuntimeException exception) {
            LOGGER.error(
                    "Beverage data reload rejected; keeping the previous snapshot",
                    exception
            );
        }
    }

    public static BeverageCatalog parseSnapshot(
            Map<ResourceLocation, JsonElement> resources,
            AlcoholicApi api
    ) {
        Objects.requireNonNull(resources, "resources");
        Objects.requireNonNull(api, "api");
        Map<ResourceId, DataNode> ingredients = new LinkedHashMap<>();
        Map<ResourceId, DataNode> processes = new LinkedHashMap<>();
        Map<ResourceId, DataNode> beverages = new LinkedHashMap<>();
        Map<ResourceId, DataNode> liquids = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            ResourceLocation source = entry.getKey();
            String path = source.getPath();
            ResourceId id = new ResourceId(source.getNamespace(), path);
            DataNode node = GsonDataNodes.from(entry.getValue());
            if (path.startsWith("ingredients/")) {
                ingredients.put(id, node);
            } else if (path.startsWith("processes/")) {
                processes.put(id, node);
            } else if (path.startsWith("beverages/")) {
                beverages.put(id, node);
            } else if (path.startsWith("liquids/")) {
                liquids.put(id, node);
            }
        }
        return LOADER.load(ingredients, processes, beverages, liquids, api);
    }
}
