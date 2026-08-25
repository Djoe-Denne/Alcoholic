package com.djden.alcoholic.api.process;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;

import java.util.Set;

/**
 * A machine or integration that can execute one or more process capabilities.
 *
 * <p>Executors advertise process types such as {@code alcoholic:press}. They
 * must not branch on a beverage identity or a concrete machine class from
 * beverage data.</p>
 */
@PublicApi
public interface ProcessExecutor {
    ResourceId supportedProcess();

    default Set<ResourceId> supportedProcesses() {
        return Set.of(supportedProcess());
    }

    default boolean supports(ResourceId processType) {
        return supportedProcesses().contains(processType);
    }

    default boolean supports(ProcessType<?> processType) {
        return supports(processType.id());
    }

    default boolean canExecute(
            ProcessInvocation invocation,
            ProcessInputs inputs,
            ProcessContext context
    ) {
        return supports(invocation.processType());
    }

    default ProcessResult execute(
            ProcessInvocation invocation,
            ProcessInputs inputs,
            ProcessContext context
    ) {
        if (!canExecute(invocation, inputs, context)) {
            return ProcessResult.rejected(
                    "Executor does not support " + invocation.processType()
            );
        }
        return ProcessResult.unsupported(invocation.processType());
    }
}
