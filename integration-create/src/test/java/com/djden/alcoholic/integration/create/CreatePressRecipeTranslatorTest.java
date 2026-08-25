package com.djden.alcoholic.integration.create;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.JsonDataParser;
import com.djden.alcoholic.application.beverage.LoadBeverageCatalogUseCase;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CreatePressRecipeTranslatorTest {
    @Test
    void translatesCreateCompatiblePressDefinitionsWithoutProcessLogic() {
        AlcoholicApi api = AlcoholicApi.create();
        BuiltinRegistrations.install(api);
        var catalog = new LoadBeverageCatalogUseCase().load(
                Map.of(
                        ResourceId.parse("pack:ingredients/apple"),
                        JsonDataParser.parse("{\"id\":\"pack:apple\",\"tags\":[\"alcoholic:fruits/apple\"]}")
                ),
                Map.of(
                        ResourceId.parse("pack:processes/press_fruit"),
                        JsonDataParser.parse("""
                                {
                                  "id": "pack:press_fruit",
                                  "process": "alcoholic:press",
                                  "config": {
                                    "input": { "ingredient": "pack:apple", "amount": 4 },
                                    "output": { "liquid": "pack:apple_must", "volume": 500 },
                                    "byproduct": { "item": "minecraft:stick", "amount": 1 },
                                    "create_compatible": true
                                  },
                                  "inputs": { "source": { "ingredient": "pack:apple" } },
                                  "outputs": ["must"]
                                }
                                """)
                ),
                Map.of(),
                Map.of(
                        ResourceId.parse("pack:liquids/apple_must"),
                        JsonDataParser.parse("{\"id\":\"pack:apple_must\",\"defaults\":{\"alcoholic:sugar\":0.5}}")
                ),
                api
        );

        var spec = CreatePressRecipeTranslator.from(
                catalog.process(ResourceId.parse("pack:press_fruit")).orElseThrow()
        ).orElseThrow();
        String json = CreatePressRecipeTranslator.toCompactingJson(spec);

        assertTrue(json.contains("\"type\": \"create:compacting\""));
        assertTrue(json.contains("pack:apple_must"));
        assertTrue(json.contains("minecraft:stick"));
        assertTrue(json.contains("\"amount\": 500"));
    }
}
