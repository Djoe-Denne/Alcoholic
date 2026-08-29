package com.djden.alcoholic.application.quality;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.liquid.LiquidBatchView;
import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.domain.beverage.BeverageDefinition;
import com.djden.alcoholic.domain.quality.BuiltinQualityGraphs;
import com.djden.alcoholic.domain.quality.QualityGraph;

import java.util.Objects;
import java.util.Optional;

public final class QualityResolver {
    private QualityResolver() {
    }

    public static QualityGraph resolve(LiquidBatchView batch, BeverageCatalog catalog) {
        Objects.requireNonNull(batch, "batch");
        BeverageCatalog source = catalog == null ? BeverageCatalog.empty() : catalog;
        return batch.identity()
                .flatMap(identity -> source.beverage(identity.definitionId()))
                .flatMap(BeverageDefinition::quality)
                .flatMap(source::quality)
                .or(() -> batch.baseLiquid().flatMap(source::beverage).flatMap(BeverageDefinition::quality).flatMap(source::quality))
                .or(() -> batch.baseLiquid().flatMap(source::quality))
                .or(() -> Optional.ofNullable(source.qualityGraphs().get(BuiltinQualityGraphs.GENERIC)))
                .orElseGet(BuiltinQualityGraphs::generic);
    }

    public static Optional<QualityGraph> graph(BeverageCatalog catalog, ResourceId id) {
        if (catalog == null || id == null) {
            return Optional.empty();
        }
        return catalog.quality(id);
    }
}
