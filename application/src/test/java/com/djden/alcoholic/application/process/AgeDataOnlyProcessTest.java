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
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.domain.vessel.EnvironmentProfile;
import com.djden.alcoholic.domain.vessel.VesselProfile;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgeDataOnlyProcessTest {
    private static final ResourceId AGE = ResourceId.parse("alcoholic:age");
    private static final ResourceId MATURITY = ResourceId.parse("alcoholic:maturity");

    @Test
    void ciderAgeNodeCompletesToAgedCider() {
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

        SelectorMatcher matcher = SelectorMatcher.tags(item -> Set.of(), loaded);
        ProcessInvocation age = ProcessRecipeResolver.find(
                loaded,
                api,
                AGE,
                matcher,
                Optional.empty(),
                Optional.of(ResourceId.parse("testpack:young_cider"))
        ).orElseThrow();

        LiquidBatch young = LiquidBatch.of(
                ResourceId.parse("testpack:young_cider"),
                1000,
                PropertyBag.empty().with(MATURITY, 0.0)
        );
        ProcessResult aged = new ExecuteProcessUseCase(api).execute(
                new CapabilityProcessExecutor(AGE),
                age,
                ProcessInputs.ofLiquid("source", young),
                ProcessContext.of(
                        14.0,
                        80.0,
                        false,
                        Optional.of(VesselProfile.oakBarrel()),
                        Optional.of(EnvironmentProfile.temperateCellar()),
                        0L
                )
        );
        assertTrue(aged.success(), aged.message());
        LiquidBatch result = (LiquidBatch) aged.outputs().get(0);
        assertEquals(ResourceId.parse("testpack:aged_cider"), result.baseLiquid().orElseThrow());
        assertTrue(result.number(MATURITY, 0.0) >= 1.0);
    }

    @Test
    void whiskyAgeDefinitionMatchesNewMakeSpirit() {
        AlcoholicApi api = AlcoholicApi.create();
        BuiltinRegistrations.install(api);
        BeverageCatalog loaded = new LoadBeverageCatalogUseCase().load(
                FixtureCatalogs.ingredients(),
                FixtureCatalogs.processes(),
                Map.of(
                        ResourceId.parse("testpack:beverages/whisky"),
                        FixtureCatalogs.read("data/testpack/alcoholic/beverages/whisky.json")
                ),
                FixtureCatalogs.liquids(),
                FixtureCatalogs.quality(),
                api
        );
        Optional<ProcessInvocation> found = ProcessRecipeResolver.find(
                loaded,
                api,
                AGE,
                SelectorMatcher.tags(item -> Set.of(), loaded),
                Optional.empty(),
                Optional.of(ResourceId.parse("testpack:new_make_spirit"))
        );
        assertTrue(found.isPresent());
    }
}
