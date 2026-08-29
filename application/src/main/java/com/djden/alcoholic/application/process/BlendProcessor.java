package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.liquid.LiquidBatchView;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessHandler;
import com.djden.alcoholic.api.process.ProcessRequest;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.api.property.PropertyAggregator;
import com.djden.alcoholic.api.property.PropertyMerge;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.process.QualityProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class BlendProcessor implements ProcessHandler<BlendConfig> {
    private final Function<ResourceId, PropertyMerge> strategies;
    private final Function<ResourceId, PropertyAggregator> aggregators;

    public BlendProcessor() {
        this(id -> PropertyMerge.WEIGHTED_AVERAGE, id -> PropertyAggregator.forStrategy(PropertyMerge.WEIGHTED_AVERAGE));
    }

    public BlendProcessor(
            Function<ResourceId, PropertyMerge> strategies,
            Function<ResourceId, PropertyAggregator> aggregators
    ) {
        this.strategies = Objects.requireNonNull(strategies, "strategies");
        this.aggregators = Objects.requireNonNull(aggregators, "aggregators");
    }

    @Override
    public ProcessResult apply(ProcessRequest request, BlendConfig config, ProcessContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(config, "config");
        List<LiquidBatch> batches = new ArrayList<>();
        for (LiquidBatchView view : request.liquids().values()) {
            if (view instanceof LiquidBatch batch && batch.volume() > 0.0) {
                batches.add(batch);
            }
        }
        if (batches.size() < config.minInputs()) {
            return ProcessResult.rejected("blend requires at least " + config.minInputs() + " liquid inputs");
        }
        for (LiquidBatch batch : batches) {
            ResourceId definition = batch.baseLiquid().orElse(null);
            if (definition == null || !config.acceptsLiquid(definition)) {
                return ProcessResult.rejected("blend input is not accepted");
            }
        }
        double total = batches.stream().mapToDouble(LiquidBatch::volume).sum();
        if (config.minFraction().isPresent()) {
            for (LiquidBatch batch : batches) {
                if (batch.volume() / total < config.minFraction().orElseThrow()) {
                    return ProcessResult.rejected("blend proportion is below the configured minimum");
                }
            }
        }
        ResourceId output = config.outputLiquid().orElse(null);
        if (output == null) {
            return ProcessResult.rejected("blend config is missing output liquid");
        }
        LiquidBatch acc = batches.get(0);
        for (int index = 1; index < batches.size(); index++) {
            var blended = acc.blend(batches.get(index), output, strategies, aggregators);
            if (blended.isEmpty()) {
                return ProcessResult.rejected("blend inputs are not aggregable");
            }
            acc = blended.get();
        }
        return ProcessResult.success(QualityProfile.stampCap(acc, context.executorModifiers()));
    }
}
