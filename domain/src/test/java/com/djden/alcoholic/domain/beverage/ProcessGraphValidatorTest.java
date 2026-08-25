package com.djden.alcoholic.domain.beverage;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessGraphValidatorTest {
    @Test
    void acceptsAcyclicMultiInputGraph() {
        ProcessGraph graph = new ProcessGraph(
                List.of(
                        node("mill", Map.of(), List.of("grist")),
                        node(
                                "mash",
                                Map.of(
                                        "grist", new InputReference.NodeOutputInput("mill", "grist"),
                                        "water", new InputReference.ItemInput(ResourceId.parse("minecraft:water_bucket"))
                                ),
                                List.of("wort")
                        )
                ),
                Map.of("result", new OutputReference("mash", "wort"))
        );

        assertTrue(ProcessGraphValidator.validate(graph, "graph").isEmpty());
    }

    @Test
    void rejectsCyclesAndUnknownPorts() {
        ProcessGraph graph = new ProcessGraph(
                List.of(
                        node(
                                "a",
                                Map.of("in", new InputReference.NodeOutputInput("b", "out")),
                                List.of("out")
                        ),
                        node(
                                "b",
                                Map.of("in", new InputReference.NodeOutputInput("a", "missing")),
                                List.of("out")
                        )
                ),
                Map.of()
        );

        List<GraphIssue> issues = ProcessGraphValidator.validate(graph, "graph");
        assertTrue(issues.stream().anyMatch(issue -> issue.message().contains("cycle")));
        assertTrue(issues.stream().anyMatch(issue -> issue.message().contains("unknown port")));
    }

    @Test
    void liquidBatchKeepsTypedPropertiesWithoutFixedFields() {
        ResourceId ethanol = ResourceId.parse("alcoholic:ethanol");
        LiquidBatch batch = LiquidBatch.of(1.0).withProperty(ethanol, 0.12);
        assertEquals(1.0, batch.volume());
        assertEquals(Optional.of(0.12), batch.property(ethanol));
    }

    private static ProcessNode node(
            String id,
            Map<String, InputReference> inputs,
            List<String> outputs
    ) {
        return new ProcessNode(
                id,
                Optional.of(ResourceId.parse("alcoholic:press")),
                Optional.empty(),
                DataNode.object(Map.of()),
                inputs,
                outputs
        );
    }
}
