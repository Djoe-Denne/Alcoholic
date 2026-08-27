package com.djden.alcoholic.forge.jei;

import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.minecraft.beverage.BeverageDataReloadListener;
import com.djden.alcoholic.minecraft.beverage.BeverageRuntime;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.locating.IModFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Live catalog after datapack reload; otherwise client resources, then the
 * Alcoholic jar. World-only datapacks on a dedicated server are not visible
 * until a catalog sync exists.
 */
final class ClientProcessCatalog {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientProcessCatalog.class);

    private ClientProcessCatalog() {
    }

    static BeverageCatalog load() {
        BeverageCatalog live = BeverageRuntime.shared().catalog();
        if (!live.processes().isEmpty()) {
            return live;
        }
        BeverageCatalog resources = loadFromResourceManager();
        if (!resources.processes().isEmpty()) {
            return resources;
        }
        return loadFromModJar();
    }

    private static BeverageCatalog loadFromResourceManager() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft == null) {
                return BeverageCatalog.empty();
            }
            ResourceManager manager = minecraft.getResourceManager();
            Map<ResourceLocation, JsonElement> resources = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, Resource> entry : manager.listResources(
                    "alcoholic",
                    location -> location.getPath().endsWith(".json")
            ).entrySet()) {
                indexResource(entry.getKey(), entry.getValue(), resources);
            }
            return BeverageDataReloadListener.parseSnapshot(resources, BeverageRuntime.shared().api());
        } catch (RuntimeException exception) {
            LOGGER.warn("JEI could not read beverage resources from the resource manager", exception);
            return BeverageCatalog.empty();
        }
    }

    private static void indexResource(
            ResourceLocation id,
            Resource resource,
            Map<ResourceLocation, JsonElement> resources
    ) {
        try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
            resources.put(normalize(id), JsonParser.parseReader(reader));
        } catch (Exception exception) {
            LOGGER.warn("Skipped unreadable beverage resource {}", id, exception);
        }
    }

    private static ResourceLocation normalize(ResourceLocation id) {
        String path = id.getPath();
        if (path.startsWith("alcoholic/")) {
            path = path.substring("alcoholic/".length());
        }
        if (path.endsWith(".json")) {
            path = path.substring(0, path.length() - 5);
        }
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), path);
    }

    private static BeverageCatalog loadFromModJar() {
        try {
            IModFile file = ModList.get().getModFileById("alcoholic").getFile();
            Path root = file.findResource("data");
            if (!Files.isDirectory(root)) {
                LOGGER.warn("JEI jar fallback found no data directory in the Alcoholic mod file");
                return BeverageCatalog.empty();
            }
            Map<ResourceLocation, JsonElement> resources = new LinkedHashMap<>();
            try (Stream<Path> walk = Files.walk(root)) {
                walk.filter(path -> path.getFileName().toString().endsWith(".json"))
                        .forEach(path -> indexJar(root, path, resources));
            }
            return BeverageDataReloadListener.parseSnapshot(resources, BeverageRuntime.shared().api());
        } catch (Exception exception) {
            LOGGER.warn("JEI jar fallback failed", exception);
            return BeverageCatalog.empty();
        }
    }

    private static void indexJar(Path dataRoot, Path file, Map<ResourceLocation, JsonElement> resources) {
        try {
            Path relative = dataRoot.relativize(file);
            if (relative.getNameCount() < 3) {
                return;
            }
            String namespace = relative.getName(0).toString();
            String folder = relative.getName(1).toString();
            if (!"alcoholic".equals(folder)) {
                return;
            }
            StringBuilder path = new StringBuilder();
            for (int index = 2; index < relative.getNameCount(); index++) {
                if (index > 2) {
                    path.append('/');
                }
                path.append(relative.getName(index));
            }
            String jsonPath = path.toString();
            if (!jsonPath.endsWith(".json")) {
                return;
            }
            jsonPath = jsonPath.substring(0, jsonPath.length() - 5);
            try (Reader reader = Files.newBufferedReader(file)) {
                resources.put(
                        ResourceLocation.fromNamespaceAndPath(namespace, jsonPath),
                        JsonParser.parseReader(reader)
                );
            }
        } catch (Exception exception) {
            LOGGER.warn("Skipped unreadable jar beverage file {}", file, exception);
        }
    }
}
