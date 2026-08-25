package com.djden.alcoholic.domain.liquid;

import com.djden.alcoholic.api.ResourceId;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchProvenanceTest {
    @Test
    void flattensMergeAndDropsTinyFractions() {
        Map<ResourceId, Double> many = new LinkedHashMap<>();
        for (int index = 0; index < 20; index++) {
            many.put(ResourceId.parse("test:origin_" + index), 1.0);
        }
        many.put(ResourceId.parse("test:trace"), 0.004);
        BatchProvenance left = new BatchProvenance(many, Map.of(), 0.2, 10.0, 0.1, 0.05);
        BatchProvenance right = BatchProvenance.ofOrigin(ResourceId.parse("test:origin_0"), 1.0);
        BatchProvenance merged = left.merge(right, 1000, 1000);

        assertTrue(merged.originComposition().size() <= BatchProvenance.MAX_MAP_ENTRIES);
        double sum = merged.originComposition().values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(1.0, sum, 1e-9);
        assertTrue(merged.originComposition().values().stream().allMatch(value -> value >= BatchProvenance.MIN_FRACTION - 1e-12));
        assertEquals(0.1, merged.fermentationStress(), 1e-9);
    }

    @Test
    void blendRecordsDefinitionFractions() {
        ResourceId first = ResourceId.parse("test:young");
        ResourceId second = ResourceId.parse("test:finished");
        BatchProvenance blended = BatchProvenance.empty().blendWith(
                first,
                second,
                BatchProvenance.empty(),
                750,
                250
        );
        assertEquals(0.75, blended.blendComposition().get(first), 1e-9);
        assertEquals(0.25, blended.blendComposition().get(second), 1e-9);
    }
}
