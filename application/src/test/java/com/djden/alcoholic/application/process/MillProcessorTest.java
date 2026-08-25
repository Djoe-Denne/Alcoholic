package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.application.beverage.builtin.BuiltinRegistrations;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MillProcessorTest {
    @Test
    void millsMaltedGrainIntoGrist() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        var invocation = loaded.find(
                BuiltinRegistrations.MILL,
                Optional.of(GrainProcessHarness.MALTED),
                Optional.empty()
        );
        ProcessResult result = loaded.engine().execute(
                new CapabilityProcessExecutor(BuiltinRegistrations.MILL),
                invocation,
                ProcessInputs.ofSolids("malt", List.of(
                        GrainProcessHarness.lot(
                                GrainProcessHarness.MALTED,
                                1,
                                PropertyBag.empty()
                                        .with(GrainProcessHarness.SUGAR, 0.85)
                                        .with(GrainProcessHarness.COLOR, 0.12)
                        )
                )),
                ProcessContext.empty()
        );
        assertTrue(result.success(), result.message());
        assertEquals(GrainProcessHarness.GRIST, result.items().get(0).item());
        assertEquals(0.85, (Double) result.items().get(0).properties().get(GrainProcessHarness.SUGAR), 1e-9);
    }
}
