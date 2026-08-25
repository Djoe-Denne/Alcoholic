package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.liquid.LiquidBatchView;
import com.djden.alcoholic.api.process.ItemOutput;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessHandler;
import com.djden.alcoholic.api.process.ProcessRequest;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.domain.liquid.BatchSplitResult;
import com.djden.alcoholic.domain.liquid.LiquidBatch;

import java.util.List;
import java.util.Objects;

public final class BottleProcessor implements ProcessHandler<BottleConfig> {
    @Override
    public ProcessResult apply(ProcessRequest request, BottleConfig config, ProcessContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(config, "config");
        LiquidBatchView view = request.liquids().values().stream().findFirst().orElse(null);
        if (!(view instanceof LiquidBatch batch) || batch.volume() < config.volumeMillibuckets()) {
            return ProcessResult.rejected("bottle requires a liquid batch of sufficient volume");
        }
        BatchSplitResult split = batch.split(config.volumeMillibuckets());
        return ProcessResult.success(
                List.of(split.remaining()),
                List.of(new ItemOutput(config.bottleItem(), 1))
        );
    }
}
