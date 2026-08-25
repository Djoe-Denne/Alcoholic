package com.djden.alcoholic.api.process;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.liquid.LiquidBatchView;

import java.util.List;
import java.util.Objects;

@PublicApi
public record ProcessResult(
        Status status,
        List<LiquidBatchView> outputs,
        List<ItemOutput> items,
        String message
) {
    public ProcessResult {
        Objects.requireNonNull(status, "status");
        outputs = List.copyOf(Objects.requireNonNull(outputs, "outputs"));
        items = List.copyOf(Objects.requireNonNull(items, "items"));
        Objects.requireNonNull(message, "message");
    }

    public ProcessResult(Status status, List<LiquidBatchView> outputs, String message) {
        this(status, outputs, List.of(), message);
    }

    public enum Status {
        SUCCESS,
        REJECTED,
        UNSUPPORTED
    }

    public boolean success() {
        return status == Status.SUCCESS;
    }

    public static ProcessResult unsupported(ResourceId processType) {
        return new ProcessResult(
                Status.UNSUPPORTED,
                List.of(),
                List.of(),
                "Process " + processType + " is registered but not executable yet"
        );
    }

    public static ProcessResult rejected(String message) {
        return new ProcessResult(Status.REJECTED, List.of(), List.of(), message);
    }

    public static ProcessResult success(LiquidBatchView output) {
        return success(List.of(output), List.of());
    }

    public static ProcessResult success(List<LiquidBatchView> outputs, List<ItemOutput> items) {
        return new ProcessResult(Status.SUCCESS, outputs, items, "");
    }
}
