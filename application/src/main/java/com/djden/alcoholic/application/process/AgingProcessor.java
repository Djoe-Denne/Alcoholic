package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.liquid.LiquidBatchView;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessHandler;
import com.djden.alcoholic.api.process.ProcessRequest;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.process.AgingPhysics;
import com.djden.alcoholic.domain.process.AgingState;
import com.djden.alcoholic.domain.process.QualityProfile;
import com.djden.alcoholic.domain.vessel.EnvironmentProfile;
import com.djden.alcoholic.domain.vessel.VesselProfile;

import java.util.Objects;

public final class AgingProcessor implements ProcessHandler<AgingConfig> {
    @Override
    public ProcessResult apply(ProcessRequest request, AgingConfig config, ProcessContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(context, "context");
        LiquidBatchView view = request.liquids().values().stream().findFirst().orElse(null);
        if (!(view instanceof LiquidBatch batch)) {
            return ProcessResult.rejected("age requires a liquid batch");
        }
        if (config.inputLiquid().isPresent()
                && batch.baseLiquid().filter(id -> id.equals(config.inputLiquid().orElseThrow())).isEmpty()) {
            return ProcessResult.rejected("liquid is not accepted by this age definition");
        }
        VesselProfile vessel = context.vessel()
                .filter(VesselProfile.class::isInstance)
                .map(VesselProfile.class::cast)
                .orElseGet(VesselProfile::oakBarrel);
        EnvironmentProfile environment = context.environment()
                .filter(EnvironmentProfile.class::isInstance)
                .map(EnvironmentProfile.class::cast)
                .orElseGet(() -> new EnvironmentProfile(context.temperatureCelsius(), 0.5, true));
        AgingState next = AgingPhysics.step(
                batch,
                config.kinetics(),
                config.temperature(),
                vessel,
                environment,
                config.maturityProperty(),
                config.woodProperty(),
                config.oxidationProperty(),
                config.outputLiquid(),
                context.executorModifiers().scaleDelta(context.deltaTicks())
        );
        return ProcessResult.success(QualityProfile.stampCap(next.batch(), context.executorModifiers()));
    }
}
