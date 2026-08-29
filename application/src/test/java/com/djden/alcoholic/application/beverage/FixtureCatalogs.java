package com.djden.alcoholic.application.beverage;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.data.JsonDataParser;
import com.djden.alcoholic.application.quality.ShippedQualityGraphs;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FixtureCatalogs {
    private FixtureCatalogs() {
    }

    public static Map<ResourceId, DataNode> ingredients() {
        return Map.of(source("testpack", "ingredients/apple"), read("data/testpack/alcoholic/ingredients/apple.json"));
    }

    public static Map<ResourceId, DataNode> processes() {
        Map<ResourceId, DataNode> processes = new LinkedHashMap<>();
        processes.put(source("testpack", "processes/press_fruit"), read("data/testpack/alcoholic/processes/press_fruit.json"));
        processes.put(source("testpack", "processes/age_new_make"), read("data/testpack/alcoholic/processes/age_new_make.json"));
        return processes;
    }

    public static Map<ResourceId, DataNode> quality() {
        return ShippedQualityGraphs.sources();
    }

    public static Map<ResourceId, DataNode> liquids() {
        Map<ResourceId, DataNode> liquids = new LinkedHashMap<>();
        liquids.put(source("testpack", "liquids/apple_must"), read("data/testpack/alcoholic/liquids/apple_must.json"));
        liquids.put(source("testpack", "liquids/young_cider"), read("data/testpack/alcoholic/liquids/young_cider.json"));
        liquids.put(source("testpack", "liquids/aged_cider"), read("data/testpack/alcoholic/liquids/aged_cider.json"));
        liquids.put(source("testpack", "liquids/new_make_spirit"), read("data/testpack/alcoholic/liquids/new_make_spirit.json"));
        liquids.put(source("testpack", "liquids/whisky"), read("data/testpack/alcoholic/liquids/whisky.json"));
        liquids.put(source("testpack", "liquids/beer"), read("data/testpack/alcoholic/liquids/beer.json"));
        liquids.put(source("testpack", "liquids/barrel_aged_beer"), read("data/testpack/alcoholic/liquids/barrel_aged_beer.json"));
        liquids.put(source("testpack", "liquids/rum_spirit"), read("data/testpack/alcoholic/liquids/rum_spirit.json"));
        liquids.put(source("testpack", "liquids/aged_rum"), read("data/testpack/alcoholic/liquids/aged_rum.json"));
        return liquids;
    }

    static Map<ResourceId, DataNode> acceptanceBeverages() {
        Map<ResourceId, DataNode> beverages = new LinkedHashMap<>();
        put(beverages, "red_wine");
        put(beverages, "cider");
        put(beverages, "beer");
        put(beverages, "whisky");
        put(beverages, "rum");
        put(beverages, "spirit");
        put(beverages, "fruit_liqueur");
        put(beverages, "wheat_beer");
        put(beverages, "grain_mash");
        return beverages;
    }

    static Map<ResourceId, DataNode> addonBeverages() {
        return Map.of(
                source("testaddon", "beverages/polished_rice_wash"),
                read("data/testaddon/alcoholic/beverages/polished_rice_wash.json")
        );
    }

    public static DataNode read(String classpath) {
        try (InputStream input = FixtureCatalogs.class.getClassLoader().getResourceAsStream(classpath)) {
            if (input == null) {
                throw new IllegalArgumentException("Missing fixture " + classpath);
            }
            return JsonDataParser.parse(new String(input.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static void put(Map<ResourceId, DataNode> beverages, String name) {
        beverages.put(
                source("testpack", "beverages/" + name),
                read("data/testpack/alcoholic/beverages/" + name + ".json")
        );
    }

    private static ResourceId source(String namespace, String path) {
        return new ResourceId(namespace, path);
    }
}
