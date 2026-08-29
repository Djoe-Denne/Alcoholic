package com.djden.alcoholic.domain.quality;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.liquid.LiquidBatchView;
import com.djden.alcoholic.api.process.ExecutorModifiers;
import com.djden.alcoholic.api.quality.QualityEvaluationContext;
import com.djden.alcoholic.api.quality.QualityOperator;
import com.djden.alcoholic.api.quality.QualitySignal;
import com.djden.alcoholic.api.registry.RegistryView;
import com.djden.alcoholic.domain.beverage.OutputReference;
import com.djden.alcoholic.domain.process.QualityProfile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class QualityEvaluator {
    private QualityEvaluator() {
    }

    public static QualityProfile evaluate(
            QualityGraph graph,
            LiquidBatchView batch,
            ExecutorModifiers modifiers
    ) {
        return evaluate(graph, BuiltinQualityOperators.map(), batch, modifiers);
    }

    public static QualityProfile evaluate(
            QualityGraph graph,
            RegistryView<QualityOperator<?>> operators,
            LiquidBatchView batch,
            ExecutorModifiers modifiers
    ) {
        Map<ResourceId, QualityOperator<?>> map = new LinkedHashMap<>();
        Objects.requireNonNull(operators, "operators").values().forEach(operator -> map.put(operator.id(), operator));
        return evaluate(graph, map, batch, modifiers);
    }

    public static QualityProfile evaluate(
            QualityGraph graph,
            Map<ResourceId, ? extends QualityOperator<?>> operators,
            LiquidBatchView batch,
            ExecutorModifiers modifiers
    ) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(operators, "operators");
        Objects.requireNonNull(batch, "batch");
        ExecutorModifiers scale = modifiers == null ? ExecutorModifiers.identity() : modifiers;
        Map<String, QualityNode> nodes = new LinkedHashMap<>();
        graph.nodes().forEach(node -> nodes.put(node.id(), node));
        Map<String, QualitySignal> evaluated = new LinkedHashMap<>();
        for (String id : topological(graph, nodes)) {
            QualityNode node = nodes.get(id);
            QualityOperator<?> operator = operators.get(node.operator());
            if (operator == null) {
                throw new IllegalArgumentException("unknown quality operator " + node.operator());
            }
            Object config = operator.configCodec().decode(node.config(), "quality/" + graph.id() + "/nodes/" + id);
            Map<String, QualitySignal> inputs = resolveInputs(node, evaluated);
            QualitySignal signal = operator.evaluateDecoded(new Context(batch, scale, inputs), config);
            evaluated.put(id, signal);
        }
        QualitySignal profile = profileSignal(graph, evaluated);
        return new QualityProfile(
                profile.get("purity", 0.0),
                profile.get("complexity", 0.0),
                profile.get("maturity", 0.0),
                profile.get("balance", 0.5),
                profile.get("defects", 0.0),
                profile.get("summary", 0.0)
        );
    }

    private static QualitySignal profileSignal(
            QualityGraph graph,
            Map<String, QualitySignal> evaluated
    ) {
        OutputReference output = graph.outputs().get("profile");
        if (output == null) {
            throw new IllegalArgumentException("quality graph " + graph.id() + " has no profile output");
        }
        QualitySignal signal = evaluated.get(output.nodeId());
        if (signal == null) {
            throw new IllegalArgumentException("unknown quality output node " + output.nodeId());
        }
        return signal;
    }

    private static Map<String, QualitySignal> resolveInputs(
            QualityNode node,
            Map<String, QualitySignal> evaluated
    ) {
        Map<String, QualitySignal> inputs = new LinkedHashMap<>();
        node.inputs().forEach((port, input) -> inputs.put(port, resolve(input, port, evaluated)));
        return inputs;
    }

    private static QualitySignal resolve(
            QualityInput input,
            String inputPort,
            Map<String, QualitySignal> evaluated
    ) {
        if (input instanceof QualityInput.NodePort reference) {
            QualitySignal source = require(evaluated, reference.nodeId());
            double extracted = pick(source, reference.port());
            return source.with("value", extracted).with(inputPort, extracted);
        }
        if (input instanceof QualityInput.Sum sum) {
            double total = 0.0;
            for (QualityInput.NodePort reference : sum.sources()) {
                total += pick(require(evaluated, reference.nodeId()), reference.port());
            }
            return QualitySignal.empty().with("value", total).with(inputPort, total);
        }
        return QualitySignal.empty();
    }

    private static QualitySignal require(Map<String, QualitySignal> evaluated, String nodeId) {
        QualitySignal signal = evaluated.get(nodeId);
        if (signal == null) {
            throw new IllegalArgumentException("quality node " + nodeId + " has not been evaluated");
        }
        return signal;
    }

    private static double pick(QualitySignal signal, String requestedPort) {
        return signal.get(requestedPort, 0.0);
    }

    private static List<String> topological(QualityGraph graph, Map<String, QualityNode> nodes) {
        Map<String, Set<String>> outgoing = new HashMap<>();
        Map<String, Integer> incoming = new HashMap<>();
        nodes.keySet().forEach(id -> {
            outgoing.put(id, new HashSet<>());
            incoming.put(id, 0);
        });
        for (QualityNode node : graph.nodes()) {
            for (QualityInput input : node.inputs().values()) {
                for (QualityInput.NodePort reference : sources(input)) {
                    if (nodes.containsKey(reference.nodeId())
                            && outgoing.get(reference.nodeId()).add(node.id())) {
                        incoming.merge(node.id(), 1, Integer::sum);
                    }
                }
            }
        }
        Deque<String> ready = new ArrayDeque<>();
        incoming.forEach((id, count) -> {
            if (count == 0) {
                ready.add(id);
            }
        });
        List<String> order = new ArrayList<>();
        while (!ready.isEmpty()) {
            String current = ready.removeFirst();
            order.add(current);
            for (String next : outgoing.get(current)) {
                int remaining = incoming.merge(next, -1, Integer::sum);
                if (remaining == 0) {
                    ready.add(next);
                }
            }
        }
        if (order.size() != nodes.size()) {
            throw new IllegalArgumentException("quality graph " + graph.id() + " contains a cycle");
        }
        return order;
    }

    private static List<QualityInput.NodePort> sources(QualityInput input) {
        if (input instanceof QualityInput.NodePort port) {
            return List.of(port);
        }
        if (input instanceof QualityInput.Sum sum) {
            return sum.sources();
        }
        return List.of();
    }

    private record Context(
            LiquidBatchView batch,
            ExecutorModifiers modifiers,
            Map<String, QualitySignal> inputs
    ) implements QualityEvaluationContext {
        @Override
        public QualitySignal inputSignal(String port) {
            return inputs.getOrDefault(port, QualitySignal.empty());
        }
    }
}
