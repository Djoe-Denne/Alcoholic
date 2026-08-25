package com.djden.alcoholic.integration.create;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.JsonDataParser;
import com.djden.alcoholic.application.beverage.LoadBeverageCatalogUseCase;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateMillRecipeTranslatorTest {
    @Test
    void translatesCreateCompatibleMillDefinitionsWithoutProcessLogic() {
        AlcoholicApi api = AlcoholicApi.create();
        BuiltinRegistrations.install(api);
        var catalog = new LoadBeverageCatalogUseCase().load(
                Map.of(),
                Map.of(
                        ResourceId.parse("pack:processes/mill_malt"),
                        JsonDataParser.parse("""
                                {
                                  "id": "pack:mill_malt",
                                  "process": "alcoholic:mill",
                                  "config": {
                                    "input": { "tag": "alcoholic:malted_grain", "amount": 1 },
                                    "output": { "item": "alcoholic:grist", "amount": 1 },
                                    "processing_time": 80,
                                    "create_compatible": true
                                  },
                                  "inputs": { "malt": { "tag": "alcoholic:malted_grain" } },
                                  "outputs": ["grist"]
                                }
                                """)
                ),
                Map.of(),
                Map.of(),
                api
        );

        var spec = CreateMillRecipeTranslator.from(
                catalog.process(ResourceId.parse("pack:mill_malt")).orElseThrow()
        ).orElseThrow();
        String milling = CreateMillRecipeTranslator.toMillingJson(spec);
        String crushing = CreateMillRecipeTranslator.toCrushingJson(spec);

        assertTrue(milling.contains("\"type\": \"create:milling\""));
        assertTrue(milling.contains("alcoholic:grist"));
        assertTrue(milling.contains("alcoholic:malted_grain"));
        assertTrue(crushing.contains("\"type\": \"create:crushing\""));
        assertTrue(crushing.contains("alcoholic:grist"));
        assertTrue(crushing.contains("\"processingTime\": 40"));
    }
}
