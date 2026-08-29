package com.djden.alcoholic.api.quality;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;

import java.util.List;
import java.util.Objects;

/**
 * A registered quality-graph primitive. Java addons register operators;
 * datapacks wire them into a DAG.
 */
@PublicApi
public interface QualityOperator<C> {
    ResourceId id();

    DataCodec<C> configCodec();

    QualitySignal evaluate(QualityEvaluationContext context, C config);

    default List<String> defaultOutputs() {
        return List.of("value");
    }

    @SuppressWarnings("unchecked")
    default QualitySignal evaluateDecoded(QualityEvaluationContext context, Object config) {
        return evaluate(context, (C) config);
    }

    static <C> QualityOperator<C> of(ResourceId id, DataCodec<C> configCodec, QualityHandler<C> handler) {
        return of(id, configCodec, handler, List.of("value"));
    }

    static <C> QualityOperator<C> of(
            ResourceId id,
            DataCodec<C> configCodec,
            QualityHandler<C> handler,
            List<String> defaultOutputs
    ) {
        return new RegisteredQualityOperator<>(id, configCodec, handler, defaultOutputs);
    }

    record RegisteredQualityOperator<C>(
            ResourceId id,
            DataCodec<C> configCodec,
            QualityHandler<C> handler,
            List<String> defaultOutputs
    ) implements QualityOperator<C> {
        public RegisteredQualityOperator {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(configCodec, "configCodec");
            Objects.requireNonNull(handler, "handler");
            defaultOutputs = List.copyOf(Objects.requireNonNull(defaultOutputs, "defaultOutputs"));
        }

        @Override
        public QualitySignal evaluate(QualityEvaluationContext context, C config) {
            return handler.apply(context, config);
        }
    }
}
