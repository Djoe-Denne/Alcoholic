package com.djden.alcoholic.api.registry;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.quality.QualityOperator;

@PublicApi
public interface QualityOperatorRegistrar extends RegistryView<QualityOperator<?>> {
    <C> QualityOperator<C> register(QualityOperator<C> operator);
}
