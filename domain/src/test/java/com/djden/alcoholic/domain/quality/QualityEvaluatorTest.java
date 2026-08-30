package com.djden.alcoholic.domain.quality;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.process.ExecutorModifiers;
import com.djden.alcoholic.domain.beverage.OutputReference;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.domain.process.QualityProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QualityEvaluatorTest {
    private static final ResourceId MUST = ResourceId.parse("alcoholic:red_grape_must");

    @Test
    void evaluatesNodesInTopologicalOrder() {
        QualityGraph graph = new QualityGraph(
                ResourceId.parse("test:topo"),
                List.of(
                        fold("fold", Map.of("complexity", new QualityInput.NodePort("harvest"))),
                        harvest("harvest")
                ),
                Map.of("profile", new OutputReference("fold", "summary"))
        );
        QualityProfile profile = QualityEvaluator.evaluate(
                graph,
                BuiltinQualityOperators.map(),
                batch(0.70),
                ExecutorModifiers.identity()
        );
        assertEquals(0.70, profile.complexity(), 1e-9);
        assertTrue(profile.summary() > 0.0);
    }

    @Test
    void unknownOperatorThrows() {
        QualityGraph graph = new QualityGraph(
                ResourceId.parse("test:missing"),
                List.of(new QualityNode(
                        "x",
                        ResourceId.parse("test:missing"),
                        DataNode.object(Map.of()),
                        Map.of(),
                        QualityGraphValidator.PROFILE_PORTS
                )),
                Map.of("profile", new OutputReference("x", "summary"))
        );
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> QualityEvaluator.evaluate(
                        graph,
                        BuiltinQualityOperators.map(),
                        batch(0.50),
                        ExecutorModifiers.identity()
                )
        );
        assertTrue(thrown.getMessage().contains("unknown quality operator"));
    }

    @Test
    void sumsUpstreamValues() {
        QualityGraph graph = new QualityGraph(
                ResourceId.parse("test:sum"),
                List.of(
                        harvest("left"),
                        harvest("right"),
                        fold("fold", Map.of(
                                "complexity",
                                new QualityInput.Sum(List.of(
                                        new QualityInput.NodePort("left"),
                                        new QualityInput.NodePort("right")
                                ))
                        ))
                ),
                Map.of("profile", new OutputReference("fold", "summary"))
        );
        QualityProfile profile = QualityEvaluator.evaluate(
                graph,
                BuiltinQualityOperators.map(),
                batch(0.20),
                ExecutorModifiers.identity()
        );
        assertEquals(0.40, profile.complexity(), 1e-9);
    }

    @Test
    void profileReadsAllSixPortsFromTheFoldNode() {
        QualityGraph graph = new QualityGraph(
                ResourceId.parse("test:bag"),
                List.of(fold("fold", Map.of())),
                Map.of("profile", new OutputReference("fold", "summary"))
        );
        QualityProfile profile = QualityEvaluator.evaluate(
                graph,
                BuiltinQualityOperators.map(),
                batch(0.10),
                ExecutorModifiers.identity()
        );
        assertEquals(1.0, profile.purity(), 1e-9);
        assertEquals(0.0, profile.complexity(), 1e-9);
        assertEquals(0.0, profile.maturity(), 1e-9);
        assertEquals(0.5, profile.balance(), 1e-9);
        assertEquals(0.0, profile.defects(), 1e-9);
        assertTrue(profile.summary() > 0.0);
    }

    @Test
    void pickFallsBackToValueWhenRequestedPortIsMissing() {
        QualityGraph graph = new QualityGraph(
                ResourceId.parse("test:fallback"),
                List.of(
                        harvest("harvest"),
                        fold("fold", Map.of(
                                "complexity",
                                new QualityInput.NodePort("harvest", "complexity")
                        ))
                ),
                Map.of("profile", new OutputReference("fold", "summary"))
        );
        QualityProfile profile = QualityEvaluator.evaluate(
                graph,
                BuiltinQualityOperators.map(),
                batch(0.55),
                ExecutorModifiers.identity()
        );
        assertEquals(0.55, profile.complexity(), 1e-9);
    }

    private static QualityNode harvest(String id) {
        return new QualityNode(
                id,
                BuiltinQualityOperators.HARVEST_COMPLEXITY,
                DataNode.object(Map.of()),
                Map.of(),
                List.of("value")
        );
    }

    private static QualityNode fold(String id, Map<String, QualityInput> inputs) {
        return new QualityNode(
                id,
                BuiltinQualityOperators.FOLD_SUMMARY,
                DataNode.object(Map.of()),
                inputs,
                QualityGraphValidator.PROFILE_PORTS
        );
    }

    private static LiquidBatch batch(double harvest) {
        return LiquidBatch.of(
                MUST,
                1000,
                PropertyBag.empty().with(QualityProfile.HARVEST_QUALITY, harvest)
        );
    }
}
