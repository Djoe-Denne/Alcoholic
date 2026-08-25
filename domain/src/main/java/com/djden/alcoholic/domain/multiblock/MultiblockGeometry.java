package com.djden.alcoholic.domain.multiblock;

import java.util.List;
import java.util.Objects;

public record MultiblockGeometry(
        AxisBox bounds,
        int interiorVolume,
        int capacityMillibuckets,
        List<CellCoord> ports,
        CellCoord controller
) {
    public MultiblockGeometry {
        Objects.requireNonNull(bounds, "bounds");
        if (interiorVolume < 0) {
            throw new IllegalArgumentException("interiorVolume must be >= 0");
        }
        if (capacityMillibuckets < 0) {
            throw new IllegalArgumentException("capacityMillibuckets must be >= 0");
        }
        ports = ports == null ? List.of() : List.copyOf(ports);
        Objects.requireNonNull(controller, "controller");
    }
}
