package com.djden.alcoholic.forge;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratedResourceContractTest {
    private static final String[] STAGES = {
            "planted",
            "establishing",
            "vegetative",
            "flowering",
            "green_fruit",
            "ripening",
            "harvest_ready",
            "dormant"
    };

    @Test
    void vineryTagsAreOptionalSemanticContributions() throws IOException {
        JsonObject red = resource("data/alcoholic/tags/items/grapes/red.json");
        JsonObject white = resource("data/alcoholic/tags/items/grapes/white.json");

        assertOptionalTag(red, "#vinery:red_grape");
        assertOptionalTag(white, "#vinery:white_grape");
    }

    @Test
    void worldgenIsConditionallyDisabledWithoutUnregisteringContent() throws IOException {
        JsonObject modifier = resource(
                "data/alcoholic/forge/biome_modifier/wild_grapevines.json"
        );
        JsonObject condition = modifier.getAsJsonArray("forge:conditions")
                .get(0)
                .getAsJsonObject();

        assertEquals("forge:not", condition.get("type").getAsString());
        JsonObject nested = condition.getAsJsonObject("value");
        assertEquals("forge:mod_loaded", nested.get("type").getAsString());
        assertEquals("vinery", nested.get("modid").getAsString());
        assertEquals("forge:add_features", modifier.get("type").getAsString());
    }

    @Test
    void barleyWorldgenIsConditionallyDisabledWhenBreweryIsPresent() throws IOException {
        JsonObject modifier = resource(
                "data/alcoholic/forge/biome_modifier/wild_barley.json"
        );
        JsonObject condition = modifier.getAsJsonArray("forge:conditions")
                .get(0)
                .getAsJsonObject();
        assertEquals("forge:not", condition.get("type").getAsString());
        JsonObject nested = condition.getAsJsonObject("value");
        assertEquals("alcoholic:item_present", nested.get("type").getAsString());
        assertEquals("brewery:barley", nested.get("item").getAsString());
    }

    @Test
    void viticultureProfilesMatchRuntimeLoaderContract() throws IOException {
        JsonObject settings = resource("data/alcoholic/viticulture/settings.json");
        JsonObject red = resource("data/alcoholic/viticulture/red_grape.json");
        JsonObject white = resource("data/alcoholic/viticulture/white_grape.json");

        assertTraining(settings, "untrained", 0.70, 0.85);
        assertTraining(settings, "trained", 1.0, 1.0);
        assertClimate(red, 25.0, 0.50);
        assertClimate(white, 18.0, 0.65);
        assertPruning(red);
        assertPruning(white);
    }

    @Test
    void vinesProvideEightTrainedAndUntrainedAssetStages() throws IOException {
        for (String color : new String[]{"red", "white"}) {
            JsonObject variants = resource(
                    "assets/alcoholic/blockstates/" + color + "_grapevine.json"
            ).getAsJsonObject("variants");
            assertEquals(32, variants.size());
            for (String stage : STAGES) {
                for (boolean trained : new boolean[]{false, true}) {
                    for (boolean extended : new boolean[]{false, true}) {
                        String key = "extended=" + extended + ",stage=" + stage
                                + ",trained=" + trained;
                        assertTrue(variants.has(key), "Missing variant " + key);
                    }
                    resource(
                            "assets/alcoholic/models/block/" + color
                                    + "_grapevine_" + stage + "_untrained.json"
                    );
                    resource(
                            "assets/alcoholic/models/block/" + color
                                    + "_grapevine_" + stage + "_trained.json"
                    );
                    resource(
                            "assets/alcoholic/models/block/" + color
                                    + "_grapevine_" + stage + "_base.json"
                    );
                    resource(
                            "assets/alcoholic/models/block/" + color
                                    + "_grapevine_stem_" + stage + "_trained.json"
                    );
                    resource(
                            "assets/alcoholic/models/block/" + color
                                    + "_grapevine_stem_" + stage + "_untrained.json"
                    );
                    resource(
                            "assets/alcoholic/models/block/" + color
                                    + "_grapevine_canopy_" + stage + ".json"
                    );
                }
            }
            JsonObject stemVariants = resource(
                    "assets/alcoholic/blockstates/" + color + "_grapevine_stem.json"
            ).getAsJsonObject("variants");
            assertEquals(16, stemVariants.size());
            JsonObject canopyVariants = resource(
                    "assets/alcoholic/blockstates/" + color + "_grapevine_canopy.json"
            ).getAsJsonObject("variants");
            assertEquals(16, canopyVariants.size());
            assertTrue(
                    variants.keySet().stream().noneMatch(
                            key -> key.startsWith("age=") || key.contains(",age=")
                    ),
                    "Legacy age must not duplicate every visual variant"
            );
        }
    }

    @Test
    void everyGeneratedModelAndTextureDependencyExists() throws IOException {
        for (String color : new String[]{"red", "white"}) {
            for (String stage : STAGES) {
                assertPng16("assets/alcoholic/textures/block/" + color
                        + "_grapevine_" + stage + ".png");
            }
            for (String item : new String[]{
                    color + "_grapes",
                    color + "_grape_cutting"
            }) {
                resource("assets/alcoholic/models/item/" + item + ".json");
                assertPng16("assets/alcoholic/textures/item/" + item + ".png");
            }
        }

        for (String block : new String[]{
                "vineyard_post",
                "end_post",
                "trellis_wire"
        }) {
            resource("assets/alcoholic/blockstates/" + block + ".json");
            resource("assets/alcoholic/models/block/" + block + ".json");
            resource("assets/alcoholic/models/item/" + block + ".json");
            assertPng16("assets/alcoholic/textures/block/" + block + ".png");
        }
        for (String item : new String[]{"trellis_spool", "pruning_shears"}) {
            resource("assets/alcoholic/models/item/" + item + ".json");
            assertPng16("assets/alcoholic/textures/item/" + item + ".png");
        }
        for (int age = 0; age <= 2; age++) {
            resource("assets/alcoholic/models/block/barley_crop_" + age + ".json");
            resource("assets/alcoholic/models/block/hop_bine_" + age + ".json");
            assertPng16("assets/alcoholic/textures/block/barley_crop_" + age + ".png");
            assertPng16("assets/alcoholic/textures/block/hop_bine_" + age + ".png");
        }
        resource("assets/alcoholic/blockstates/barley_crop.json");
        resource("assets/alcoholic/blockstates/hop_bine.json");
        for (String block : new String[]{
                "artisanal_press",
                "artisanal_fermenter",
                "oak_barrel",
                "artisanal_blending_crock",
                "brewing_kettle",
                "industrial_casing",
                "machine_window",
                "access_hatch",
                "fluid_port",
                "item_port",
                "kinetic_port",
                "industrial_press_controller",
                "industrial_vat_controller",
                "industrial_tank_controller",
                "industrial_malt_house_controller",
                "industrial_roller_mill_controller",
                "industrial_mash_tun_controller",
                "industrial_brewing_kettle_controller",
                "industrial_conditioning_vessel_controller"
        }) {
            resource("assets/alcoholic/blockstates/" + block + ".json");
            resource("assets/alcoholic/models/block/" + block + ".json");
            resource("assets/alcoholic/models/item/" + block + ".json");
            assertPng16("assets/alcoholic/textures/block/" + block + ".png");
        }
        for (String item : new String[]{
                "yeast",
                "grape_pomace",
                "red_grape_must_bucket",
                "white_grape_must_bucket",
                "young_red_wine_bucket",
                "young_white_wine_bucket",
                "red_wine_bucket",
                "white_wine_bucket",
                "empty_bottle",
                "beverage_bottle",
                "barley",
                "barley_seeds",
                "malted_barley",
                "grist",
                "hops",
                "hop_rhizome",
                "spent_grain",
                "wort_bucket",
                "hopped_wort_bucket",
                "beer_bucket"
        }) {
            resource("assets/alcoholic/models/item/" + item + ".json");
            assertPng16("assets/alcoholic/textures/item/" + item + ".png");
        }
    }

    @Test
    void englishAndFrenchCoverInspectionAndHarvestLotText() throws IOException {
        for (String language : new String[]{"en_us", "fr_fr"}) {
            JsonObject translations = resource(
                    "assets/alcoholic/lang/" + language + ".json"
            );
            for (String key : new String[]{
                    "block.alcoholic.red_grapevine",
                    "block.alcoholic.white_grapevine",
                    "block.alcoholic.red_grapevine_stem",
                    "block.alcoholic.white_grapevine_stem",
                    "block.alcoholic.red_grapevine_canopy",
                    "block.alcoholic.white_grapevine_canopy",
                    "item.alcoholic.red_grape_cutting",
                    "item.alcoholic.white_grape_cutting",
                    "message.alcoholic.vine.inspect",
                    "message.alcoholic.vine.stage.dormant",
                    "message.alcoholic.vine.pruning.balanced",
                    "tooltip.alcoholic.harvest_lot.quality",
                    "tooltip.alcoholic.harvest_lot.quality.exceptional",
                    "tooltip.alcoholic.harvest_lot.level.balanced",
                    "block.alcoholic.artisanal_press",
                    "message.alcoholic.fermenter.status",
                    "block.alcoholic.oak_barrel",
                    "message.alcoholic.barrel.status",
                    "block.alcoholic.artisanal_blending_crock",
                    "item.alcoholic.empty_bottle",
                    "tooltip.alcoholic.metadata.lost",
                    "command.alcoholic.inspect.nothing",
                    "command.alcoholic.debug.place.line",
                    "command.alcoholic.debug.place.machine",
                    "command.alcoholic.debug.place.unknown",
                    "block.alcoholic.industrial_casing",
                    "block.alcoholic.industrial_press_controller",
                    "block.alcoholic.industrial_vat_controller",
                    "block.alcoholic.industrial_tank_controller",
                    "block.alcoholic.industrial_malt_house_controller",
                    "block.alcoholic.industrial_roller_mill_controller",
                    "block.alcoholic.industrial_mash_tun_controller",
                    "block.alcoholic.industrial_brewing_kettle_controller",
                    "block.alcoholic.industrial_conditioning_vessel_controller",
                    "message.alcoholic.port.mode",
                    "block.alcoholic.malting_floor",
                    "block.alcoholic.mash_tun",
                    "block.alcoholic.brewing_kettle",
                    "item.alcoholic.barley",
                    "item.alcoholic.hops",
                    "message.alcoholic.mash.status",
                    "container.alcoholic.machine",
                    "gui.alcoholic.temperature",
                    "tooltip.alcoholic.gauge.fluid"
            }) {
                assertTrue(
                        translations.has(key),
                        "Missing " + language + " translation " + key
                );
            }
        }
    }

    @Test
    void infrastructureRecipesLootAndCropTagsAreGenerated() throws IOException {
        resource("data/alcoholic/recipes/vineyard_post.json");
        resource("data/alcoholic/recipes/end_post.json");
        resource("data/alcoholic/recipes/trellis_spool.json");
        resource("data/alcoholic/recipes/pruning_shears.json");
        resource("data/alcoholic/recipes/artisanal_press.json");
        resource("data/alcoholic/recipes/artisanal_fermenter.json");
        resource("data/alcoholic/recipes/oak_barrel.json");
        resource("data/alcoholic/recipes/artisanal_blending_crock.json");
        resource("data/alcoholic/recipes/malting_floor.json");
        resource("data/alcoholic/recipes/mash_tun.json");
        resource("data/alcoholic/recipes/brewing_kettle.json");
        resource("data/alcoholic/recipes/empty_bottle.json");
        resource("data/alcoholic/recipes/yeast.json");
        resource("data/alcoholic/tags/items/grapes.json");
        resource("data/alcoholic/tags/items/barley.json");
        resource("data/alcoholic/tags/items/barley/seeds.json");
        resource("data/alcoholic/tags/items/malted_barley.json");
        resource("data/alcoholic/tags/items/malted_grain.json");
        resource("data/alcoholic/tags/items/grist.json");
        resource("data/alcoholic/tags/items/hops.json");
        resource("data/alcoholic/tags/items/spent_grain.json");
        resource("data/alcoholic/tags/items/yeast.json");

        JsonObject crops = resource("data/minecraft/tags/blocks/crops.json");
        assertTrue(crops.getAsJsonArray("values").toString()
                .contains("alcoholic:red_grapevine"));
        resource("data/forge/tags/blocks/crops.json");
        resource("data/minecraft/tags/items/villager_plantable_seeds.json");

        JsonObject redLoot = resource(
                "data/alcoholic/loot_tables/blocks/red_grapevine.json"
        );
        assertTrue(redLoot.toString().contains("alcoholic:red_grape_cutting"));
        assertFalse(redLoot.toString().contains("alcoholic:red_grapes"));
        resource("data/alcoholic/loot_tables/blocks/white_grapevine.json");
        assertEquals(
                0,
                resource("data/alcoholic/loot_tables/blocks/trellis_wire.json")
                        .getAsJsonArray("pools")
                        .size()
        );
        assertEquals(
                0,
                resource("data/alcoholic/loot_tables/blocks/red_grapevine_stem.json")
                        .getAsJsonArray("pools")
                        .size()
        );
        assertEquals(
                0,
                resource("data/alcoholic/loot_tables/blocks/white_grapevine_stem.json")
                        .getAsJsonArray("pools")
                        .size()
        );
        assertEquals(
                0,
                resource("data/alcoholic/loot_tables/blocks/red_grapevine_canopy.json")
                        .getAsJsonArray("pools")
                        .size()
        );
        assertEquals(
                0,
                resource("data/alcoholic/loot_tables/blocks/white_grapevine_canopy.json")
                        .getAsJsonArray("pools")
                        .size()
        );
        resource("data/alcoholic/loot_tables/blocks/vineyard_post.json");
        resource("data/alcoholic/loot_tables/blocks/end_post.json");
        resource("data/alcoholic/loot_tables/blocks/artisanal_press.json");
        resource("data/alcoholic/loot_tables/blocks/artisanal_fermenter.json");
        resource("data/alcoholic/loot_tables/blocks/oak_barrel.json");
        resource("data/alcoholic/loot_tables/blocks/artisanal_blending_crock.json");
        resource("data/alcoholic/recipes/industrial_casing.json");
        resource("data/alcoholic/recipes/fluid_port.json");
        resource("data/alcoholic/recipes/industrial_press_controller.json");
        resource("data/alcoholic/recipes/industrial_malt_house_controller.json");
        resource("data/alcoholic/recipes/industrial_roller_mill_controller.json");
        resource("data/alcoholic/recipes/industrial_mash_tun_controller.json");
        resource("data/alcoholic/recipes/industrial_brewing_kettle_controller.json");
        resource("data/alcoholic/recipes/industrial_conditioning_vessel_controller.json");
        resource("data/alcoholic/tags/blocks/industrial_tank_casing.json");
        resource("data/alcoholic/tags/blocks/fermenter_casing.json");
        resource("data/alcoholic/tags/blocks/pressure_safe_casing.json");
        resource("data/alcoholic/tags/blocks/valid_machine_windows.json");
        resource("data/alcoholic/tags/blocks/industrial_ports.json");
        resource("data/alcoholic/alcoholic/processes/malt_pale.json");
        resource("data/alcoholic/alcoholic/processes/mill_malted_grain.json");
        resource("data/alcoholic/alcoholic/processes/mash_wort.json");
        resource("data/alcoholic/alcoholic/processes/boil_wort.json");
        resource("data/alcoholic/alcoholic/processes/ferment_hopped_wort.json");
        resource("data/alcoholic/alcoholic/processes/condition_beer.json");
        resource("data/alcoholic/alcoholic/beverages/beer.json");
        resource("data/alcoholic/alcoholic/liquids/wort.json");
        JsonObject barley = resource("data/alcoholic/tags/items/barley.json");
        assertTrue(barley.getAsJsonArray("values").size() > 0);
        JsonObject hops = resource("data/alcoholic/tags/items/hops.json");
        assertTrue(hops.getAsJsonArray("values").size() > 0);
        resource("data/alcoholic/alcoholic/machines/industrial_press.json");
        resource("data/alcoholic/alcoholic/machines/industrial_fermentation_vat.json");
        resource("data/alcoholic/alcoholic/machines/industrial_storage_tank.json");
        resource("data/alcoholic/alcoholic/machines/industrial_malt_house.json");
        resource("data/alcoholic/alcoholic/machines/industrial_roller_mill.json");
        resource("data/alcoholic/alcoholic/machines/industrial_mash_tun.json");
        resource("data/alcoholic/alcoholic/machines/industrial_brewing_kettle.json");
        resource("data/alcoholic/alcoholic/machines/industrial_conditioning_vessel.json");
        assertEquals(
                0,
                resource("data/alcoholic/loot_tables/blocks/industrial_press_controller.json")
                        .getAsJsonArray("pools")
                        .size()
        );
    }

    @Test
    void bothVarietiesProvideConfiguredAndPlacedWorldgen() throws IOException {
        resource("data/alcoholic/tags/worldgen/biome/has_wild_grapevines.json");
        for (String color : new String[]{"red", "white"}) {
            String feature = "wild_" + color + "_grapevines.json";
            resource("data/alcoholic/worldgen/configured_feature/" + feature);
            resource("data/alcoholic/worldgen/placed_feature/" + feature);
        }
    }

    @Test
    void legacyWorldgenDefinesCompleteHarvestReadyState() throws IOException {
        JsonObject feature = resource(
                "data/alcoholic/worldgen/configured_feature/wild_red_grapevines.json"
        );
        JsonObject properties = feature.getAsJsonObject("config")
                .getAsJsonObject("feature")
                .getAsJsonObject("feature")
                .getAsJsonObject("config")
                .getAsJsonObject("to_place")
                .getAsJsonObject("state")
                .getAsJsonObject("Properties");

        assertEquals("4", properties.get("age").getAsString());
        assertEquals("harvest_ready", properties.get("stage").getAsString());
        assertEquals("false", properties.get("trained").getAsString());
        assertEquals("false", properties.get("extended").getAsString());
    }

    private static void assertOptionalTag(JsonObject tag, String expectedId) {
        JsonArray values = tag.getAsJsonArray("values");
        JsonObject externalTag = values.get(1).getAsJsonObject();
        assertEquals(expectedId, externalTag.get("id").getAsString());
        assertFalse(externalTag.get("required").getAsBoolean());
    }

    private static void assertClimate(
            JsonObject profile,
            double temperature,
            double humidity
    ) {
        JsonObject climate = profile.getAsJsonObject("growth")
                .getAsJsonObject("climate");
        assertEquals(temperature, climate.get("temperature_celsius").getAsDouble());
        assertEquals(humidity, climate.get("humidity").getAsDouble());
    }

    private static void assertTraining(
            JsonObject settings,
            String training,
            double expectedYield,
            double expectedQuality
    ) {
        JsonObject values = settings.getAsJsonObject(training);
        assertEquals(expectedYield, values.get("yield").getAsDouble());
        assertEquals(expectedQuality, values.get("quality").getAsDouble());
    }

    private static void assertPruning(JsonObject profile) {
        JsonObject pruning = profile.getAsJsonObject("harvest")
                .getAsJsonObject("pruning");
        assertMultipliers(pruning, "light", 1.20, 0.92);
        assertMultipliers(pruning, "balanced", 1.0, 1.0);
        assertMultipliers(pruning, "severe", 0.75, 1.12);
    }

    private static void assertMultipliers(
            JsonObject pruning,
            String level,
            double expectedYield,
            double expectedQuality
    ) {
        JsonObject values = pruning.getAsJsonObject(level);
        assertEquals(expectedYield, values.get("yield").getAsDouble());
        assertEquals(expectedQuality, values.get("quality").getAsDouble());
    }

    private static void assertPng16(String path) throws IOException {
        ClassLoader loader = GeneratedResourceContractTest.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing generated texture " + path);
            BufferedImage image = ImageIO.read(stream);
            assertNotNull(image, "Invalid PNG texture " + path);
            assertEquals(16, image.getWidth(), "Unexpected texture width " + path);
            assertEquals(16, image.getHeight(), "Unexpected texture height " + path);
        }
    }

    private static JsonObject resource(String path) throws IOException {
        ClassLoader loader = GeneratedResourceContractTest.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing generated resource " + path);
            return JsonParser.parseReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)
            ).getAsJsonObject();
        }
    }
}
