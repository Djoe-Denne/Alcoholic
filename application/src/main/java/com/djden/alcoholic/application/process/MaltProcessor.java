package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.ingredient.SolidInputView;
import com.djden.alcoholic.api.process.ItemOutput;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessHandler;
import com.djden.alcoholic.api.process.ProcessRequest;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.domain.liquid.PropertyBag;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MaltProcessor implements ProcessHandler<MaltConfig> {
    static final ResourceId COLOR = ResourceId.parse("alcoholic:color");
    static final ResourceId SUGAR = ResourceId.parse("alcoholic:sugar");
    static final ResourceId ROAST = ResourceId.parse("alcoholic:roast_intensity");

    @Override
    public ProcessResult apply(ProcessRequest request, MaltConfig config, ProcessContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(context, "context");
        if (!config.executable()) {
            return ProcessResult.rejected("malt config is missing input or output");
        }
        if (config.temperature().stalled(context.temperatureCelsius())) {
            return ProcessResult.rejected("temperature is outside the malting operating range");
        }
        double humidity = context.environment()
                .map(env -> env.humidity())
                .orElse(1.0);
        if (humidity + 1e-9 < config.moistureRequirement()) {
            return ProcessResult.rejected("humidity is below the malt moisture requirement");
        }
        List<SolidInputView> solids = request.solidsOn("grain", "input", "solid");
        int available = solids.stream().mapToInt(SolidInputView::count).sum();
        if (available < config.inputAmount()) {
            return ProcessResult.rejected("insufficient solid input for malt");
        }
        int units = Math.min(
                available / config.inputAmount(),
                context.executorModifiers().maxBatchUnits()
        );
        if (units < 1) {
            return ProcessResult.rejected("insufficient solid input for malt");
        }
        Map<ResourceId, Object> properties = new LinkedHashMap<>();
        properties.put(COLOR, config.kiln().colorPotential());
        properties.put(SUGAR, config.kiln().fermentablePotential());
        properties.put(ROAST, config.kiln().roastIntensity());
        PropertyBag agricultural = AgriculturalTransfer.combine(solids);
        agricultural.get(COLOR).ifPresent(value -> properties.put(COLOR, value));
        return ProcessResult.success(
                List.of(),
                List.of(new ItemOutput(
                        config.outputItem().orElseThrow(),
                        config.outputAmount() * units,
                        Map.copyOf(properties)
                ))
        );
    }
}
