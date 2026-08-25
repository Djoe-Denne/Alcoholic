package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.data.JsonDataParser;
import com.djden.alcoholic.api.process.ProcessInvocation;
import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.application.beverage.LoadBeverageCatalogUseCase;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.application.ingredient.SemanticTags;
import com.djden.alcoholic.domain.ingredient.IngredientLot;
import com.djden.alcoholic.domain.liquid.PropertyBag;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

final class GrainProcessHarness {
    static final ResourceId BARLEY = ResourceId.parse("alcoholic:barley");
    static final ResourceId EXTERNAL_BARLEY = ResourceId.parse("brewery:barley");
    static final ResourceId MALTED = ResourceId.parse("alcoholic:malted_barley");
    static final ResourceId MALT_PALE = ResourceId.parse("alcoholic:malt_pale");
    static final ResourceId MALT_DARK = ResourceId.parse("alcoholic:malt_dark");
    static final ResourceId GRIST = ResourceId.parse("alcoholic:grist");
    static final ResourceId HOPS = ResourceId.parse("alcoholic:hops");
    static final ResourceId WATER = ResourceId.parse("minecraft:water");
    static final ResourceId WORT = ResourceId.parse("alcoholic:wort");
    static final ResourceId HOPPED = ResourceId.parse("alcoholic:hopped_wort");
    static final ResourceId BEVERAGE = ResourceId.parse("alcoholic:beer");
    static final ResourceId SUGAR = ResourceId.parse("alcoholic:sugar");
    static final ResourceId COLOR = ResourceId.parse("alcoholic:color");
    static final ResourceId BITTERNESS = ResourceId.parse("alcoholic:bitterness");
    static final ResourceId ETHANOL = ResourceId.parse("alcoholic:ethanol");

    private GrainProcessHarness() {
    }

    static Loaded load() {
        AlcoholicApi api = AlcoholicApi.create();
        AtomicReference<BeverageCatalog> catalog = new AtomicReference<>(BeverageCatalog.empty());
        BuiltinRegistrations.install(api, catalog::get);
        Map<ResourceId, DataNode> processes = new LinkedHashMap<>();
        processes.put(
                ResourceId.parse("alcoholic:processes/malt_dark"),
                JsonDataParser.parse("""
                        {
                          "id": "alcoholic:malt_dark",
                          "process": "alcoholic:malt",
                          "config": {
                            "input": { "tag": "alcoholic:barley", "amount": 1 },
                            "output": { "item": "alcoholic:malted_barley", "amount": 1 },
                            "processing_time": 20,
                            "moisture_requirement": 0.4,
                            "kiln_profile": {
                              "id": "alcoholic:dark",
                              "color_potential": 0.75,
                              "fermentable_potential": 0.55,
                              "roast_intensity": 0.80
                            }
                          },
                          "inputs": { "grain": { "tag": "alcoholic:barley" } },
                          "outputs": ["malt"]
                        }
                        """)
        );
        processes.put(
                ResourceId.parse("alcoholic:processes/malt_pale"),
                JsonDataParser.parse("""
                        {
                          "id": "alcoholic:malt_pale",
                          "process": "alcoholic:malt",
                          "config": {
                            "input": { "tag": "alcoholic:barley", "amount": 1 },
                            "output": { "item": "alcoholic:malted_barley", "amount": 1 },
                            "processing_time": 20,
                            "moisture_requirement": 0.4,
                            "kiln_profile": {
                              "id": "alcoholic:pale",
                              "color_potential": 0.12,
                              "fermentable_potential": 0.85,
                              "roast_intensity": 0.15
                            }
                          },
                          "inputs": { "grain": { "tag": "alcoholic:barley" } },
                          "outputs": ["malt"]
                        }
                        """)
        );
        processes.put(
                ResourceId.parse("alcoholic:processes/mill_malted_grain"),
                JsonDataParser.parse("""
                        {
                          "id": "alcoholic:mill_malted_grain",
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
        );
        processes.put(
                ResourceId.parse("alcoholic:processes/mash_wort"),
                JsonDataParser.parse("""
                        {
                          "id": "alcoholic:mash_wort",
                          "process": "alcoholic:mash",
                          "config": {
                            "solid": { "tag": "alcoholic:grist", "amount": 1 },
                            "liquid": { "fluid": "minecraft:water", "volume": 1000 },
                            "output": { "liquid": "alcoholic:wort", "volume": 1000 },
                            "byproduct": { "item": "alcoholic:spent_grain", "amount": 1 },
                            "processing_time": 40,
                            "preferred_temperature": { "min": 62, "max": 68 },
                            "operating_temperature": { "min": 52, "max": 78 }
                          },
                          "inputs": {
                            "grist": { "tag": "alcoholic:grist" },
                            "water": { "item": "minecraft:water_bucket" }
                          },
                          "outputs": ["wort"]
                        }
                        """)
        );
        processes.put(
                ResourceId.parse("alcoholic:processes/boil_wort"),
                JsonDataParser.parse("""
                        {
                          "id": "alcoholic:boil_wort",
                          "process": "alcoholic:boil",
                          "config": {
                            "input_liquid": "alcoholic:wort",
                            "output": { "liquid": "alcoholic:hopped_wort" },
                            "addition": { "tag": "alcoholic:hops", "amount": 1 },
                            "processing_time": 40,
                            "preferred_temperature": { "min": 98, "max": 105 },
                            "operating_temperature": { "min": 90, "max": 110 },
                            "hop_profile": {
                              "bitterness_potential": 0.55,
                              "aroma_potential": 0.40
                            }
                          },
                          "inputs": { "hops": { "tag": "alcoholic:hops" } },
                          "outputs": ["hopped_wort"]
                        }
                        """)
        );
        processes.put(
                ResourceId.parse("alcoholic:processes/ferment_hopped_wort"),
                JsonDataParser.parse("""
                        {
                          "id": "alcoholic:ferment_hopped_wort",
                          "process": "alcoholic:ferment",
                          "config": {
                            "input_liquid": "alcoholic:hopped_wort",
                            "output": { "liquid": "alcoholic:beer" },
                            "yeast": { "tag": "alcoholic:yeast" },
                            "require_yeast": true,
                            "ticks_to_complete": 40,
                            "kinetics": {
                              "sugar_to_ethanol": 0.47,
                              "completion_threshold": 0.02,
                              "co2_per_sugar": 0.45
                            }
                          },
                          "inputs": { "yeast": { "tag": "alcoholic:yeast" } },
                          "outputs": ["finished"]
                        }
                        """)
        );
        BeverageCatalog loaded = new LoadBeverageCatalogUseCase().load(
                Map.of(),
                processes,
                Map.of(),
                Map.of(
                        ResourceId.parse("alcoholic:liquids/wort"),
                        JsonDataParser.parse("""
                                {"id":"alcoholic:wort","defaults":{"alcoholic:sugar":0.0,"alcoholic:color":0.12,"alcoholic:ethanol":0.0}}
                                """),
                        ResourceId.parse("alcoholic:liquids/hopped_wort"),
                        JsonDataParser.parse("""
                                {"id":"alcoholic:hopped_wort","defaults":{"alcoholic:sugar":0.0,"alcoholic:bitterness":0.0}}
                                """),
                        ResourceId.parse("alcoholic:liquids/beer"),
                        JsonDataParser.parse("""
                                {"id":"alcoholic:beer","defaults":{"alcoholic:ethanol":0.0}}
                                """)
                ),
                api
        );
        catalog.set(loaded);
        SelectorMatcher matcher = SelectorMatcher.tags(GrainProcessHarness::tagsOf, loaded);
        return new Loaded(api, loaded, new ExecuteProcessUseCase(api), matcher);
    }

    static Set<ResourceId> tagsOf(ResourceId item) {
        if (BARLEY.equals(item) || EXTERNAL_BARLEY.equals(item)) {
            return Set.of(SemanticTags.BARLEY);
        }
        if (MALTED.equals(item)) {
            return Set.of(SemanticTags.MALTED_BARLEY, SemanticTags.MALTED_GRAIN);
        }
        if (GRIST.equals(item)) {
            return Set.of(SemanticTags.GRIST);
        }
        if (HOPS.equals(item) || ResourceId.parse("brewery:hops").equals(item)) {
            return Set.of(SemanticTags.HOPS);
        }
        if (ResourceId.parse("alcoholic:yeast").equals(item)) {
            return Set.of(SemanticTags.YEAST);
        }
        return Set.of();
    }

    static IngredientLot lot(ResourceId item, int count, PropertyBag properties) {
        return new IngredientLot(item, count, properties);
    }

    record Loaded(
            AlcoholicApi api,
            BeverageCatalog catalog,
            ExecuteProcessUseCase engine,
            SelectorMatcher matcher
    ) {
        ProcessInvocation find(ResourceId process, Optional<ResourceId> item, Optional<ResourceId> liquid) {
            return find(process, item, liquid, Optional.empty());
        }

        ProcessInvocation find(
                ResourceId process,
                Optional<ResourceId> item,
                Optional<ResourceId> liquid,
                Optional<ResourceId> definitionId
        ) {
            return ProcessRecipeResolver.find(catalog, api, process, matcher, item, liquid, definitionId)
                    .orElseThrow();
        }
    }
}
