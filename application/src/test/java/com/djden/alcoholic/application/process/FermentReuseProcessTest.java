package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FermentReuseProcessTest {
    @Test
    void hoppedWortFermentsThroughGenericFermentOnly() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        LiquidBatch hopped = LiquidBatch.of(
                GrainProcessHarness.HOPPED,
                1000,
                PropertyBag.empty()
                        .with(GrainProcessHarness.SUGAR, 0.80)
                        .with(GrainProcessHarness.ETHANOL, 0.01)
                        .with(GrainProcessHarness.BITTERNESS, 0.40)
        );
        var invocation = loaded.find(
                BuiltinRegistrations.FERMENT,
                Optional.empty(),
                Optional.of(GrainProcessHarness.HOPPED)
        );
        assertEquals(BuiltinRegistrations.FERMENT, invocation.processType());
        CapabilityProcessExecutor fermenter = new CapabilityProcessExecutor(BuiltinRegistrations.FERMENT);
        ProcessResult fermented = loaded.engine().execute(
                fermenter,
                invocation,
                ProcessInputs.ofLiquid("wort", hopped),
                ProcessContext.of(20.0, 1.0, true)
        );
        for (int tick = 0; tick < 80 && fermented.success(); tick++) {
            LiquidBatch current = (LiquidBatch) fermented.outputs().get(0);
            if (current.number(GrainProcessHarness.SUGAR, 1.0) <= 0.02
                    && current.baseLiquid().filter(GrainProcessHarness.BEVERAGE::equals).isPresent()) {
                break;
            }
            fermented = loaded.engine().execute(
                    fermenter,
                    invocation,
                    ProcessInputs.ofLiquid("wort", current),
                    ProcessContext.of(20.0, 1.0, true)
            );
        }
        assertTrue(fermented.success(), fermented.message());
        LiquidBatch finished = (LiquidBatch) fermented.outputs().get(0);
        assertTrue(finished.number(GrainProcessHarness.SUGAR, 1.0) < 0.80);
        assertTrue(finished.number(GrainProcessHarness.ETHANOL, 0.0) > 0.01);
        assertEquals(GrainProcessHarness.BEVERAGE, finished.baseLiquid().orElseThrow());
        assertEquals(0.40, finished.number(GrainProcessHarness.BITTERNESS, 0.0), 1e-9);
    }
}
