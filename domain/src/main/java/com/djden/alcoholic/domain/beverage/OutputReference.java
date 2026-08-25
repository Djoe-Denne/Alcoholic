package com.djden.alcoholic.domain.beverage;

import java.util.Objects;

public record OutputReference(String nodeId, String port) {
    public OutputReference {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(port, "port");
        if (nodeId.isBlank()) {
            throw new IllegalArgumentException("nodeId is blank");
        }
        if (port.isBlank()) {
            throw new IllegalArgumentException("port is blank");
        }
    }
}
