package com.djden.alcoholic.domain.quality;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.domain.beverage.GraphIssue;
import com.djden.alcoholic.domain.beverage.OutputReference;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QualityGraphValidatorTest {
    @Test
    void shippedGraphsAreAcyclic() {
        BuiltinQualityGraphs.all().values().forEach(graph ->
                assertTrue(QualityGraphValidator.validate(graph, "quality/" + graph.id()).isEmpty())
        );
    }

    @Test
    void rejectsCycles() {
        QualityNode first = new QualityNode(
                "a",
                BuiltinQualityOperators.READ,
                DataNode.object(Map.of()),
                Map.of("in", new QualityInput.NodePort("b")),
                List.of("value")
        );
        QualityNode second = new QualityNode(
                "b",
                BuiltinQualityOperators.READ,
                DataNode.object(Map.of()),
                Map.of("in", new QualityInput.NodePort("a")),
                List.of("value")
        );
        QualityGraph graph = new QualityGraph(
                ResourceId.parse("test:loop"),
                List.of(first, second),
                Map.of("profile", new OutputReference("a", "value"))
        );
        List<GraphIssue> issues = QualityGraphValidator.validate(graph, "quality/test:loop");
        assertFalse(issues.isEmpty());
        assertTrue(issues.get(0).message().contains("cycle"));
        assertTrue(issues.get(0).message().contains("a"));
        assertTrue(issues.get(0).message().contains("b"));
    }

    @Test
    void rejectsGraphsExceedingNodeLimit() {
        QualityGraph graph = graphWithNodes(QualityGraphValidator.MAX_NODES + 1);
        List<GraphIssue> issues = QualityGraphValidator.validate(graph, "quality/test:huge");
        assertEquals(1, issues.size());
        assertTrue(issues.get(0).message().contains("exceeds " + QualityGraphValidator.MAX_NODES));
        assertTrue(issues.get(0).path().endsWith("/nodes"));
    }

    @Test
    void acceptsGraphAtNodeLimit() {
        QualityGraph graph = graphWithNodes(QualityGraphValidator.MAX_NODES);
        assertTrue(QualityGraphValidator.validate(graph, "quality/test:limit").isEmpty());
    }

    private static QualityGraph graphWithNodes(int count) {
        List<QualityNode> nodes = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            nodes.add(new QualityNode(
                    "n" + index,
                    BuiltinQualityOperators.READ,
                    DataNode.object(Map.of()),
                    Map.of(),
                    List.of("value")
            ));
        }
        return new QualityGraph(
                ResourceId.parse("test:sized"),
                nodes,
                Map.of("profile", new OutputReference("n0", "value"))
        );
    }
}
