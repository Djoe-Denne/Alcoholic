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
import static org.junit.jupiter.api.Assertions.assertTrue;

class QualityGraphValidatorTest {
    @Test
    void rejectsCycles() {
        QualityNode first = profileNode("a", Map.of("in", new QualityInput.NodePort("b")));
        QualityNode second = valueNode("b", Map.of("in", new QualityInput.NodePort("a", "summary")));
        QualityGraph graph = new QualityGraph(
                ResourceId.parse("test:loop"),
                List.of(first, second),
                Map.of("profile", new OutputReference("a", "summary"))
        );
        List<GraphIssue> issues = QualityGraphValidator.validate(graph, "quality/test:loop");
        assertTrue(issues.stream().anyMatch(issue -> issue.message().contains("cycle")
                && issue.message().contains("a")
                && issue.message().contains("b")));
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
                List.of(profileNode("only", Map.of())),
                Map.of("profile", new OutputReference("only", "summary"))
        );
        assertTrue(QualityGraphValidator.validate(graph, "quality/test:one").isEmpty());
    }

    @Test
    void rejectsDuplicateNodeIds() {
        QualityGraph graph = new QualityGraph(
                ResourceId.parse("test:dup"),
                List.of(profileNode("x", Map.of()), profileNode("x", Map.of())),
                Map.of("profile", new OutputReference("x", "summary"))
        );
        List<GraphIssue> issues = QualityGraphValidator.validate(graph, "quality/test:dup");
        assertTrue(issues.stream().anyMatch(issue -> issue.message().contains("duplicate node")));
    }

    @Test
    void rejectsUnknownInputNode() {
        QualityGraph graph = new QualityGraph(
                ResourceId.parse("test:missing"),
                List.of(profileNode("x", Map.of("in", new QualityInput.NodePort("gone")))),
                Map.of("profile", new OutputReference("x", "summary"))
        );
        List<GraphIssue> issues = QualityGraphValidator.validate(graph, "quality/test:missing");
        assertTrue(issues.stream().anyMatch(issue -> issue.message().contains("unknown node gone")));
    }

    @Test
    void rejectsUnknownOutputPort() {
        QualityGraph graph = new QualityGraph(
                ResourceId.parse("test:port"),
                List.of(valueNode("x", Map.of())),
                Map.of("profile", new OutputReference("x", "summary"))
        );
        List<GraphIssue> issues = QualityGraphValidator.validate(graph, "quality/test:port");
        assertTrue(issues.stream().anyMatch(issue -> issue.message().contains("unknown port summary")));
    }

    @Test
    void acceptsSumInputs() {
        QualityGraph graph = new QualityGraph(
                ResourceId.parse("test:sum"),
                List.of(
                        valueNode("left", Map.of()),
                        valueNode("right", Map.of()),
                        profileNode("fold", Map.of(
                                "complexity",
                                new QualityInput.Sum(List.of(
                                        new QualityInput.NodePort("left"),
                                        new QualityInput.NodePort("right")
                                ))
                        ))
                ),
                Map.of("profile", new OutputReference("fold", "summary"))
        );
        assertTrue(QualityGraphValidator.validate(graph, "quality/test:sum").isEmpty());
    }

    @Test
    void rejectsUndeclaredValuePort() {
        QualityNode oxygen = new QualityNode(
                "oxygen",
                BuiltinQualityOperators.OXYGEN_CURVE,
                DataNode.object(Map.of()),
                Map.of(),
                List.of("complexity", "defects")
        );
        QualityGraph graph = new QualityGraph(
                ResourceId.parse("test:value"),
                List.of(
                        oxygen,
                        profileNode("fold", Map.of("complexity", new QualityInput.NodePort("oxygen", "value")))
                ),
                Map.of("profile", new OutputReference("fold", "summary"))
        );
        List<GraphIssue> issues = QualityGraphValidator.validate(graph, "quality/test:value");
        assertTrue(issues.stream().anyMatch(issue -> issue.message().contains("unknown port value")));
    }

    @Test
    void rejectsProfilePointedAtHarvestValue() {
        QualityGraph graph = new QualityGraph(
                ResourceId.parse("test:harvest"),
                List.of(valueNode("harvest", Map.of())),
                Map.of("profile", new OutputReference("harvest", "value"))
        );
        List<GraphIssue> issues = QualityGraphValidator.validate(graph, "quality/test:harvest");
        assertTrue(issues.stream().anyMatch(issue ->
                issue.message().contains("profile node harvest missing port purity")));
    }

    private static QualityNode profileNode(String id, Map<String, QualityInput> inputs) {
        return new QualityNode(
                id,
                BuiltinQualityOperators.FOLD_SUMMARY,
                DataNode.object(Map.of()),
                inputs,
                QualityGraphValidator.PROFILE_PORTS
        );
    }

    private static QualityNode valueNode(String id, Map<String, QualityInput> inputs) {
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
        nodes.add(profileNode("n0", Map.of()));
        for (int index = 1; index < count; index++) {
            nodes.add(valueNode("n" + index, Map.of()));
        }
        return new QualityGraph(
                ResourceId.parse("test:sized"),
                nodes,
                Map.of("profile", new OutputReference("n0", "summary"))
        );
    }
}
