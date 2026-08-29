package com.djden.alcoholic.domain.quality;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public sealed interface QualityInput {
    record NodePort(String nodeId, String port) implements QualityInput {
        public NodePort {
            Objects.requireNonNull(nodeId, "nodeId");
            Objects.requireNonNull(port, "port");
            if (nodeId.isBlank()) {
                throw new IllegalArgumentException("nodeId is blank");
            }
            if (port.isBlank()) {
                throw new IllegalArgumentException("port is blank");
            }
        }

        public NodePort(String nodeId) {
            this(nodeId, "value");
        }
    }

    record Sum(List<NodePort> sources) implements QualityInput {
        public Sum {
            sources = List.copyOf(new ArrayList<>(Objects.requireNonNull(sources, "sources")));
        }
    }
}
