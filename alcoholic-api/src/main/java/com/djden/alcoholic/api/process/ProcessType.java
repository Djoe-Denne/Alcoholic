package com.djden.alcoholic.api.process;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodec;
import com.djden.alcoholic.api.ingredient.IngredientSelector;

import java.util.Objects;
import java.util.function.BiPredicate;

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

    default boolean acceptsSolid(C config, ResourceId item, BiPredicate<IngredientSelector, ResourceId> matcher) {
        if (config instanceof SolidAccepting accepting) {
            return accepting.inputSelector().filter(selector -> matcher.test(selector, item)).isPresent();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    default boolean acceptsDecodedSolid(Object config, ResourceId item, BiPredicate<IngredientSelector, ResourceId> matcher) {
        return acceptsSolid((C) config, item, matcher);
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
