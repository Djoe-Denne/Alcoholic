package com.djden.alcoholic.api.process;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;

import java.util.Map;
import java.util.Objects;

/**
 * A process-type invocation independent of any beverage identity.
 */
@PublicApi
public record ProcessInvocation(
        ResourceId processType,
        DataNode config,
        String nodeId
) {
    public ProcessInvocation {
        Objects.requireNonNull(processType, "processType");
        config = config == null ? DataNode.object(Map.of()) : config;
        Objects.requireNonNull(nodeId, "nodeId");
        if (nodeId.isBlank()) {
            throw new IllegalArgumentException("nodeId is blank");
        }
    }
}
