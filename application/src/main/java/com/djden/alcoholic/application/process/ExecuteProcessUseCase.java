package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.process.ProcessContext;
import com.djden.alcoholic.api.process.ProcessExecutor;
import com.djden.alcoholic.api.process.ProcessInputs;
import com.djden.alcoholic.api.process.ProcessInvocation;
import com.djden.alcoholic.api.process.ProcessRequest;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.api.process.ProcessType;

import java.util.Objects;

/**
 * Application-facing process execution. Executors advertise capabilities;
 * process types own transformation.
 */
public final class ExecuteProcessUseCase {
    private final AlcoholicApi api;

    public ExecuteProcessUseCase(AlcoholicApi api) {
        this.api = Objects.requireNonNull(api, "api");
    }

    public boolean canExecute(
            ProcessExecutor executor,
            ProcessInvocation invocation,
            ProcessInputs inputs,
            ProcessContext context
    ) {
        Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(invocation, "invocation");
        Objects.requireNonNull(inputs, "inputs");
        Objects.requireNonNull(context, "context");
        return executor.canExecute(invocation, inputs, context)
                && api.processes().contains(invocation.processType());
    }

    public ProcessResult execute(
            ProcessExecutor executor,
            ProcessInvocation invocation,
            ProcessInputs inputs,
            ProcessContext context
    ) {
        if (!canExecute(executor, invocation, inputs, context)) {
            return ProcessResult.rejected(
                    "executor cannot run " + invocation.processType() + " at node " + invocation.nodeId()
            );
        }
        return apply(invocation, inputs, context);
    }

    @SuppressWarnings("unchecked")
    public ProcessResult apply(
            ProcessInvocation invocation,
            ProcessInputs inputs,
            ProcessContext context
    ) {
        Objects.requireNonNull(invocation, "invocation");
        ProcessType<Object> type = (ProcessType<Object>) api.processes()
                .get(invocation.processType())
                .orElse(null);
        if (type == null) {
            return ProcessResult.rejected("unknown process type " + invocation.processType());
        }
        Object config = type.configCodec().decode(invocation.config());
        return type.apply(ProcessRequest.of(inputs), config, context);
    }
}
