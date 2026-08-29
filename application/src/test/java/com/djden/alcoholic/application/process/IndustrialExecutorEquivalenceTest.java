package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ExecutorModifiers;
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
import com.djden.alcoholic.domain.process.QualityProfile;
import com.djden.alcoholic.domain.process.ThermalStability;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndustrialExecutorEquivalenceTest {
    private static final ResourceId APPLE = ResourceId.parse("testpack:apple");
    private static final ResourceId SUGAR = ResourceId.parse("alcoholic:sugar");
    private static final ResourceId PRESS = ResourceId.parse("alcoholic:press");
    private static final ResourceId FERMENT = ResourceId.parse("alcoholic:ferment");

    @Test
    void artisanalAndIndustrialPressMatchWithoutModifiers() {
        Harness harness = Harness.load();
        ProcessInvocation node = ProcessRecipeResolver.find(
                harness.catalog,
                harness.api,
                PRESS,
                harness.matcher,
                Optional.of(APPLE),
                Optional.empty()
        ).orElseThrow();
        ProcessInputs inputs = ProcessInputs.ofSolids("source", List.of(lot(8, 0.80, 0.30)));
        ProcessResult artisanal = harness.engine.execute(
                new CapabilityProcessExecutor(PRESS),
                node,
                inputs,
                ProcessContext.empty()
        );
        ProcessResult industrial = harness.engine.execute(
                new CapabilityProcessExecutor(PRESS),
                node,
                inputs,
                ProcessContext.of(20.0, 1.0, false, Optional.empty(), Optional.empty(), 0L, ExecutorModifiers.identity())
        );
        assertTrue(artisanal.success());
        assertTrue(industrial.success());
        LiquidBatch left = (LiquidBatch) artisanal.outputs().get(0);
        LiquidBatch right = (LiquidBatch) industrial.outputs().get(0);
        assertEquals(left.volume(), right.volume(), 1e-9);
        assertEquals(left.baseLiquid(), right.baseLiquid());
        assertEquals(left.number(SUGAR, 0.0), right.number(SUGAR, 0.0), 1e-9);
    }

    @Test
    void industrialYieldModifierScalesOutputOnly() {
        Harness harness = Harness.load();
        ProcessInvocation node = ProcessRecipeResolver.find(
                harness.catalog,
                harness.api,
                PRESS,
                harness.matcher,
                Optional.of(APPLE),
                Optional.empty()
        ).orElseThrow();
        ProcessResult baseline = harness.engine.execute(
                new CapabilityProcessExecutor(PRESS),
                node,
                ProcessInputs.ofSolids("source", List.of(lot(8, 0.80, 0.30))),
                ProcessContext.empty()
        );
        ProcessResult boosted = harness.engine.execute(
                new CapabilityProcessExecutor(PRESS),
                node,
                ProcessInputs.ofSolids("source", List.of(lot(8, 0.80, 0.30))),
                ProcessContext.of(
                        20.0,
                        1.0,
                        false,
                        Optional.empty(),
                        Optional.empty(),
                        0L,
                        new ExecutorModifiers(1.05, 1.0, 1.0, 1)
                )
        );
        double base = ((LiquidBatch) baseline.outputs().get(0)).volume();
        double industrial = ((LiquidBatch) boosted.outputs().get(0)).volume();
        assertEquals(base * 1.05, industrial, 1e-9);
    }

    @Test
    void largeHomogeneousPressIsOneApply() {
        Harness harness = Harness.load();
        ProcessInvocation node = ProcessRecipeResolver.find(
                harness.catalog,
                harness.api,
                PRESS,
                harness.matcher,
                Optional.of(APPLE),
                Optional.empty()
        ).orElseThrow();
        ProcessResult result = harness.engine.execute(
                new CapabilityProcessExecutor(PRESS),
                node,
                ProcessInputs.ofSolids("source", List.of(lot(500, 0.80, 0.30))),
                ProcessContext.of(
                        20.0,
                        1.0,
                        false,
                        Optional.empty(),
                        Optional.empty(),
                        0L,
                        ExecutorModifiers.industrialPress()
                )
        );
        assertTrue(result.success());
        LiquidBatch batch = (LiquidBatch) result.outputs().get(0);
        int units = 500 / 8;
        assertEquals(1000.0 * 1.05 * units, batch.volume(), 1e-6);
        assertEquals(0.55, batch.number(ResourceId.parse("alcoholic:complexity_cap"), 1.0), 1e-9);
        assertEquals(0.15, batch.number(ResourceId.parse("alcoholic:purity_floor"), 0.0), 1e-9);
        assertTrue(result.items().isEmpty());
    }

    @Test
    void fermentEquivalenceIsIndependentOfVolume() {
        Harness harness = Harness.load();
        ProcessInvocation node = ProcessRecipeResolver.find(
                harness.catalog,
                harness.api,
                FERMENT,
                harness.matcher,
                Optional.empty(),
                Optional.of(ResourceId.parse("testpack:apple_must"))
        ).orElseThrow();
        LiquidBatch small = must(2_000);
        LiquidBatch large = must(2_000_000);
        ProcessResult a = harness.engine.execute(
                new CapabilityProcessExecutor(FERMENT),
                node,
                ProcessInputs.ofLiquid("must", small),
                ProcessContext.of(20.0, 10.0, true)
        );
        ProcessResult b = harness.engine.execute(
                new CapabilityProcessExecutor(FERMENT),
                node,
                ProcessInputs.ofLiquid("must", large),
                ProcessContext.of(20.0, 10.0, true)
        );
        LiquidBatch nextSmall = (LiquidBatch) a.outputs().get(0);
        LiquidBatch nextLarge = (LiquidBatch) b.outputs().get(0);
        assertEquals(nextSmall.number(SUGAR, 1.0), nextLarge.number(SUGAR, 1.0), 1e-9);
        assertEquals(2_000, nextSmall.volume(), 1e-9);
        assertEquals(2_000_000, nextLarge.volume(), 1e-9);
    }

    @Test
    void fermentSpeedModifierAdvancesTheSameBatchFaster() {
        Harness harness = Harness.load();
        ProcessInvocation node = ProcessRecipeResolver.find(
                harness.catalog,
                harness.api,
                FERMENT,
                harness.matcher,
                Optional.empty(),
                Optional.of(ResourceId.parse("testpack:apple_must"))
        ).orElseThrow();
        ProcessResult baseline = harness.engine.execute(
                new CapabilityProcessExecutor(FERMENT),
                node,
                ProcessInputs.ofLiquid("must", must(2_000)),
                ProcessContext.of(20.0, 10.0, true)
        );
        ProcessResult industrial = harness.engine.execute(
                new CapabilityProcessExecutor(FERMENT),
                node,
                ProcessInputs.ofLiquid("must", must(2_000)),
                ProcessContext.of(
                        20.0,
                        10.0,
                        true,
                        new ExecutorModifiers(1.0, 4.0, 4.0, 1, 0.70, 0.55, 0.12)
                )
        );
        double slowSugar = ((LiquidBatch) baseline.outputs().get(0)).number(SUGAR, 1.0);
        double fastSugar = ((LiquidBatch) industrial.outputs().get(0)).number(SUGAR, 1.0);
        assertTrue(fastSugar < slowSugar);
        assertEquals(
                0.55,
                ((LiquidBatch) industrial.outputs().get(0)).number(ResourceId.parse("alcoholic:complexity_cap"), 1.0),
                1e-9
        );
    }

    @Test
    void thermalStabilityDampsAmbientSwing() {
        assertEquals(18.0, ThermalStability.effectiveCelsius(18.0, 18.0, 4.0), 1e-9);
        assertEquals(19.5, ThermalStability.effectiveCelsius(24.0, 18.0, 4.0), 1e-9);
        assertEquals(24.0, ThermalStability.effectiveCelsius(24.0, 18.0, 1.0), 1e-9);
    }

    @Test
    void batchMetadataSurvivesTankMergeAtIndustrialScale() {
        LiquidBatch first = LiquidBatch.of(
                ResourceId.parse("testpack:apple_must"),
                20_000,
                PropertyBag.empty().with(SUGAR, 0.80)
        );
        LiquidBatch second = LiquidBatch.of(
                ResourceId.parse("testpack:apple_must"),
                20_000,
                PropertyBag.empty().with(SUGAR, 0.40)
        );
        LiquidBatch merged = first.merge(second, id -> com.djden.alcoholic.api.property.PropertyMerge.WEIGHTED_AVERAGE)
                .orElseThrow();
        assertEquals(40_000, merged.volume(), 1e-9);
        assertEquals(0.60, merged.number(SUGAR, 0.0), 1e-9);
    }

    @Test
    void industrialComplexityCapDoesNotRiseOnBlend() {
        Harness harness = Harness.load();
        ResourceId must = ResourceId.parse("testpack:apple_must");
        LiquidBatch industrial = QualityProfile.stampCap(
                LiquidBatch.of(must, 1000, PropertyBag.empty().with(SUGAR, 0.80)),
                ExecutorModifiers.industrialPress()
        );
        LiquidBatch artisanal = QualityProfile.stampCap(
                LiquidBatch.of(must, 1000, PropertyBag.empty().with(SUGAR, 0.40)),
                ExecutorModifiers.artisanal()
        );
        LiquidBatch merged = industrial.merge(
                artisanal,
                PropertyMerges.from(harness.api),
                PropertyMerges.aggregators(harness.api)
        ).orElseThrow();
        assertEquals(0.55, merged.number(QualityProfile.COMPLEXITY_CAP, 1.0), 1e-9);
        assertEquals(0.15, merged.number(QualityProfile.PURITY_FLOOR, 0.0), 1e-9);
        QualityProfile profile = QualityProfile.derive(merged);
        assertTrue(profile.complexity() <= 0.55 + 1e-9);
        assertTrue(profile.defects() >= 0.15 - 1e-9);
    }

    private static LiquidBatch must(double volume) {
        return LiquidBatch.of(
                ResourceId.parse("testpack:apple_must"),
                volume,
                PropertyBag.empty().with(SUGAR, 0.80).with(ResourceId.parse("alcoholic:ethanol"), 0.0)
        );
    }

    private static IngredientLot lot(int count, double sugar, double acidity) {
        return new IngredientLot(
                APPLE,
                count,
                PropertyBag.empty().with(SUGAR, sugar).with(ResourceId.parse("alcoholic:acidity"), acidity)
        );
    }

    private record Harness(
            AlcoholicApi api,
            BeverageCatalog catalog,
            ExecuteProcessUseCase engine,
            SelectorMatcher matcher
    ) {
        static Harness load() {
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
            return new Harness(
                    api,
                    loaded,
                    new ExecuteProcessUseCase(api),
                    SelectorMatcher.tags(item -> Set.of(), loaded)
            );
        }
    }
}
