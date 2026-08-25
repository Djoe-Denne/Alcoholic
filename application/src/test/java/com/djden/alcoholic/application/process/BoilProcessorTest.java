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

class BoilProcessorTest {
    @Test
    void boilsWortWithHopsAndPropagatesBitterness() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        LiquidBatch wort = LiquidBatch.of(
                GrainProcessHarness.WORT,
                1000,
                PropertyBag.empty()
                        .with(GrainProcessHarness.SUGAR, 0.80)
                        .with(GrainProcessHarness.COLOR, 0.12)
                        .with(GrainProcessHarness.ETHANOL, 0.0)
        );
        ProcessResult result = boil(loaded, wort, 100.0, 1);
        assertTrue(result.success(), result.message());
        LiquidBatch hopped = (LiquidBatch) result.outputs().get(0);
        assertEquals(GrainProcessHarness.HOPPED, hopped.baseLiquid().orElseThrow());
        assertEquals(1000.0, hopped.volume(), 1e-9);
        assertEquals(0.80, hopped.number(GrainProcessHarness.SUGAR, 0.0), 1e-9);
        assertEquals(0.12, hopped.number(GrainProcessHarness.COLOR, 0.0), 1e-9);
        assertTrue(hopped.number(GrainProcessHarness.BITTERNESS, 0.0) > 0.0);
    }

    @Test
    void preservesUnrelatedProperties() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        LiquidBatch wort = LiquidBatch.of(
                GrainProcessHarness.WORT,
                500,
                PropertyBag.empty()
                        .with(GrainProcessHarness.SUGAR, 0.66)
                        .with(ResourceId.parse("alcoholic:acidity"), 0.22)
        );
        LiquidBatch hopped = (LiquidBatch) boil(loaded, wort, 100.0, 1).outputs().get(0);
        assertEquals(0.66, hopped.number(GrainProcessHarness.SUGAR, 0.0), 1e-9);
        assertEquals(0.22, hopped.number(ResourceId.parse("alcoholic:acidity"), 0.0), 1e-9);
    }

    @Test
    void rejectsMissingHopsWhenRequired() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        LiquidBatch wort = LiquidBatch.of(GrainProcessHarness.WORT, 1000, PropertyBag.empty());
        var invocation = loaded.find(
                BuiltinRegistrations.BOIL,
                Optional.empty(),
                Optional.of(GrainProcessHarness.WORT)
        );
        ProcessResult result = loaded.engine().execute(
                new CapabilityProcessExecutor(BuiltinRegistrations.BOIL),
                invocation,
                ProcessInputs.ofLiquid("wort", wort),
                ProcessContext.of(100.0, 1.0, false)
        );
        assertFalse(result.success());
    }

    @Test
    void rejectsColdBoil() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        LiquidBatch wort = LiquidBatch.of(GrainProcessHarness.WORT, 1000, PropertyBag.empty());
        ProcessResult result = boil(loaded, wort, 40.0, 1);
        assertFalse(result.success());
    }

    private static ProcessResult boil(
            GrainProcessHarness.Loaded loaded,
            LiquidBatch wort,
            double celsius,
            int hops
    ) {
        var invocation = loaded.find(
                BuiltinRegistrations.BOIL,
                Optional.empty(),
                Optional.of(GrainProcessHarness.WORT)
        );
        return loaded.engine().execute(
                new CapabilityProcessExecutor(BuiltinRegistrations.BOIL),
                invocation,
                ProcessInputs.of(
                        "hops",
                        List.of(GrainProcessHarness.lot(GrainProcessHarness.HOPS, hops, PropertyBag.empty())),
                        "wort",
                        wort
                ),
                ProcessContext.of(celsius, 1.0, false)
        );
    }
}
