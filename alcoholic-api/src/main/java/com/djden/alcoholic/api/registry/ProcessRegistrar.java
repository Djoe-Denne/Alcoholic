package com.djden.alcoholic.api.registry;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.process.ProcessHandler;
import com.djden.alcoholic.api.process.ProcessType;

@PublicApi
public interface ProcessRegistrar extends RegistryView<ProcessType<?>> {
    <C> ProcessType<C> register(ResourceId id, DataCodec<C> configCodec, ProcessHandler<C> handler);

    <C> ProcessType<C> register(ProcessType<C> type);
}
