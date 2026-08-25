package com.djden.alcoholic.api.process;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;

import java.util.Objects;

@PublicApi
public interface ProcessType<C> {
    ResourceId id();

    DataCodec<C> configCodec();

    ProcessResult apply(ProcessRequest request, C config, ProcessContext context);

    default boolean acceptsLiquid(C config, ResourceId liquid) {
        if (config instanceof LiquidAccepting accepting) {
            return accepting.acceptsLiquid(liquid);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    default boolean acceptsDecodedLiquid(Object config, ResourceId liquid) {
        return acceptsLiquid((C) config, liquid);
    }

    static <C> ProcessType<C> of(ResourceId id, DataCodec<C> configCodec, ProcessHandler<C> handler) {
        return new RegisteredProcessType<>(id, configCodec, handler);
    }

    record RegisteredProcessType<C>(
            ResourceId id,
            DataCodec<C> configCodec,
            ProcessHandler<C> handler
    ) implements ProcessType<C> {
        public RegisteredProcessType {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(configCodec, "configCodec");
            Objects.requireNonNull(handler, "handler");
        }

        @Override
        public ProcessResult apply(ProcessRequest request, C config, ProcessContext context) {
            return handler.apply(request, config, context);
        }
    }
}
