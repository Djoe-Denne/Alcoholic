package com.djden.alcoholic.api.registry;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.quality.QualityHandler;
import com.djden.alcoholic.api.quality.QualityOperator;

@PublicApi
public interface QualityOperatorRegistrar extends RegistryView<QualityOperator<?>> {
    <C> QualityOperator<C> register(QualityOperator<C> operator);

    default <C> QualityOperator<C> register(ResourceId id, DataCodec<C> configCodec, QualityHandler<C> handler) {
        return register(QualityOperator.of(id, configCodec, handler));
    }
}
