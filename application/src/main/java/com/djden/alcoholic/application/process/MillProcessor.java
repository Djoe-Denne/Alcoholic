package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ingredient.SolidInputView;
import com.djden.alcoholic.api.process.ItemOutput;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessHandler;
import com.djden.alcoholic.api.process.ProcessRequest;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.domain.liquid.PropertyBag;

import java.util.List;
import java.util.Objects;

public final class MillProcessor implements ProcessHandler<MillConfig> {
    @Override
    public ProcessResult apply(ProcessRequest request, MillConfig config, ProcessContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(context, "context");
        if (!config.executable()) {
            return ProcessResult.rejected("mill config is missing input or output");
        }
        List<SolidInputView> solids = request.solidsOn("malt", "grain", "input", "solid");
        int available = solids.stream().mapToInt(SolidInputView::count).sum();
        if (available < config.inputAmount()) {
            return ProcessResult.rejected("insufficient solid input for mill");
        }
        int units = Math.min(
                available / config.inputAmount(),
                context.executorModifiers().maxBatchUnits()
        );
        if (units < 1) {
            return ProcessResult.rejected("insufficient solid input for mill");
        }
        double yield = context.executorModifiers().yieldModifier();
        int amount = Math.max(1, (int) Math.round(config.outputAmount() * units * yield));
        PropertyBag transferred = AgriculturalTransfer.combine(solids);
        return ProcessResult.success(
                List.of(),
                List.of(new ItemOutput(config.outputItem().orElseThrow(), amount, transferred.asMap()))
        );
    }
}
