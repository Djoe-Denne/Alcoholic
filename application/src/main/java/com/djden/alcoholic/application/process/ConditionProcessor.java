package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.liquid.LiquidBatchView;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessHandler;
import com.djden.alcoholic.api.process.ProcessRequest;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.process.QualityProfile;

import java.util.Objects;

public final class ConditionProcessor implements ProcessHandler<ConditionConfig> {
    @Override
    public ProcessResult apply(ProcessRequest request, ConditionConfig config, ProcessContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(context, "context");
        if (!config.executable()) {
            return ProcessResult.rejected("condition config is missing input or output liquid");
        }
        LiquidBatchView view = request.liquids().values().stream().findFirst().orElse(null);
        if (!(view instanceof LiquidBatch batch)) {
            return ProcessResult.rejected("condition requires a liquid batch");
        }
        boolean alreadyOutput = config.outputLiquid()
                .map(output -> batch.baseLiquid().filter(output::equals).isPresent())
                .orElse(false);
        double maturity = batch.number(config.maturityProperty(), 0.0);
        if (alreadyOutput && maturity + 1e-9 >= config.kinetics().completionThreshold()) {
            return ProcessResult.success(batch);
        }
        if (config.inputLiquid().isPresent()
                && batch.baseLiquid().filter(id -> id.equals(config.inputLiquid().orElseThrow())).isEmpty()
                && !alreadyOutput) {
            return ProcessResult.rejected("liquid is not accepted by this condition definition");
        }
        double rate = config.temperature().rateFactor(context.temperatureCelsius());
        if (rate <= 0.0) {
            return ProcessResult.rejected("temperature is outside the conditioning operating range");
        }
        double stability = context.executorModifiers().thermalStability();
        double advance = context.executorModifiers().scaleDelta(context.deltaTicks())
                * rate
                * config.kinetics().maturityPerTick()
                * Math.min(2.0, stability / 3.0 + 0.67);
        double nextMaturity = Math.min(1.0, maturity + advance);
        LiquidBatch next = batch.withProperty(config.maturityProperty(), nextMaturity);
        if (context.yeastPresent() && config.kinetics().carbonationFromSugar() > 0.0) {
            double sugar = batch.number(config.sugarProperty(), 0.0);
            double carbonation = batch.number(config.carbonationProperty(), 0.0);
            double added = Math.max(0.0, sugar) * config.kinetics().carbonationFromSugar()
                    * Math.max(0.0, nextMaturity - maturity);
            next = next.withProperty(config.carbonationProperty(), carbonation + added);
        }
        if (nextMaturity + 1e-9 >= config.kinetics().completionThreshold()) {
            next = next.withBaseLiquid(config.outputLiquid().orElseThrow());
        }
        return ProcessResult.success(QualityProfile.stampCap(next, context.executorModifiers()));
    }
}
