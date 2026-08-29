package com.djden.alcoholic.domain.quality;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.domain.beverage.OutputReference;
import com.djden.alcoholic.domain.process.QualityProfile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BuiltinQualityGraphs {
    public static final ResourceId WINE = ResourceId.parse("alcoholic:wine");
    public static final ResourceId BEER = ResourceId.parse("alcoholic:beer");
    public static final ResourceId GENERIC = ResourceId.parse("alcoholic:generic");
    public static final ResourceId SPIRIT = ResourceId.parse("alcoholic:spirit");

    private BuiltinQualityGraphs() {
    }

    public static QualityGraph wine() {
        return graph(
                WINE,
                harvest(),
                closeness("nuance"),
                tanninComplexity(),
                colorComplexity(),
                wood(),
                oxygen(),
                wineBalance(),
                maturity(),
                stress(),
                caps(),
                fold(
                        List.of("harvest", "nuance", "tannin_c", "color_c", "wood", "oxygen"),
                        "balance"
                )
        );
    }

    public static QualityGraph beer() {
        return graph(
                BEER,
                harvest(),
                hopComplexity(),
                colorComplexity(),
                wood(),
                oxygen(),
                beerBalance(),
                maturity(),
                stress(),
                caps(),
                fold(
                        List.of("harvest", "hop", "color_c", "wood", "oxygen"),
                        "balance"
                )
        );
    }

    public static QualityGraph generic() {
        return graph(
                GENERIC,
                harvest(),
                closeness("nuance"),
                hopComplexity(),
                tanninComplexity(),
                colorComplexity(),
                wood(),
                oxygen(),
                genericBalance(),
                maturity(),
                stress(),
                caps(),
                fold(
                        List.of("harvest", "nuance", "hop", "tannin_c", "color_c", "wood", "oxygen"),
                        "balance"
                )
        );
    }

    public static QualityGraph spirit() {
        return graph(
                SPIRIT,
                harvest(),
                wood(),
                oxygen(),
                maturity(),
                stress(),
                caps(),
                fold(List.of("harvest", "wood", "oxygen"), null)
        );
    }

    public static Map<ResourceId, QualityGraph> all() {
        Map<ResourceId, QualityGraph> graphs = new LinkedHashMap<>();
        graphs.put(WINE, wine());
        graphs.put(BEER, beer());
        graphs.put(GENERIC, generic());
        graphs.put(SPIRIT, spirit());
        return Map.copyOf(graphs);
    }

    private static QualityGraph graph(ResourceId id, QualityNode... nodes) {
        return new QualityGraph(
                id,
                List.of(nodes),
                Map.of("profile", new OutputReference("fold", "summary"))
        );
    }

    private static QualityNode harvest() {
        return node("harvest", BuiltinQualityOperators.HARVEST_COMPLEXITY, DataNode.object(Map.of()));
    }

    private static QualityNode closeness(String id) {
        return node(id, BuiltinQualityOperators.WEIGHTED_PRESENT, DataNode.objectBuilder()
                .put("mode", DataNode.string("closeness"))
                .put("left", DataNode.string(QualityProfile.ACIDITY.toString()))
                .put("right", DataNode.string(QualityProfile.SUGAR.toString()))
                .put("weight", DataNode.number(0.15))
                .build());
    }

    private static QualityNode hopComplexity() {
        return node("hop", BuiltinQualityOperators.WEIGHTED_PRESENT, DataNode.objectBuilder()
                .put("mode", DataNode.string("sum"))
                .put("weights", DataNode.objectBuilder()
                        .put(QualityProfile.AROMA.toString(), DataNode.number(0.35))
                        .put(QualityProfile.BITTERNESS.toString(), DataNode.number(0.20))
                        .build())
                .build());
    }

    private static QualityNode tanninComplexity() {
        return node("tannin_c", BuiltinQualityOperators.WOOD_SWEET_SPOT, DataNode.objectBuilder()
                .put("property", DataNode.string(QualityProfile.TANNIN.toString()))
                .put("weight", DataNode.number(0.45))
                .build());
    }

    private static QualityNode colorComplexity() {
        return node("color_c", BuiltinQualityOperators.WEIGHTED_PRESENT, DataNode.objectBuilder()
                .put("mode", DataNode.string("sum"))
                .put("weights", DataNode.objectBuilder()
                        .put(QualityProfile.COLOR.toString(), DataNode.number(0.10))
                        .build())
                .build());
    }

    private static QualityNode wood() {
        return node("wood", BuiltinQualityOperators.WOOD_SWEET_SPOT, DataNode.object(Map.of()));
    }

    private static QualityNode oxygen() {
        return new QualityNode(
                "oxygen",
                BuiltinQualityOperators.OXYGEN_CURVE,
                DataNode.object(Map.of()),
                Map.of(),
                List.of("complexity", "defects")
        );
    }

    private static QualityNode wineBalance() {
        return node("balance", BuiltinQualityOperators.DISTANCE_BALANCE, balance(
                group(List.of(QualityProfile.SUGAR, QualityProfile.ACIDITY), Map.of(
                        QualityProfile.SUGAR, 0.35,
                        QualityProfile.ACIDITY, 0.45
                )),
                group(List.of(QualityProfile.TANNIN), Map.of(QualityProfile.TANNIN, 0.45))
        ));
    }

    private static QualityNode beerBalance() {
        return node("balance", BuiltinQualityOperators.DISTANCE_BALANCE, balance(
                group(List.of(QualityProfile.AROMA, QualityProfile.BITTERNESS), Map.of(
                        QualityProfile.BITTERNESS, 0.40
                )),
                group(List.of(QualityProfile.CARBONATION), Map.of(QualityProfile.CARBONATION, 0.35))
        ));
    }

    private static QualityNode genericBalance() {
        return node("balance", BuiltinQualityOperators.DISTANCE_BALANCE, balance(
                group(List.of(QualityProfile.SUGAR, QualityProfile.ACIDITY), Map.of(
                        QualityProfile.SUGAR, 0.35,
                        QualityProfile.ACIDITY, 0.45
                )),
                group(List.of(QualityProfile.AROMA, QualityProfile.BITTERNESS), Map.of(
                        QualityProfile.BITTERNESS, 0.40
                )),
                group(List.of(QualityProfile.CARBONATION), Map.of(QualityProfile.CARBONATION, 0.35)),
                group(List.of(QualityProfile.TANNIN), Map.of(QualityProfile.TANNIN, 0.45))
        ));
    }

    private static QualityNode maturity() {
        return node("maturity", BuiltinQualityOperators.AGING_MATURITY, DataNode.object(Map.of()));
    }

    private static QualityNode stress() {
        return node("stress", BuiltinQualityOperators.STRESS, DataNode.object(Map.of()));
    }

    private static QualityNode caps() {
        return new QualityNode(
                "caps",
                BuiltinQualityOperators.CAP_FLOOR,
                DataNode.object(Map.of()),
                Map.of(),
                List.of("cap", "floor")
        );
    }

    private static QualityNode fold(List<String> complexity, String balanceNode) {
        Map<String, QualityInput> inputs = new LinkedHashMap<>();
        inputs.put("complexity", sum(complexity));
        if (balanceNode != null) {
            inputs.put("balance", new QualityInput.NodePort(balanceNode));
        }
        inputs.put("maturity", new QualityInput.NodePort("maturity"));
        inputs.put("defects", new QualityInput.Sum(List.of(
                new QualityInput.NodePort("stress"),
                new QualityInput.NodePort("oxygen", "defects"),
                new QualityInput.NodePort("caps", "floor")
        )));
        inputs.put("caps", new QualityInput.NodePort("caps", "cap"));
        return new QualityNode(
                "fold",
                BuiltinQualityOperators.FOLD_SUMMARY,
                DataNode.object(Map.of()),
                inputs,
                List.of("purity", "complexity", "maturity", "balance", "defects", "summary")
        );
    }

    private static QualityInput sum(List<String> nodes) {
        List<QualityInput.NodePort> sources = new ArrayList<>();
        for (String id : nodes) {
            sources.add(new QualityInput.NodePort(id));
        }
        return new QualityInput.Sum(sources);
    }

    private static QualityNode node(String id, ResourceId operator, DataNode config) {
        return new QualityNode(id, operator, config, Map.of(), List.of("value"));
    }

    private static DataNode balance(DataNode... groups) {
        return DataNode.objectBuilder().put("groups", DataNode.list(List.of(groups))).build();
    }

    private static DataNode group(List<ResourceId> present, Map<ResourceId, Double> targets) {
        DataNode.ObjectBuilder targetNode = DataNode.objectBuilder();
        targets.forEach((id, value) -> targetNode.put(id.toString(), DataNode.number(value)));
        List<DataNode> presentNodes = new ArrayList<>();
        present.forEach(id -> presentNodes.add(DataNode.string(id.toString())));
        return DataNode.objectBuilder()
                .put("present", DataNode.list(presentNodes))
                .put("targets", targetNode.build())
                .build();
    }
}
