package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.liquid.LiquidBatchView;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessHandler;
import com.djden.alcoholic.api.process.ProcessRequest;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.process.FermentationPhysics;
import com.djden.alcoholic.domain.process.FermentationState;
import com.djden.alcoholic.domain.process.QualityProfile;

import java.util.Objects;

public final class FermentProcessor implements ProcessHandler<FermentConfig> {
    @Override
    public ProcessResult apply(ProcessRequest request, FermentConfig config, ProcessContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(context, "context");
        LiquidBatchView view = request.liquids().values().stream().findFirst().orElse(null);
        if (!(view instanceof LiquidBatch batch)) {
            return ProcessResult.rejected("ferment requires a liquid batch");
        }
        boolean alreadyOutput = config.outputLiquid()
                .map(output -> batch.baseLiquid().filter(output::equals).isPresent())
                .orElse(false);
        boolean complete = batch.number(config.sugarProperty(), 1.0) <= config.kinetics().completionThreshold()
                && alreadyOutput;
        if (complete) {
            return ProcessResult.success(batch);
        }
        if (config.inputLiquid().isPresent()
                && batch.baseLiquid().filter(id -> id.equals(config.inputLiquid().orElseThrow())).isEmpty()) {
            return ProcessResult.rejected("liquid is not accepted by this ferment definition");
        }
        boolean yeast = context.yeastPresent() || !config.requireYeast();
        ResourceId output = config.outputLiquid().orElse(batch.baseLiquid().orElse(null));
        if (output == null) {
            return ProcessResult.rejected("ferment config is missing output liquid");
        }
        FermentationState state = new FermentationState(
                batch,
                yeast,
                false,
                0.0,
                batch.number(config.stressProperty(), 0.0)
        );
        FermentationState next = FermentationPhysics.step(
                state,
                config.kinetics(),
                config.temperature(),
                config.sugarProperty(),
                config.ethanolProperty(),
                config.stressProperty(),
                output,
                context.temperatureCelsius(),
                context.executorModifiers().scaleDelta(context.deltaTicks())
        );
        LiquidBatch result = next.batch();
        double stress = Math.max(
                result.batchProvenance().fermentationStress(),
                result.number(config.stressProperty(), 0.0)
        );
        result = result.withProvenance(result.batchProvenance().withSummaries(
                stress,
                result.batchProvenance().totalAgingTime(),
                result.batchProvenance().woodExposure(),
                result.batchProvenance().oxidationExposure()
        ));
        return ProcessResult.success(QualityProfile.stampCap(result, context.executorModifiers()));
    }
}
