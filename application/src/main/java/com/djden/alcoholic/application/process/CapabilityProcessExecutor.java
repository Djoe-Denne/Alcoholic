package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ProcessExecutor;

import java.util.Objects;
import java.util.Set;

/**
 * Capability-only executor. Transformation is delegated to {@link ExecuteProcessUseCase}.
 */
public final class CapabilityProcessExecutor implements ProcessExecutor {
    private final Set<ResourceId> capabilities;

    public CapabilityProcessExecutor(ResourceId processType) {
        this(Set.of(processType));
    }

    public CapabilityProcessExecutor(Set<ResourceId> capabilities) {
        this.capabilities = Set.copyOf(Objects.requireNonNull(capabilities, "capabilities"));
        if (this.capabilities.isEmpty()) {
            throw new IllegalArgumentException("capabilities must not be empty");
        }
    }

    @Override
    public ResourceId supportedProcess() {
        return capabilities.iterator().next();
    }

    @Override
    public Set<ResourceId> supportedProcesses() {
        return capabilities;
    }
}
