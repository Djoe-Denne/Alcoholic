package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.data.DataDecodeException;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.data.JsonDataParser;
import com.djden.alcoholic.api.ingredient.IngredientSelector;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.api.process.SolidAccepting;
import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.application.beverage.LoadBeverageCatalogUseCase;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.domain.process.ProcessDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessDisplayRecipesTest {
    @Test
    void grainCatalogExposesEveryProcessThatHasVisiblePorts() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        List<ProcessDisplayRecipe> recipes = ProcessDisplayRecipes.fromCatalog(loaded.catalog(), loaded.api());
        assertEquals(loaded.catalog().processes().size(), recipes.size());
        Map<ResourceId, List<ProcessDisplayRecipe>> grouped =
                ProcessDisplayRecipes.groupByType(loaded.catalog(), loaded.api());
        assertTrue(grouped.containsKey(BuiltinRegistrations.MILL));
        assertTrue(grouped.containsKey(BuiltinRegistrations.MASH));
        assertTrue(grouped.containsKey(BuiltinRegistrations.BOIL));
        assertTrue(grouped.containsKey(BuiltinRegistrations.MALT));
        assertTrue(grouped.containsKey(BuiltinRegistrations.FERMENT));
        assertTrue(grouped.containsKey(BuiltinRegistrations.CONDITION));
    }

    @Test
    void millMashAndBoilKeepTypedInputsAndOutputs() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();

        ProcessDisplayRecipe mill = ProcessDisplayRecipes.from(
                loaded.catalog().process(ResourceId.parse("alcoholic:mill_malted_grain")).orElseThrow(),
                loaded.api()
        ).orElseThrow();
        assertEquals(BuiltinRegistrations.MILL, mill.processType());
        assertEquals(1, mill.itemInputs().size());
        assertEquals(
                new IngredientSelector.Tag(ResourceId.parse("alcoholic:malted_grain")),
                mill.itemInputs().get(0).selector()
        );
        assertEquals(
                new IngredientSelector.Item(ResourceId.parse("alcoholic:grist")),
                mill.itemOutputs().get(0).selector()
        );
        assertEquals(80, mill.durationTicks().orElseThrow());

        ProcessDisplayRecipe mash = ProcessDisplayRecipes.from(
                loaded.catalog().process(ResourceId.parse("alcoholic:mash_wort")).orElseThrow(),
                loaded.api()
        ).orElseThrow();
        assertEquals(1, mash.itemInputs().size());
        assertEquals(ResourceId.parse("minecraft:water"), mash.fluidInputs().get(0).fluid());
        assertEquals(1000, mash.fluidInputs().get(0).millibuckets().orElseThrow());
        assertEquals(ResourceId.parse("alcoholic:wort"), mash.fluidOutputs().get(0).fluid());
        assertEquals(1000, mash.fluidOutputs().get(0).millibuckets().orElseThrow());
        assertEquals(
                new IngredientSelector.Item(ResourceId.parse("alcoholic:spent_grain")),
                mash.itemOutputs().get(0).selector()
        );
        assertEquals(62.0, mash.preferredTemperature().orElseThrow().min(), 1e-9);

        ProcessDisplayRecipe boil = ProcessDisplayRecipes.from(
                loaded.catalog().process(ResourceId.parse("alcoholic:boil_wort")).orElseThrow(),
                loaded.api()
        ).orElseThrow();
        assertFalse(boil.itemInputs().isEmpty());
        assertEquals(ResourceId.parse("alcoholic:wort"), boil.fluidInputs().get(0).fluid());
        assertTrue(boil.fluidInputs().get(0).millibuckets().isEmpty());
        assertEquals(ResourceId.parse("alcoholic:hopped_wort"), boil.fluidOutputs().get(0).fluid());
    }

    @Test
    void fermentDoesNotInventABucketVolume() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        ProcessDisplayRecipe ferment = ProcessDisplayRecipes.from(
                loaded.catalog().process(ResourceId.parse("alcoholic:ferment_hopped_wort")).orElseThrow(),
                loaded.api()
        ).orElseThrow();
        assertEquals(ResourceId.parse("alcoholic:hopped_wort"), ferment.fluidInputs().get(0).fluid());
        assertTrue(ferment.fluidInputs().get(0).millibuckets().isEmpty());
        assertEquals(ResourceId.parse("alcoholic:beer"), ferment.fluidOutputs().get(0).fluid());
        assertTrue(ferment.fluidOutputs().get(0).millibuckets().isEmpty());
    }

    @Test
    void bottleProjectsEmptyBottleAndConfiguredItem() {
        assertEquals(
                "250 mB",
                new BottleConfig(250, ResourceId.parse("alcoholic:beverage_bottle"))
                        .display()
                        .itemInputs()
                        .get(0)
                        .hint()
                        .orElseThrow()
        );
        assertEquals(
                new IngredientSelector.Item(ResourceId.parse("alcoholic:empty_bottle")),
                new BottleConfig(250, ResourceId.parse("alcoholic:beverage_bottle"))
                        .display()
                        .itemInputs()
                        .get(0)
                        .selector()
        );
    }

    @Test
    void addonSolidAcceptingProjectsWithoutACoreSwitch() {
        AlcoholicApi api = AlcoholicApi.create();
        AtomicReference<BeverageCatalog> catalog = new AtomicReference<>(BeverageCatalog.empty());
        BuiltinRegistrations.install(api, catalog::get);
        ResourceId addon = ResourceId.parse("pack:addon");
        api.processes().register(addon, AddonConfig.CODEC, (request, config, context) ->
                ProcessResult.rejected("unused")
        );
        BeverageCatalog loaded = new LoadBeverageCatalogUseCase().load(
                Map.of(),
                Map.of(
                        ResourceId.parse("pack:processes/addon_barley"),
                        JsonDataParser.parse("""
                                {
                                  "id": "pack:addon_barley",
                                  "process": "pack:addon",
                                  "config": {
                                    "input": { "tag": "alcoholic:barley", "amount": 1 }
                                  }
                                }
                                """)
                ),
                Map.of(),
                Map.of(),
                api
        );
        catalog.set(loaded);
        ProcessDisplayRecipe recipe = ProcessDisplayRecipes.from(
                loaded.process(ResourceId.parse("pack:addon_barley")).orElseThrow(),
                api
        ).orElseThrow();
        assertEquals(addon, recipe.processType());
        assertEquals(
                new IngredientSelector.Tag(ResourceId.parse("alcoholic:barley")),
                recipe.itemInputs().get(0).selector()
        );
    }

    @Test
    void decodeFailureHidesTheRecipeInsteadOfInventingGenericPorts() {
        AlcoholicApi api = AlcoholicApi.create();
        ResourceId type = ResourceId.parse("pack:strict");
        api.processes().register(type, StrictConfig.CODEC, (request, config, context) ->
                ProcessResult.rejected("unused")
        );
        ProcessDefinition definition = new ProcessDefinition(
                ResourceId.parse("pack:broken"),
                type,
                JsonDataParser.parse("""
                        { "ratio": 2.0 }
                        """),
                Map.of(),
                List.of()
        );
        assertTrue(ProcessDisplayRecipes.from(definition, api).isEmpty());
    }

    private record AddonConfig(Optional<IngredientSelector> inputSelector) implements SolidAccepting {
        private static final DataCodec<AddonConfig> CODEC = new DataCodec<>() {
            @Override
            public AddonConfig decode(DataNode node, String path) {
                DataNode.ObjectNode object = node.asObject(path);
                DataNode.ObjectNode input = object.require("input", path)
                        .asObject(DataDecodeException.child(path, "input"));
                return new AddonConfig(Optional.of(
                        PressConfig.selector(input, DataDecodeException.child(path, "input"))
                ));
            }

            @Override
            public DataNode encode(AddonConfig value) {
                return DataNode.objectBuilder().build();
            }
        };
    }

    private record StrictConfig(double ratio) {
        private static final DataCodec<StrictConfig> CODEC = new DataCodec<>() {
            @Override
            public StrictConfig decode(DataNode node, String path) {
                double ratio = node.asObject(path)
                        .require("ratio", path)
                        .asNumber(DataDecodeException.child(path, "ratio"))
                        .doubleValue();
                if (ratio < 0.0 || ratio > 1.0) {
                    throw new DataDecodeException(DataDecodeException.child(path, "ratio"), "out of range");
                }
                return new StrictConfig(ratio);
            }

            @Override
            public DataNode encode(StrictConfig value) {
                return DataNode.objectBuilder().put("ratio", DataNode.number(value.ratio())).build();
            }
        };
    }
}
