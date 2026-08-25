package com.djden.alcoholic.application.process;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrainProcessGraphTest {
    @Test
    void followsGenericProcessGraphFromGrainToFermentedLiquid() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        ExecuteProcessUseCase engine = loaded.engine();

        ProcessResult malted = engine.execute(
                new CapabilityProcessExecutor(BuiltinRegistrations.MALT),
                loaded.find(
                        BuiltinRegistrations.MALT,
                        Optional.of(GrainProcessHarness.BARLEY),
                        Optional.empty(),
                        Optional.of(GrainProcessHarness.MALT_PALE)
                ),
                ProcessInputs.ofSolids("grain", List.of(
                        GrainProcessHarness.lot(GrainProcessHarness.BARLEY, 1, PropertyBag.empty())
                )),
                ProcessContext.of(15.0, 1.0, false)
        );
        assertTrue(malted.success(), malted.message());

        ProcessResult milled = engine.execute(
                new CapabilityProcessExecutor(BuiltinRegistrations.MILL),
                loaded.find(BuiltinRegistrations.MILL, Optional.of(GrainProcessHarness.MALTED), Optional.empty()),
                ProcessInputs.ofSolids("malt", List.of(
                        GrainProcessHarness.lot(
                                GrainProcessHarness.MALTED,
                                1,
                                new PropertyBag(malted.items().get(0).properties())
                        )
                )),
                ProcessContext.empty()
        );
        assertTrue(milled.success(), milled.message());
        assertEquals(GrainProcessHarness.GRIST, milled.items().get(0).item());
        assertEquals(0.85, (Double) milled.items().get(0).properties().get(GrainProcessHarness.SUGAR), 1e-9);
        assertEquals(0.12, (Double) milled.items().get(0).properties().get(GrainProcessHarness.COLOR), 1e-9);

        ProcessResult mashed = engine.execute(
                new CapabilityProcessExecutor(BuiltinRegistrations.MASH),
                loaded.find(
                        BuiltinRegistrations.MASH,
                        Optional.of(GrainProcessHarness.GRIST),
                        Optional.of(GrainProcessHarness.WATER)
                ),
                ProcessInputs.of(
                        "grist",
                        List.of(GrainProcessHarness.lot(
                                GrainProcessHarness.GRIST,
                                1,
                                new PropertyBag(milled.items().get(0).properties())
                        )),
                        "water",
                        LiquidBatch.of(GrainProcessHarness.WATER, 1000, PropertyBag.empty())
                ),
                ProcessContext.of(65.0, 1.0, false)
        );
        assertTrue(mashed.success(), mashed.message());
        LiquidBatch wort = (LiquidBatch) mashed.outputs().get(0);

        ProcessResult boiled = engine.execute(
                new CapabilityProcessExecutor(BuiltinRegistrations.BOIL),
                loaded.find(BuiltinRegistrations.BOIL, Optional.empty(), Optional.of(GrainProcessHarness.WORT)),
                ProcessInputs.of(
                        "hops",
                        List.of(GrainProcessHarness.lot(GrainProcessHarness.HOPS, 1, PropertyBag.empty())),
                        "wort",
                        wort
                ),
                ProcessContext.of(100.0, 1.0, false)
        );
        assertTrue(boiled.success(), boiled.message());
        LiquidBatch hopped = (LiquidBatch) boiled.outputs().get(0);

        var ferment = loaded.find(
                BuiltinRegistrations.FERMENT,
                Optional.empty(),
                Optional.of(GrainProcessHarness.HOPPED)
        );
        ProcessResult fermented = engine.execute(
                new CapabilityProcessExecutor(BuiltinRegistrations.FERMENT),
                ferment,
                ProcessInputs.ofLiquid("wort", hopped),
                ProcessContext.of(20.0, 1.0, true)
        );
        for (int tick = 0; tick < 80 && fermented.success(); tick++) {
            LiquidBatch current = (LiquidBatch) fermented.outputs().get(0);
            if (current.baseLiquid().filter(GrainProcessHarness.BEVERAGE::equals).isPresent()
                    && current.number(GrainProcessHarness.SUGAR, 1.0) <= 0.02) {
                break;
            }
            fermented = engine.execute(
                    new CapabilityProcessExecutor(BuiltinRegistrations.FERMENT),
                    ferment,
                    ProcessInputs.ofLiquid("wort", current),
                    ProcessContext.of(20.0, 1.0, true)
            );
        }
        assertTrue(fermented.success(), fermented.message());
        LiquidBatch finished = (LiquidBatch) fermented.outputs().get(0);
        assertEquals(GrainProcessHarness.BEVERAGE, finished.baseLiquid().orElseThrow());
        assertTrue(finished.number(GrainProcessHarness.ETHANOL, 0.0) > 0.0);
    }
}
