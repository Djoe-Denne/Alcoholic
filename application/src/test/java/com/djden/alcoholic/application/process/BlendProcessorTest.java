package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessRequest;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.api.property.PropertyMerge;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlendProcessorTest {
    private static final ResourceId YOUNG = ResourceId.parse("test:young");
    private static final ResourceId FINISHED = ResourceId.parse("test:finished");
    private static final ResourceId CUVEE = ResourceId.parse("test:cuvee");
    private static final ResourceId ETHANOL = ResourceId.parse("alcoholic:ethanol");

    @Test
    void blendsDistinctDefinitionsIntoConfiguredOutput() {
        BlendConfig config = new BlendConfig(Set.of(YOUNG, FINISHED), Optional.of(CUVEE), 2, Optional.empty());
        LiquidBatch left = LiquidBatch.of(YOUNG, 750, PropertyBag.empty().with(ETHANOL, 0.10));
        LiquidBatch right = LiquidBatch.of(FINISHED, 250, PropertyBag.empty().with(ETHANOL, 0.14));
        ProcessResult result = new BlendProcessor().apply(
                ProcessRequest.of(new ProcessInputs(Map.of(), Map.of("a", left, "b", right))),
                config,
                ProcessContext.empty()
        );
        assertTrue(result.success(), result.message());
        LiquidBatch blended = (LiquidBatch) result.outputs().get(0);
        assertEquals(CUVEE, blended.baseLiquid().orElseThrow());
        assertEquals(1000.0, blended.volume(), 1e-9);
        assertEquals(0.11, blended.number(ETHANOL, 0.0), 1e-9);
        assertEquals(0.75, blended.batchProvenance().blendComposition().get(YOUNG), 1e-9);
    }

    @Test
    void rejectsWhenAPropertyCannotAggregate() {
        BlendConfig config = new BlendConfig(Set.of(YOUNG, FINISHED), Optional.of(CUVEE), 2, Optional.empty());
        ResourceId lot = ResourceId.parse("test:lot");
        LiquidBatch left = LiquidBatch.of(YOUNG, 500, PropertyBag.empty().with(lot, "a"));
        LiquidBatch right = LiquidBatch.of(FINISHED, 500, PropertyBag.empty().with(lot, "b"));
        ProcessResult result = new BlendProcessor(
                id -> PropertyMerge.IDENTICAL_OR_REJECT,
                id -> com.djden.alcoholic.api.property.PropertyAggregator.forStrategy(PropertyMerge.IDENTICAL_OR_REJECT)
        ).apply(
                ProcessRequest.of(new ProcessInputs(Map.of(), Map.of("a", left, "b", right))),
                config,
                ProcessContext.empty()
        );
        assertTrue(!result.success());
    }
}
