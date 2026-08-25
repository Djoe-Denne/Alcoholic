package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.domain.vessel.EnvironmentProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaltProcessorTest {
    @Test
    void maltsBarleyThroughSemanticTag() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        var invocation = loaded.find(
                BuiltinRegistrations.MALT,
                Optional.of(GrainProcessHarness.BARLEY),
                Optional.empty(),
                Optional.of(GrainProcessHarness.MALT_PALE)
        );
        ProcessResult result = loaded.engine().execute(
                new CapabilityProcessExecutor(BuiltinRegistrations.MALT),
                invocation,
                ProcessInputs.ofSolids("grain", List.of(
                        GrainProcessHarness.lot(GrainProcessHarness.BARLEY, 1, PropertyBag.empty())
                )),
                ProcessContext.of(15.0, 1.0, false)
        );
        assertTrue(result.success(), result.message());
        assertEquals(GrainProcessHarness.MALTED, result.items().get(0).item());
        assertEquals(0.85, (Double) result.items().get(0).properties().get(GrainProcessHarness.SUGAR), 1e-9);
        assertEquals(0.12, (Double) result.items().get(0).properties().get(GrainProcessHarness.COLOR), 1e-9);
    }

    @Test
    void refusesAmbiguousMaltSelectionWithoutDefinitionId() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        assertTrue(ProcessRecipeResolver.find(
                loaded.catalog(),
                loaded.api(),
                BuiltinRegistrations.MALT,
                loaded.matcher(),
                Optional.of(GrainProcessHarness.BARLEY),
                Optional.empty()
        ).isEmpty());
    }

    @Test
    void appliesSelectedKilnProfileRatherThanFirstMatchingDefinition() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        var dark = loaded.find(
                BuiltinRegistrations.MALT,
                Optional.of(GrainProcessHarness.BARLEY),
                Optional.empty(),
                Optional.of(GrainProcessHarness.MALT_DARK)
        );
        ProcessResult result = loaded.engine().execute(
                new CapabilityProcessExecutor(BuiltinRegistrations.MALT),
                dark,
                ProcessInputs.ofSolids("grain", List.of(
                        GrainProcessHarness.lot(GrainProcessHarness.BARLEY, 1, PropertyBag.empty())
                )),
                ProcessContext.of(15.0, 1.0, false)
        );
        assertTrue(result.success(), result.message());
        assertEquals(0.55, (Double) result.items().get(0).properties().get(GrainProcessHarness.SUGAR), 1e-9);
        assertEquals(0.75, (Double) result.items().get(0).properties().get(GrainProcessHarness.COLOR), 1e-9);
    }

    @Test
    void acceptsExternalProviderBarleyThroughTheSameTag() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        var invocation = loaded.find(
                BuiltinRegistrations.MALT,
                Optional.of(GrainProcessHarness.EXTERNAL_BARLEY),
                Optional.empty(),
                Optional.of(GrainProcessHarness.MALT_PALE)
        );
        ProcessResult result = loaded.engine().execute(
                new CapabilityProcessExecutor(BuiltinRegistrations.MALT),
                invocation,
                ProcessInputs.ofSolids("grain", List.of(
                        GrainProcessHarness.lot(GrainProcessHarness.EXTERNAL_BARLEY, 1, PropertyBag.empty())
                )),
                ProcessContext.of(15.0, 1.0, false)
        );
        assertTrue(result.success(), result.message());
        assertEquals(GrainProcessHarness.MALTED, result.items().get(0).item());
    }

    @Test
    void rejectsUnknownSolids() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        assertTrue(ProcessRecipeResolver.find(
                loaded.catalog(),
                loaded.api(),
                BuiltinRegistrations.MALT,
                loaded.matcher(),
                Optional.of(GrainProcessHarness.HOPS),
                Optional.empty()
        ).isEmpty());
    }

    @Test
    void rejectsInsufficientInput() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        var invocation = loaded.find(
                BuiltinRegistrations.MALT,
                Optional.of(GrainProcessHarness.BARLEY),
                Optional.empty(),
                Optional.of(GrainProcessHarness.MALT_PALE)
        );
        ProcessResult result = loaded.engine().execute(
                new CapabilityProcessExecutor(BuiltinRegistrations.MALT),
                invocation,
                ProcessInputs.empty(),
                ProcessContext.of(15.0, 1.0, false)
        );
        assertFalse(result.success());
    }

    @Test
    void rejectsWhenEnvironmentHumidityIsBelowRequirement() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        var invocation = loaded.find(
                BuiltinRegistrations.MALT,
                Optional.of(GrainProcessHarness.BARLEY),
                Optional.empty(),
                Optional.of(GrainProcessHarness.MALT_PALE)
        );
        ProcessResult result = loaded.engine().execute(
                new CapabilityProcessExecutor(BuiltinRegistrations.MALT),
                invocation,
                ProcessInputs.ofSolids("grain", List.of(
                        GrainProcessHarness.lot(GrainProcessHarness.BARLEY, 1, PropertyBag.empty())
                )),
                ProcessContext.of(
                        15.0,
                        1.0,
                        false,
                        Optional.empty(),
                        Optional.of(new EnvironmentProfile(15.0, 0.5, true, 0.1)),
                        0L
                )
        );
        assertFalse(result.success());
    }
}
