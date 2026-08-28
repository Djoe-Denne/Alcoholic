package com.djden.alcoholic.forge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
    void hopsWorldgenIsConditionallyDisabledWhenBreweryIsPresent() throws IOException {
        JsonObject modifier = resource(
                "data/alcoholic/forge/biome_modifier/wild_hops.json"
        );
        JsonObject condition = modifier.getAsJsonArray("forge:conditions")
                .get(0)
                .getAsJsonObject();
        assertEquals("forge:not", condition.get("type").getAsString());
        JsonObject nested = condition.getAsJsonObject("value");
        assertEquals("alcoholic:item_present", nested.get("type").getAsString());
        assertEquals("brewery:hops", nested.get("item").getAsString());
        assertEquals("forge:add_features", modifier.get("type").getAsString());
        assertEquals("#alcoholic:has_wild_hops", modifier.get("biomes").getAsString());
    }

    @Test
    void wildHopsWorldgenAndAssetsArePresent() throws IOException {
        resource("data/alcoholic/tags/worldgen/biome/has_wild_hops.json");
        resource("data/alcoholic/worldgen/configured_feature/wild_hops.json");
        resource("data/alcoholic/worldgen/placed_feature/wild_hops.json");
        JsonObject loot = resource("data/alcoholic/loot_tables/blocks/wild_hops.json");
        assertEquals(2, loot.getAsJsonArray("pools").size());
        resource("assets/alcoholic/blockstates/wild_hops.json");
        JsonObject model = resource("assets/alcoholic/models/block/wild_hops.json");
        assertEquals(
                "alcoholic:block/wild_hops",
                model.getAsJsonObject("textures").get("cross").getAsString()
        );
        assertCrossFoliage(model);
        assertNoWoodTrunk(model);
        JsonObject itemModel = resource("assets/alcoholic/models/item/wild_hops.json");
        assertEquals("minecraft:item/generated", itemModel.get("parent").getAsString());
        assertEquals(
                "alcoholic:item/wild_hops",
                itemModel.getAsJsonObject("textures").get("layer0").getAsString()
        );
        assertPng16("assets/alcoholic/textures/block/wild_hops.png");
        assertPng16("assets/alcoholic/textures/item/wild_hops.png");
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
                    JsonObject trainedModel = resource(
                            "assets/alcoholic/models/block/" + color
                                    + "_grapevine_" + stage + "_trained.json"
                    );
                    assertCrossFoliage(trainedModel);
                    assertNoWoodTrunk(trainedModel);
                    resource(
                            "assets/alcoholic/models/block/" + color
                                    + "_grapevine_" + stage + "_base.json"
                    );
                    assertWoodTrunk(resource(
                            "assets/alcoholic/models/block/" + color
                                    + "_grapevine_" + stage + "_base.json"
                    ));
                    JsonObject trainedStem = resource(
                            "assets/alcoholic/models/block/" + color
                                    + "_grapevine_stem_" + stage + "_trained.json"
                    );
                    assertCrossFoliage(trainedStem);
                    assertWoodTrunk(trainedStem);
                    JsonObject untrainedStem = resource(
                            "assets/alcoholic/models/block/" + color
                                    + "_grapevine_stem_" + stage + "_untrained.json"
                    );
                    assertCrossFoliage(untrainedStem);
                    assertWoodTrunk(untrainedStem);
                    JsonObject canopy = resource(
                            "assets/alcoholic/models/block/" + color
                                    + "_grapevine_canopy_" + stage + ".json"
                    );
                    assertCrossFoliage(canopy);
                    assertNoWoodTrunk(canopy);
                    assertTrue(
                            canopy.getAsJsonObject("textures").has("wire"),
                            "Canopy must keep the trellis wire"
                    );
                    JsonObject canopyTrunk = resource(
                            "assets/alcoholic/models/block/" + color
                                    + "_grapevine_canopy_" + stage + "_trunk.json"
                    );
                    assertCrossFoliage(canopyTrunk);
                    assertWoodTrunk(canopyTrunk);
                    assertTrue(
                            canopyTrunk.getAsJsonObject("textures").has("wire"),
                            "Tall canopy must keep the trellis wire"
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
            assertEquals(32, canopyVariants.size());
            assertTrue(
                    variants.keySet().stream().noneMatch(
                            key -> key.startsWith("age=") || key.contains(",age=")
                    ),
                    "Legacy age must not duplicate every visual variant"
            );
        }
    }

    @Test
    void hopBinesProvideColumnSegmentAssets() throws IOException {
        JsonObject variants = resource("assets/alcoholic/blockstates/hop_bine.json")
                .getAsJsonObject("variants");
        assertEquals(12, variants.size());

        for (int age = 0; age <= 2; age++) {
            JsonObject single = resource("assets/alcoholic/models/block/hop_bine_" + age + ".json");
            JsonObject bottom = resource(
                    "assets/alcoholic/models/block/hop_bine_" + age + "_bottom.json"
            );
            JsonObject middle = resource(
                    "assets/alcoholic/models/block/hop_bine_" + age + "_middle.json"
            );
            JsonObject top = resource(
                    "assets/alcoholic/models/block/hop_bine_" + age + "_top.json"
            );
            assertEquals("minecraft:block/cross", single.get("parent").getAsString());
            assertCrossFoliage(bottom);
            assertCrossFoliage(middle);
            assertCrossFoliage(top);
            assertWoodTrunk(bottom);
            assertWoodTrunk(middle);
            assertWoodTrunk(top);
            assertNotEquals(bottom, top, "Bottom and top hop models must differ");
            assertNotEquals(bottom, middle, "Bottom and middle hop models must differ");
            for (String segment : new String[]{"single", "bottom", "middle", "top"}) {
                String modelName = "hop_bine_" + age
                        + ("single".equals(segment) ? "" : "_" + segment);
                JsonObject variant = variants.getAsJsonObject(
                        "age=" + age + ",segment=" + segment
                );
                assertEquals("alcoholic:block/" + modelName, variant.get("model").getAsString());
            }
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
                "oak_barrel",
                "artisanal_blending_crock",
                "electric_motor",
                "access_hatch",
                "industrial_casing",
                "machine_window",
                "fluid_port",
                "item_port",
                "kinetic_port",
                "industrial_press_controller",
                "industrial_roller_mill_controller",
                "industrial_vat_controller",
                "industrial_tank_controller",
                "industrial_malt_house_controller",
                "industrial_mash_tun_controller",
                "industrial_brewing_kettle_controller",
                "industrial_conditioning_vessel_controller"
        }) {
            resource("assets/alcoholic/blockstates/" + block + ".json");
            resource("assets/alcoholic/models/block/" + block + ".json");
            resource("assets/alcoholic/models/item/" + block + ".json");
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
                    "jei.alcoholic.category.mill",
                    "jei.alcoholic.category.mash",
                    "jei.alcoholic.category.boil",
                    "jei.alcoholic.category.malt",
                    "jei.alcoholic.category.press",
                    "jei.alcoholic.category.ferment",
                    "jei.alcoholic.category.age",
                    "jei.alcoholic.category.blend",
                    "jei.alcoholic.category.condition",
                    "jei.alcoholic.category.bottle",
                    "jei.alcoholic.category.addon",
                    "jei.alcoholic.duration",
                    "jei.alcoholic.volume.unspecified",
                    "tooltip.alcoholic.gauge.fluid",
                    "advancements.alcoholic.root.title",
                    "advancements.alcoholic.harvest_grapes.title",
                    "advancements.alcoholic.produce_must.description",
                    "advancements.alcoholic.ferment_beverage.title",
                    "advancements.alcoholic.bottle.description"
            }) {
                assertTrue(
                        translations.has(key),
                        "Missing " + language + " translation " + key
                );
            }
        }
    }

    @Test
    void alcoholicProgressionAdvancementsAreGenerated() throws IOException {
        JsonObject root = resource("data/alcoholic/advancements/root.json");
        assertEquals("minecraft:inventory_changed", criterionTrigger(root, "has_red_cutting"));
        assertTrue(root.has("display"));

        JsonObject harvest = resource("data/alcoholic/advancements/harvest_grapes.json");
        assertEquals("alcoholic:root", harvest.get("parent").getAsString());
        assertEquals("alcoholic:crop_harvested", criterionTrigger(harvest, "harvest_red"));

        JsonObject must = resource("data/alcoholic/advancements/produce_must.json");
        assertEquals("alcoholic:process_completed", criterionTrigger(must, "press_red_must"));
        assertEquals("alcoholic:process_completed", criterionTrigger(must, "mash_wort"));

        JsonObject ferment = resource("data/alcoholic/advancements/ferment_beverage.json");
        assertEquals("alcoholic:produce_must", ferment.get("parent").getAsString());
        assertEquals("alcoholic:process_completed", criterionTrigger(ferment, "ferment_red"));

        JsonObject bottle = resource("data/alcoholic/advancements/bottle.json");
        assertEquals("alcoholic:ferment_beverage", bottle.get("parent").getAsString());
        assertEquals("alcoholic:process_completed", criterionTrigger(bottle, "bottle"));

        resource("data/alcoholic/advancements/harvest_hops.json");
        resource("data/alcoholic/advancements/age_wine.json");
        resource("data/alcoholic/advancements/blend.json");
    }

    private static String criterionTrigger(JsonObject advancement, String criterion) {
        return advancement.getAsJsonObject("criteria")
                .getAsJsonObject(criterion)
                .get("trigger")
                .getAsString();
    }

    private static final String[] CRAFTABLE_MACHINES = {
            "vineyard_post",
            "end_post",
            "trellis_spool",
            "artisanal_press",
            "artisanal_fermenter",
            "oak_barrel",
            "artisanal_blending_crock",
            "malting_floor",
            "mash_tun",
            "brewing_kettle",
            "malt_mill",
            "primitive_combustion_engine",
            "electric_motor",
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
    };

    private static final String[] ALWAYS_VANILLA_RECIPES = {
            "yeast",
            "empty_bottle",
            "pruning_shears"
    };

    private static final String[] CREATE_PROCESS_RECIPES = {
            "mill_malted_grain_millstone",
            "mill_malted_grain_crushing",
            "press_red_grapes_create",
            "press_white_grapes_create"
    };

    private static final Set<String> VANILLA_INGREDIENT_NAMESPACES = Set.of(
            "minecraft",
            "alcoholic",
            "forge"
    );

    @Test
    void craftableMachinesAreVanillaXorCreate() throws IOException {
        for (String id : CRAFTABLE_MACHINES) {
            JsonObject vanilla = resource("data/alcoholic/recipes/" + id + ".json");
            assertDisabledWhenCreate(vanilla);
            JsonObject vanillaRecipe = innerRecipe(vanilla);
            assertEquals("minecraft:crafting_shaped", vanillaRecipe.get("type").getAsString());
            assertEquals("alcoholic:" + id, resultItem(vanillaRecipe));
            for (String ingredient : recipeIngredients(vanillaRecipe)) {
                assertTrue(
                        VANILLA_INGREDIENT_NAMESPACES.contains(namespaceOf(ingredient)),
                        id + " vanilla recipe uses " + ingredient
                );
            }

            JsonObject create = resource("data/alcoholic/recipes/" + id + "_create.json");
            assertEnabledWhenCreate(create);
            JsonObject createRecipe = innerRecipe(create);
            assertEquals("minecraft:crafting_shaped", createRecipe.get("type").getAsString());
            assertEquals("alcoholic:" + id, resultItem(createRecipe));
            Set<String> createIngredients = recipeIngredients(createRecipe);
            assertTrue(
                    createIngredients.stream().anyMatch(item -> "create".equals(namespaceOf(item))),
                    id + " create recipe has no create ingredient: " + createIngredients
            );
        }
    }

    @Test
    void consumableRecipesStayVanillaWithoutCreateGate() throws IOException {
        for (String id : ALWAYS_VANILLA_RECIPES) {
            JsonObject recipe = resource("data/alcoholic/recipes/" + id + ".json");
            assertNotEquals("forge:conditional", recipe.get("type").getAsString());
            assertFalse(recipe.has("conditions"));
            for (String ingredient : recipeIngredients(recipe)) {
                assertEquals("minecraft", namespaceOf(ingredient), id + " uses " + ingredient);
            }
        }
    }

    @Test
    void electricMotorIeRecipeYieldsToCreate() throws IOException {
        JsonObject wrapper = resource("data/alcoholic/recipes/electric_motor_ie.json");
        assertDisabledWhenCreate(wrapper);
        JsonArray conditions = recipeConditions(wrapper);
        assertEquals(2, conditions.size());
        JsonObject itemPresent = conditions.get(1).getAsJsonObject();
        assertEquals("alcoholic:item_present", itemPresent.get("type").getAsString());
        assertEquals("immersiveengineering:coil_lv", itemPresent.get("item").getAsString());
        JsonObject recipe = innerRecipe(wrapper);
        assertEquals("alcoholic:electric_motor", resultItem(recipe));
    }

    @Test
    void createProcessRecipesStayModLoaded() throws IOException {
        assertEquals(
                "create:milling",
                innerRecipe(resource("data/alcoholic/recipes/mill_malted_grain_millstone.json"))
                        .get("type")
                        .getAsString()
        );
        assertEquals(
                "create:crushing",
                innerRecipe(resource("data/alcoholic/recipes/mill_malted_grain_crushing.json"))
                        .get("type")
                        .getAsString()
        );
        assertEquals(
                "create:compacting",
                innerRecipe(resource("data/alcoholic/recipes/press_red_grapes_create.json"))
                        .get("type")
                        .getAsString()
        );
        assertEquals(
                "create:compacting",
                innerRecipe(resource("data/alcoholic/recipes/press_white_grapes_create.json"))
                        .get("type")
                        .getAsString()
        );
        for (String id : CREATE_PROCESS_RECIPES) {
            assertEnabledWhenCreate(resource("data/alcoholic/recipes/" + id + ".json"));
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

    private static void assertNoWoodTrunk(JsonObject model) {
        assertTrue(findCentralStem(model) == null, "Wood trunk must not appear on a one-block plant");
    }

    private static void assertWoodTrunk(JsonObject model) {
        JsonObject stem = findCentralStem(model);
        assertNotNull(stem, "Missing central wood trunk");
        JsonObject textures = model.getAsJsonObject("textures");
        assertNotNull(textures, "Missing model textures");
        assertEquals(
                "alcoholic:block/vineyard_post",
                textures.get("wood").getAsString(),
                "Wood trunk must use the vineyard post texture"
        );
        JsonObject faces = stem.getAsJsonObject("faces");
        assertNotNull(faces, "Missing wood trunk faces");
        for (String face : new String[]{"down", "up", "north", "south", "west", "east"}) {
            assertEquals(
                    "#wood",
                    faces.getAsJsonObject(face).get("texture").getAsString(),
                    "Wood trunk face " + face + " must use #wood"
            );
        }
    }

    private static void assertCrossFoliage(JsonObject model) {
        assertTrue(hasCrossFoliage(model), "Missing cross foliage planes");
    }

    private static boolean hasCrossFoliage(JsonObject model) {
        JsonArray elements = model.getAsJsonArray("elements");
        if (elements == null) {
            return false;
        }
        for (JsonElement element : elements) {
            JsonObject candidate = element.getAsJsonObject();
            if (!candidate.has("from") || !candidate.has("to")) {
                continue;
            }
            JsonArray from = candidate.getAsJsonArray("from");
            if (from.get(0).getAsDouble() == 0.8) {
                return true;
            }
        }
        return false;
    }

    private static JsonObject findCentralStem(JsonObject model) {
        JsonArray elements = model.getAsJsonArray("elements");
        if (elements == null) {
            return null;
        }
        for (JsonElement element : elements) {
            JsonObject candidate = element.getAsJsonObject();
            if (!candidate.has("from") || !candidate.has("to")) {
                continue;
            }
            JsonArray from = candidate.getAsJsonArray("from");
            JsonArray to = candidate.getAsJsonArray("to");
            if (from.get(0).getAsDouble() == 7.25
                    && from.get(1).getAsDouble() == 0.0
                    && from.get(2).getAsDouble() == 7.25
                    && to.get(0).getAsDouble() == 8.75
                    && to.get(2).getAsDouble() == 8.75) {
                return candidate;
            }
        }
        return null;
    }

    private static void assertDisabledWhenCreate(JsonObject wrapper) {
        JsonObject first = recipeConditions(wrapper).get(0).getAsJsonObject();
        assertEquals("forge:not", first.get("type").getAsString());
        JsonObject nested = first.getAsJsonObject("value");
        assertEquals("forge:mod_loaded", nested.get("type").getAsString());
        assertEquals("create", nested.get("modid").getAsString());
    }

    private static void assertEnabledWhenCreate(JsonObject wrapper) {
        JsonObject first = recipeConditions(wrapper).get(0).getAsJsonObject();
        assertEquals("forge:mod_loaded", first.get("type").getAsString());
        assertEquals("create", first.get("modid").getAsString());
    }

    private static JsonArray recipeConditions(JsonObject wrapper) {
        assertEquals("forge:conditional", wrapper.get("type").getAsString());
        return wrapper.getAsJsonArray("recipes")
                .get(0)
                .getAsJsonObject()
                .getAsJsonArray("conditions");
    }

    private static JsonObject innerRecipe(JsonObject wrapper) {
        assertEquals("forge:conditional", wrapper.get("type").getAsString());
        return wrapper.getAsJsonArray("recipes")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("recipe");
    }

    private static String resultItem(JsonObject recipe) {
        JsonElement result = recipe.get("result");
        if (result.isJsonPrimitive()) {
            return result.getAsString();
        }
        return result.getAsJsonObject().get("item").getAsString();
    }

    private static Set<String> recipeIngredients(JsonObject recipe) {
        Set<String> ids = new HashSet<>();
        collectRefs(recipe.get("key"), ids);
        collectRefs(recipe.get("ingredients"), ids);
        collectRefs(recipe.get("ingredient"), ids);
        return ids;
    }

    private static void collectRefs(JsonElement node, Set<String> ids) {
        if (node == null || node.isJsonNull()) {
            return;
        }
        if (node.isJsonObject()) {
            JsonObject object = node.getAsJsonObject();
            if (object.has("item") && object.get("item").isJsonPrimitive()) {
                ids.add(object.get("item").getAsString());
            }
            if (object.has("tag") && object.get("tag").isJsonPrimitive()) {
                ids.add("#" + object.get("tag").getAsString());
            }
            for (var entry : object.entrySet()) {
                if (!"item".equals(entry.getKey()) && !"tag".equals(entry.getKey())) {
                    collectRefs(entry.getValue(), ids);
                }
            }
            return;
        }
        if (node.isJsonArray()) {
            for (JsonElement element : node.getAsJsonArray()) {
                collectRefs(element, ids);
            }
        }
    }

    private static String namespaceOf(String id) {
        String value = id.startsWith("#") ? id.substring(1) : id;
        int colon = value.indexOf(':');
        return colon < 0 ? "minecraft" : value.substring(0, colon);
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
