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
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessRecipeResolverAddonTest {
    private static final ResourceId ADDON = ResourceId.parse("pack:addon");

    @Test
    void matchesAddonSolidAcceptingConfigsWithoutAClosedCodecList() {
        AlcoholicApi api = AlcoholicApi.create();
        AtomicReference<BeverageCatalog> catalog = new AtomicReference<>(BeverageCatalog.empty());
        BuiltinRegistrations.install(api, catalog::get);
        api.processes().register(ADDON, AddonConfig.CODEC, (request, config, context) -> ProcessResult.rejected("unused"));
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
        SelectorMatcher matcher = SelectorMatcher.tags(GrainProcessHarness::tagsOf, loaded);

        var invocation = ProcessRecipeResolver.find(
                loaded,
                api,
                ADDON,
                matcher,
                Optional.of(GrainProcessHarness.BARLEY),
                Optional.empty()
        ).orElseThrow();
        assertEquals("pack:addon_barley", invocation.nodeId());
        assertTrue(ProcessRecipeResolver.find(
                loaded,
                api,
                ADDON,
                matcher,
                Optional.of(GrainProcessHarness.HOPS),
                Optional.empty()
        ).isEmpty());
    }

    private record AddonConfig(Optional<IngredientSelector> inputSelector) implements SolidAccepting {
        private static final DataCodec<AddonConfig> CODEC = new DataCodec<>() {
            @Override
            public AddonConfig decode(DataNode node, String path) {
                DataNode.ObjectNode object = node.asObject(path);
                DataNode.ObjectNode input = object.require("input", path)
                        .asObject(DataDecodeException.child(path, "input"));
                return new AddonConfig(Optional.of(PressConfig.selector(input, DataDecodeException.child(path, "input"))));
            }

            @Override
            public DataNode encode(AddonConfig value) {
                DataNode.ObjectBuilder input = DataNode.objectBuilder();
                value.inputSelector().ifPresent(selector -> PressConfig.encodeSelector(input, selector));
                return DataNode.objectBuilder().put("input", input.build()).build();
            }
        };
    }
}
