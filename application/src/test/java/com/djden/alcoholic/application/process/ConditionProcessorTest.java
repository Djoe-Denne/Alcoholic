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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionProcessorTest {
    @Test
    void maturesFinishedBeerWithoutChangingItsLiquidIdentity() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        LiquidBatch beer = LiquidBatch.of(
                GrainProcessHarness.BEVERAGE,
                1000,
                PropertyBag.empty()
                        .with(GrainProcessHarness.SUGAR, 0.08)
                        .with(GrainProcessHarness.ETHANOL, 0.40)
        );
        LiquidBatch finished = conditionUntilDone(loaded, beer, 8.0, true);
        assertEquals(GrainProcessHarness.BEVERAGE, finished.baseLiquid().orElseThrow());
        assertTrue(finished.number(GrainProcessHarness.MATURITY, 0.0) >= 0.85);
        assertTrue(finished.number(GrainProcessHarness.CARBONATION, 0.0) > 0.0);
    }

    @Test
    void carbonationRequiresYeastAndResidualSugar() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        LiquidBatch beer = LiquidBatch.of(
                GrainProcessHarness.BEVERAGE,
                1000,
                PropertyBag.empty().with(GrainProcessHarness.SUGAR, 0.10)
        );
        LiquidBatch withYeast = conditionUntilDone(loaded, beer, 8.0, true);
        LiquidBatch withoutYeast = conditionUntilDone(loaded, beer, 8.0, false);
        assertTrue(withYeast.number(GrainProcessHarness.CARBONATION, 0.0) > 0.0);
        assertEquals(0.0, withoutYeast.number(GrainProcessHarness.CARBONATION, 0.0), 1e-9);
    }

    @Test
    void rejectsTemperatureOutsideOperatingRange() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        LiquidBatch beer = LiquidBatch.of(GrainProcessHarness.BEVERAGE, 1000, PropertyBag.empty());
        ProcessResult result = condition(loaded, beer, 40.0, 1.0, false);
        assertFalse(result.success());
    }

    @Test
    void appliesToFinishedBeerFromTheOfficialDag() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        assertTrue(ProcessRecipeResolver.find(
                loaded.catalog(),
                loaded.api(),
                BuiltinRegistrations.CONDITION,
                loaded.matcher(),
                Optional.empty(),
                Optional.of(GrainProcessHarness.BEVERAGE)
        ).isPresent());
    }

    private static LiquidBatch conditionUntilDone(
            GrainProcessHarness.Loaded loaded,
            LiquidBatch start,
            double celsius,
            boolean yeast
    ) {
        ProcessResult result = condition(loaded, start, celsius, 1.0, yeast);
        for (int tick = 0; tick < 200 && result.success(); tick++) {
            LiquidBatch current = (LiquidBatch) result.outputs().get(0);
            if (current.baseLiquid().filter(GrainProcessHarness.BEVERAGE::equals).isPresent()
                    && current.number(GrainProcessHarness.MATURITY, 0.0) + 1e-9 >= 0.85) {
                return current;
            }
            result = condition(loaded, current, celsius, 1.0, yeast);
        }
        assertTrue(result.success(), result.message());
        return (LiquidBatch) result.outputs().get(0);
    }

    private static ProcessResult condition(
            GrainProcessHarness.Loaded loaded,
            LiquidBatch batch,
            double celsius,
            double delta,
            boolean yeast
    ) {
        var invocation = loaded.find(
                BuiltinRegistrations.CONDITION,
                Optional.empty(),
                Optional.of(batch.baseLiquid().orElse(GrainProcessHarness.BEVERAGE))
        );
        return loaded.engine().execute(
                new CapabilityProcessExecutor(BuiltinRegistrations.CONDITION),
                invocation,
                ProcessInputs.ofLiquid("input", batch),
                ProcessContext.of(celsius, delta, yeast)
        );
    }
}
