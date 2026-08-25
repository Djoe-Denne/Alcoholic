package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessInvocation;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.application.beverage.FixtureCatalogs;
import com.djden.alcoholic.application.beverage.LoadBeverageCatalogUseCase;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.domain.ingredient.IngredientLot;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CiderDataOnlyProcessTest {
    private static final ResourceId APPLE = ResourceId.parse("testpack:apple");
    private static final ResourceId SUGAR = ResourceId.parse("alcoholic:sugar");
    private static final ResourceId ACIDITY = ResourceId.parse("alcoholic:acidity");
    private static final ResourceId ETHANOL = ResourceId.parse("alcoholic:ethanol");
    private static final ResourceId PRESS = ResourceId.parse("alcoholic:press");
    private static final ResourceId FERMENT = ResourceId.parse("alcoholic:ferment");

    @Test
    void ciderDatapackUsesGenericPressAndFerment() {
        AlcoholicApi api = AlcoholicApi.create();
        AtomicReference<BeverageCatalog> catalog = new AtomicReference<>(BeverageCatalog.empty());
        BuiltinRegistrations.install(api, catalog::get);
        BeverageCatalog loaded = new LoadBeverageCatalogUseCase().load(
                FixtureCatalogs.ingredients(),
                FixtureCatalogs.processes(),
                Map.of(
                        ResourceId.parse("testpack:beverages/cider"),
                        FixtureCatalogs.read("data/testpack/alcoholic/beverages/cider.json")
                ),
                FixtureCatalogs.liquids(),
                api
        );
        catalog.set(loaded);

        ExecuteProcessUseCase engine = new ExecuteProcessUseCase(api);
        CapabilityProcessExecutor press = new CapabilityProcessExecutor(PRESS);
        CapabilityProcessExecutor fermenter = new CapabilityProcessExecutor(FERMENT);
        SelectorMatcher matcher = SelectorMatcher.tags(item -> Set.of(), loaded);

        ProcessInvocation pressNode = ProcessRecipeResolver.find(
                loaded,
                api,
                PRESS,
                matcher,
                Optional.of(APPLE),
                Optional.empty()
        ).orElseThrow();

        ProcessResult mustA = engine.execute(
                press,
                pressNode,
                ProcessInputs.ofSolids("source", List.of(lot(0.80, 0.30))),
                ProcessContext.empty()
        );
        ProcessResult mustB = engine.execute(
                press,
                pressNode,
                ProcessInputs.ofSolids("source", List.of(lot(0.40, 0.70))),
                ProcessContext.empty()
        );

        assertTrue(mustA.success());
        assertTrue(mustB.success());
        LiquidBatch batchA = (LiquidBatch) mustA.outputs().get(0);
        LiquidBatch batchB = (LiquidBatch) mustB.outputs().get(0);
        assertEquals(ResourceId.parse("testpack:apple_must"), batchA.baseLiquid().orElseThrow());
        assertNotEquals(batchA.number(SUGAR, 0.0), batchB.number(SUGAR, 0.0));
        assertNotEquals(batchA.number(ACIDITY, 0.0), batchB.number(ACIDITY, 0.0));

        ProcessInvocation fermentNode = ProcessRecipeResolver.find(
                loaded,
                api,
                FERMENT,
                matcher,
                Optional.empty(),
                Optional.of(ResourceId.parse("testpack:apple_must"))
        ).orElseThrow();

        ProcessResult fermented = engine.execute(
                fermenter,
                fermentNode,
                ProcessInputs.ofLiquid("must", batchA),
                ProcessContext.of(18.0, 1.0, true)
        );
        for (int tick = 0; tick < 120 && fermented.success() && !fermented.outputs().isEmpty(); tick++) {
            LiquidBatch current = (LiquidBatch) fermented.outputs().get(0);
            if (current.number(SUGAR, 1.0) <= 0.02
                    && current.baseLiquid().filter(ResourceId.parse("testpack:young_cider")::equals).isPresent()) {
                break;
            }
            fermented = engine.execute(
                    fermenter,
                    fermentNode,
                    ProcessInputs.ofLiquid("must", current),
                    ProcessContext.of(18.0, 1.0, true)
            );
        }
        assertTrue(fermented.success(), fermented.message());
        assertFalse(fermented.outputs().isEmpty(), fermented.message());
        LiquidBatch cider = (LiquidBatch) fermented.outputs().get(0);
        assertTrue(cider.number(SUGAR, 1.0) <= 0.02);
        assertTrue(cider.number(ETHANOL, 0.0) > 0.0);
        assertEquals(ResourceId.parse("testpack:young_cider"), cider.baseLiquid().orElseThrow());
    }

    private static IngredientLot lot(double sugar, double acidity) {
        return new IngredientLot(
                APPLE,
                8,
                PropertyBag.empty().with(SUGAR, sugar).with(ACIDITY, acidity)
        );
    }
}
