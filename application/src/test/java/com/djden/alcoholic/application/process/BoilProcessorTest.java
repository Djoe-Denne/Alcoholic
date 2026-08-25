package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessRequest;
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
    void additionsOnlyScheduleRequiresEveryPlannedAddition() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        BoilConfig base = BoilConfig.CODEC.decode(
                loaded.catalog().process(ResourceId.parse("alcoholic:boil_wort")).orElseThrow().config()
        );
        BoilConfig scheduled = new BoilConfig(
                base.inputLiquid(),
                base.outputLiquid(),
                Optional.empty(),
                1,
                base.processingTicks(),
                base.temperature(),
                base.hopProfile(),
                base.bitternessProperty(),
                base.aromaProperty(),
                List.of(
                        new BoilConfig.BoilAddition(base.inputSelector().orElseThrow(), 0.0, "bittering"),
                        new BoilConfig.BoilAddition(base.inputSelector().orElseThrow(), 0.8, "aroma")
                )
        );
        LiquidBatch wort = LiquidBatch.of(GrainProcessHarness.WORT, 1000, PropertyBag.empty());
        ProcessResult missingOne = new BoilProcessor().apply(
                ProcessRequest.of(ProcessInputs.of(
                        "hops",
                        List.of(GrainProcessHarness.lot(GrainProcessHarness.HOPS, 1, PropertyBag.empty())),
                        "wort",
                        wort
                )),
                scheduled,
                ProcessContext.of(100.0, 1.0, false)
        );
        assertFalse(missingOne.success());
        ProcessResult complete = new BoilProcessor().apply(
                ProcessRequest.of(ProcessInputs.of(
                        "hops",
                        List.of(GrainProcessHarness.lot(GrainProcessHarness.HOPS, 2, PropertyBag.empty())),
                        "wort",
                        wort
                )),
                scheduled,
                ProcessContext.of(100.0, 1.0, false)
        );
        assertTrue(complete.success(), complete.message());
    }

    @Test
    void bitteringAdditionsExtractMoreBitternessThanAromaAdditions() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        LiquidBatch wort = LiquidBatch.of(
                GrainProcessHarness.WORT,
                1000,
                PropertyBag.empty().with(GrainProcessHarness.SUGAR, 0.70)
        );
        LiquidBatch bittering = (LiquidBatch) boilWithRole(loaded, wort, "bittering", 0.0).outputs().get(0);
        LiquidBatch aroma = (LiquidBatch) boilWithRole(loaded, wort, "aroma", 0.8).outputs().get(0);
        assertTrue(
                bittering.number(GrainProcessHarness.BITTERNESS, 0.0)
                        > aroma.number(GrainProcessHarness.BITTERNESS, 0.0)
        );
        assertTrue(
                aroma.number(GrainProcessHarness.AROMA, 0.0)
                        > bittering.number(GrainProcessHarness.AROMA, 0.0)
        );
        assertEquals(0.70, bittering.number(GrainProcessHarness.SUGAR, 0.0), 1e-9);
    }

    @Test
    void earlyAndLateAdditionsAffectBitternessAndAromaDifferently() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        LiquidBatch wort = LiquidBatch.of(GrainProcessHarness.WORT, 1000, PropertyBag.empty());
        LiquidBatch early = (LiquidBatch) boilWithRole(loaded, wort, "dual", 0.0).outputs().get(0);
        LiquidBatch late = (LiquidBatch) boilWithRole(loaded, wort, "dual", 1.0).outputs().get(0);

        assertTrue(early.number(GrainProcessHarness.BITTERNESS, 0.0)
                > late.number(GrainProcessHarness.BITTERNESS, 0.0));
        assertTrue(late.number(GrainProcessHarness.AROMA, 0.0)
                > early.number(GrainProcessHarness.AROMA, 0.0));
    }

    @Test
    void explicitHopPotentialsScaleWithItemCount() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        LiquidBatch wort = LiquidBatch.of(GrainProcessHarness.WORT, 1000, PropertyBag.empty());
        LiquidBatch one = (LiquidBatch) boilWithPotentials(loaded, wort, 1).outputs().get(0);
        LiquidBatch two = (LiquidBatch) boilWithPotentials(loaded, wort, 2).outputs().get(0);

        assertEquals(
                one.number(GrainProcessHarness.BITTERNESS, 0.0) * 2.0,
                two.number(GrainProcessHarness.BITTERNESS, 0.0),
                1e-9
        );
        assertEquals(
                one.number(GrainProcessHarness.AROMA, 0.0) * 2.0,
                two.number(GrainProcessHarness.AROMA, 0.0),
                1e-9
        );
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

    private static ProcessResult boilWithRole(
            GrainProcessHarness.Loaded loaded,
            LiquidBatch wort,
            String role,
            double atProgress
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
                        List.of(GrainProcessHarness.lot(
                                GrainProcessHarness.HOPS,
                                1,
                                PropertyBag.empty()
                                        .with(GrainProcessHarness.ADDITION_ROLE, role)
                                        .with(GrainProcessHarness.ADDITION_PROGRESS, atProgress)
                        )),
                        "wort",
                        wort
                ),
                ProcessContext.of(100.0, 1.0, false)
        );
    }

    private static ProcessResult boilWithPotentials(
            GrainProcessHarness.Loaded loaded,
            LiquidBatch wort,
            int count
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
                        List.of(GrainProcessHarness.lot(
                                GrainProcessHarness.HOPS,
                                count,
                                PropertyBag.empty()
                                        .with(GrainProcessHarness.BITTERNESS, 0.30)
                                        .with(GrainProcessHarness.AROMA, 0.20)
                        )),
                        "wort",
                        wort
                ),
                ProcessContext.of(100.0, 1.0, false)
        );
    }
}
