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
    void rejectsCycles() {
        QualityNode first = node("a", Map.of("in", new QualityInput.NodePort("b")));
        QualityNode second = node("b", Map.of("in", new QualityInput.NodePort("a")));
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

    @Test
    void rejectsEmptyGraphMissingProfile() {
        QualityGraph graph = new QualityGraph(ResourceId.parse("test:empty"), List.of(), Map.of());
        List<GraphIssue> issues = QualityGraphValidator.validate(graph, "quality/test:empty");
        assertTrue(issues.stream().anyMatch(issue -> issue.message().contains("outputs.profile")));
    }

    @Test
    void acceptsSingleNodeGraph() {
        QualityGraph graph = new QualityGraph(
                ResourceId.parse("test:one"),
                List.of(node("only", Map.of())),
                Map.of("profile", new OutputReference("only", "value"))
        );
        assertTrue(QualityGraphValidator.validate(graph, "quality/test:one").isEmpty());
    }

    @Test
    void rejectsDuplicateNodeIds() {
        QualityGraph graph = new QualityGraph(
                ResourceId.parse("test:dup"),
                List.of(node("x", Map.of()), node("x", Map.of())),
                Map.of("profile", new OutputReference("x", "value"))
        );
        List<GraphIssue> issues = QualityGraphValidator.validate(graph, "quality/test:dup");
        assertTrue(issues.stream().anyMatch(issue -> issue.message().contains("duplicate node")));
    }

    @Test
    void rejectsUnknownInputNode() {
        QualityGraph graph = new QualityGraph(
                ResourceId.parse("test:missing"),
                List.of(node("x", Map.of("in", new QualityInput.NodePort("gone")))),
                Map.of("profile", new OutputReference("x", "value"))
        );
        List<GraphIssue> issues = QualityGraphValidator.validate(graph, "quality/test:missing");
        assertTrue(issues.stream().anyMatch(issue -> issue.message().contains("unknown node gone")));
    }

    @Test
    void rejectsUnknownOutputPort() {
        QualityGraph graph = new QualityGraph(
                ResourceId.parse("test:port"),
                List.of(node("x", Map.of())),
                Map.of("profile", new OutputReference("x", "summary"))
        );
        List<GraphIssue> issues = QualityGraphValidator.validate(graph, "quality/test:port");
        assertTrue(issues.stream().anyMatch(issue -> issue.message().contains("unknown port summary")));
    }

    private static QualityNode node(String id, Map<String, QualityInput> inputs) {
        return new QualityNode(
                id,
                BuiltinQualityOperators.READ,
                DataNode.object(Map.of()),
                inputs,
                List.of("value")
        );
    }

    private static QualityGraph graphWithNodes(int count) {
        List<QualityNode> nodes = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            nodes.add(node("n" + index, Map.of()));
        }
        return new QualityGraph(
                ResourceId.parse("test:sized"),
                nodes,
                Map.of("profile", new OutputReference("n0", "value"))
        );
    }
}
