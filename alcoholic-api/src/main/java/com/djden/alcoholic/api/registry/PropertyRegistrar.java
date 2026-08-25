package com.djden.alcoholic.api.registry;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.property.LiquidProperty;

@PublicApi
public interface PropertyRegistrar extends RegistryView<LiquidProperty<?>> {
    <T> LiquidProperty<T> register(ResourceId id, Class<T> valueType, DataCodec<T> codec);

    <T> LiquidProperty<T> register(LiquidProperty<T> property);
}
