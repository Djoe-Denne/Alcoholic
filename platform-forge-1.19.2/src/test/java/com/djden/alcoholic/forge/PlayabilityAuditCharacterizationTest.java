package com.djden.alcoholic.forge;

import com.djden.alcoholic.application.progression.ProgressionCatalog;
import com.djden.alcoholic.application.progression.ProgressionChapter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Remaining playability defects (C16–C26) stay locked here.
 * C1, C3 and C15 assertions were inverted after remédiation.
 */
class PlayabilityAuditCharacterizationTest {
    @Test
    void agingAndConditioningControllersUseDistinctIngredients() throws IOException {
        JsonObject aging = innerRecipe(resource("data/alcoholic/recipes/industrial_aging_vessel_controller.json"));
        JsonObject conditioning = innerRecipe(resource("data/alcoholic/recipes/industrial_conditioning_vessel_controller.json"));
        assertEquals(aging.getAsJsonArray("pattern").toString(), conditioning.getAsJsonArray("pattern").toString());
        assertTrue(
                !aging.getAsJsonObject("key").toString().equals(conditioning.getAsJsonObject("key").toString()),
                "AGE and CONDITION controllers still share the same craft keys"
        );
        JsonObject agingCreate = innerRecipe(resource("data/alcoholic/recipes/industrial_aging_vessel_controller_create.json"));
        JsonObject conditioningCreate = innerRecipe(resource("data/alcoholic/recipes/industrial_conditioning_vessel_controller_create.json"));
        assertTrue(
                !agingCreate.getAsJsonObject("key").toString().equals(conditioningCreate.getAsJsonObject("key").toString()),
                "Create AGE and CONDITION controllers still share the same craft keys"
        );
    }

    @Test
    void bottleProcessJsonIsPresentForJei() {
        assertNotNull(
                stream("data/alcoholic/alcoholic/processes/bottle.json"),
                "bottle.json is missing; JEI Bottling would fall back to incomplete()"
        );
        assertNotNull(stream("data/alcoholic/alcoholic/processes/mash_wort.json"));
    }

    @Test
    void bottleRackAndShelfHaveNoCraftingRecipes() {
        assertNull(stream("data/alcoholic/recipes/bottle_rack.json"));
        assertNull(stream("data/alcoholic/recipes/bottle_shelf.json"));
        assertNotNull(stream("data/alcoholic/recipes/empty_bottle.json"));
    }

    @Test
    void formAgingFlipbookIsPresent() throws IOException {
        boolean catalogued = ProgressionCatalog.official()
                .chapter(ProgressionChapter.INDUSTRIAL)
                .images()
                .stream()
                .anyMatch(image -> "form_aging".equals(image.sprite()));
        assertTrue(catalogued, "form_aging image missing from industrial chapter");
        assertNotNull(
                stream("assets/alcoholic/textures/item/ftbquests/form_conditioning.png"),
                "form_conditioning texture should exist as the control"
        );
        assertNotNull(
                stream("assets/alcoholic/textures/item/ftbquests/form_aging.png"),
                "form_aging.png is still missing from the industrial chapter"
        );
    }

    @Test
    void industrialControllerLootTablesDropNothing() throws IOException {
        String[] controllers = {
                "industrial_aging_vessel_controller",
                "industrial_conditioning_vessel_controller",
                "industrial_press_controller",
                "industrial_vat_controller",
                "industrial_tank_controller",
                "industrial_malt_house_controller",
                "industrial_roller_mill_controller",
                "industrial_mash_tun_controller",
                "industrial_brewing_kettle_controller",
                "craft_malt_house_controller",
                "craft_mill_controller",
                "craft_mash_tun_controller",
                "craft_brewing_kettle_controller",
                "craft_vat_controller"
        };
        for (String id : controllers) {
            JsonObject loot = resource("data/alcoholic/loot_tables/blocks/" + id + ".json");
            assertTrue(loot.getAsJsonArray("pools").isEmpty(), id + " loot is no longer empty");
        }
    }

    @Test
    void beerGraphFermentPortDoesNotMatchProcessOutputName() throws IOException {
        JsonObject beer = resource("data/alcoholic/alcoholic/beverages/beer.json");
        JsonArray nodes = beer.getAsJsonObject("graph").getAsJsonArray("nodes");
        JsonObject ferment = null;
        for (int i = 0; i < nodes.size(); i++) {
            JsonObject node = nodes.get(i).getAsJsonObject();
            if ("ferment".equals(node.get("id").getAsString())) {
                ferment = node;
                break;
            }
        }
        assertNotNull(ferment);
        assertEquals("finished", ferment.getAsJsonArray("outputs").get(0).getAsString());
        JsonObject process = resource("data/alcoholic/alcoholic/processes/ferment_hopped_wort.json");
        assertEquals("young", process.getAsJsonArray("outputs").get(0).getAsString());
    }

    private static JsonObject innerRecipe(JsonObject wrapper) {
        assertEquals("forge:conditional", wrapper.get("type").getAsString());
        return wrapper.getAsJsonArray("recipes")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("recipe");
    }

    private static JsonObject resource(String path) throws IOException {
        try (InputStream stream = stream(path)) {
            assertNotNull(stream, "Missing generated resource " + path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        }
    }

    private static InputStream stream(String path) {
        return PlayabilityAuditCharacterizationTest.class.getClassLoader().getResourceAsStream(path);
    }
}
