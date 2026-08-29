package com.djden.alcoholic.application.quality;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.liquid.LiquidBatchView;
import com.djden.alcoholic.api.process.ExecutorModifiers;
import com.djden.alcoholic.api.quality.QualityOperator;
import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.domain.process.QualityProfile;
import com.djden.alcoholic.domain.quality.QualityEvaluator;
import com.djden.alcoholic.domain.quality.QualityGraph;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class QualityProfiles {
    private QualityProfiles() {
    }

    public static QualityProfile derive(
            LiquidBatchView batch,
            BeverageCatalog catalog,
            AlcoholicApi api
    ) {
        return derive(batch, catalog, api, ExecutorModifiers.identity());
    }

    public static QualityProfile derive(
            LiquidBatchView batch,
            BeverageCatalog catalog,
            AlcoholicApi api,
            ExecutorModifiers modifiers
    ) {
        Objects.requireNonNull(batch, "batch");
        Objects.requireNonNull(catalog, "catalog");
        Objects.requireNonNull(api, "api");
        if (api.qualityOperators().ids().isEmpty()) {
            throw new IllegalStateException("quality operators are not registered");
        }
        QualityGraph graph = QualityResolver.resolve(batch, catalog);
        return QualityEvaluator.evaluate(graph, operators(api), batch, modifiers);
    }

    private static Map<ResourceId, QualityOperator<?>> operators(AlcoholicApi api) {
        Map<ResourceId, QualityOperator<?>> operators = new LinkedHashMap<>();
        api.qualityOperators().values().forEach(operator -> operators.put(operator.id(), operator));
        return operators;
    }
}
