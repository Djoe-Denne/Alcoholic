package com.djden.alcoholic.api.quality;

import com.djden.alcoholic.api.PublicApi;

@FunctionalInterface
@PublicApi
public interface QualityHandler<C> {
    QualitySignal apply(QualityEvaluationContext context, C config);
}
