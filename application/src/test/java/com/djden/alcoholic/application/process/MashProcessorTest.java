package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MashProcessorTest {
    @Test
    void mashesGristAndWaterIntoWortWithSpentGrain() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        ProcessResult result = mash(loaded, 65.0, 0.85, 0.12);
        assertTrue(result.success(), result.message());
        LiquidBatch wort = (LiquidBatch) result.outputs().get(0);
        assertEquals(GrainProcessHarness.WORT, wort.baseLiquid().orElseThrow());
        assertEquals(1000.0, wort.volume(), 1e-9);
        assertEquals(0.85, wort.number(GrainProcessHarness.SUGAR, 0.0), 1e-6);
        assertEquals(0.12, wort.number(GrainProcessHarness.COLOR, 0.0), 1e-6);
        assertEquals(ResourceId.parse("alcoholic:spent_grain"), result.items().get(0).item());
    }

    @Test
    void coldMashExtractsLessSugar() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        LiquidBatch preferred = (LiquidBatch) mash(loaded, 65.0, 0.85, 0.12).outputs().get(0);
        LiquidBatch cold = (LiquidBatch) mash(loaded, 55.0, 0.85, 0.12).outputs().get(0);
        assertTrue(cold.number(GrainProcessHarness.SUGAR, 0.0) < preferred.number(GrainProcessHarness.SUGAR, 0.0));
    }

    @Test
    void hotMashDegradesFermentability() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        LiquidBatch preferred = (LiquidBatch) mash(loaded, 65.0, 0.85, 0.12).outputs().get(0);
        LiquidBatch hot = (LiquidBatch) mash(loaded, 75.0, 0.85, 0.12).outputs().get(0);
        assertTrue(hot.number(GrainProcessHarness.SUGAR, 0.0) < preferred.number(GrainProcessHarness.SUGAR, 0.0));
    }

    @Test
    void rejectsMissingWater() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        var invocation = loaded.find(
                BuiltinRegistrations.MASH,
                Optional.of(GrainProcessHarness.GRIST),
                Optional.of(GrainProcessHarness.WATER)
        );
        ProcessResult result = loaded.engine().execute(
                new CapabilityProcessExecutor(BuiltinRegistrations.MASH),
                invocation,
                ProcessInputs.ofSolids("grist", List.of(
                        GrainProcessHarness.lot(GrainProcessHarness.GRIST, 1, PropertyBag.empty())
                )),
                ProcessContext.of(65.0, 1.0, false)
        );
        assertFalse(result.success());
    }

    @Test
    void usesNamedGristPortInsteadOfFlatteningEverySolid() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        var invocation = loaded.find(
                BuiltinRegistrations.MASH,
                Optional.of(GrainProcessHarness.GRIST),
                Optional.of(GrainProcessHarness.WATER)
        );
        ProcessResult named = loaded.engine().execute(
                new CapabilityProcessExecutor(BuiltinRegistrations.MASH),
                invocation,
                new ProcessInputs(
                        java.util.Map.of(
                                "grist",
                                List.of(GrainProcessHarness.lot(
                                        GrainProcessHarness.GRIST,
                                        1,
                                        PropertyBag.empty().with(GrainProcessHarness.SUGAR, 0.85)
                                )),
                                "wheat",
                                List.of(GrainProcessHarness.lot(
                                        GrainProcessHarness.BARLEY,
                                        8,
                                        PropertyBag.empty().with(GrainProcessHarness.SUGAR, 0.10)
                                ))
                        ),
                        java.util.Map.of(
                                "water",
                                LiquidBatch.of(GrainProcessHarness.WATER, 1000, PropertyBag.empty())
                        )
                ),
                ProcessContext.of(65.0, 1.0, false)
        );
        assertTrue(named.success(), named.message());
        LiquidBatch wort = (LiquidBatch) named.outputs().get(0);
        assertEquals(0.85, wort.number(GrainProcessHarness.SUGAR, 0.0), 1e-6);
    }

    @Test
    void rejectsWrongLiquid() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        var invocation = loaded.find(
                BuiltinRegistrations.MASH,
                Optional.of(GrainProcessHarness.GRIST),
                Optional.of(GrainProcessHarness.WATER)
        );
        ProcessResult result = loaded.engine().execute(
                new CapabilityProcessExecutor(BuiltinRegistrations.MASH),
                invocation,
                ProcessInputs.of(
                        "grist",
                        List.of(GrainProcessHarness.lot(GrainProcessHarness.GRIST, 1, PropertyBag.empty())),
                        "water",
                        LiquidBatch.of(GrainProcessHarness.WORT, 1000, PropertyBag.empty())
                ),
                ProcessContext.of(65.0, 1.0, false)
        );
        assertFalse(result.success());
    }

    private static ProcessResult mash(
            GrainProcessHarness.Loaded loaded,
            double celsius,
            double sugar,
            double color
    ) {
        var invocation = loaded.find(
                BuiltinRegistrations.MASH,
                Optional.of(GrainProcessHarness.GRIST),
                Optional.of(GrainProcessHarness.WATER)
        );
        return loaded.engine().execute(
                new CapabilityProcessExecutor(BuiltinRegistrations.MASH),
                invocation,
                ProcessInputs.of(
                        "grist",
                        List.of(GrainProcessHarness.lot(
                                GrainProcessHarness.GRIST,
                                1,
                                PropertyBag.empty()
                                        .with(GrainProcessHarness.SUGAR, sugar)
                                        .with(GrainProcessHarness.COLOR, color)
                        )),
                        "water",
                        LiquidBatch.of(GrainProcessHarness.WATER, 1000, PropertyBag.empty())
                ),
                ProcessContext.of(celsius, 1.0, false)
        );
    }
}
