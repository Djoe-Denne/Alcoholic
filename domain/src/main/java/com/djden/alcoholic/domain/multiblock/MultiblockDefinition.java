package com.djden.alcoholic.domain.multiblock;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ExecutorModifiers;

import java.util.Objects;
import java.util.Optional;

/**
 * Authoritative machine family. Beverage names never appear here.
 */
public record MultiblockDefinition(
        ResourceId id,
        MachineKind kind,
        Optional<ResourceId> processType,
        MultiblockConstraints constraints,
        int capacityPerInternalBlock,
        ExecutorModifiers modifiers,
        KineticRequirement kinetic,
        String controllerBlockId,
        MachineScale scale
) {
    public MultiblockDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(kind, "kind");
        processType = processType == null ? Optional.empty() : processType;
        Objects.requireNonNull(constraints, "constraints");
        if (capacityPerInternalBlock < 1) {
            throw new IllegalArgumentException("capacityPerInternalBlock must be >= 1");
        }
        modifiers = modifiers == null ? ExecutorModifiers.identity() : modifiers;
        kinetic = kinetic == null ? KineticRequirement.none() : kinetic;
        controllerBlockId = controllerBlockId == null ? "" : controllerBlockId;
        scale = scale == null ? MachineScale.INDUSTRIAL : scale;
    }

    public MultiblockDefinition(
            ResourceId id,
            MachineKind kind,
            Optional<ResourceId> processType,
            MultiblockConstraints constraints,
            int capacityPerInternalBlock,
            ExecutorModifiers modifiers,
            KineticRequirement kinetic,
            String controllerBlockId
    ) {
        this(
                id,
                kind,
                processType,
                constraints,
                capacityPerInternalBlock,
                modifiers,
                kinetic,
                controllerBlockId,
                MachineScale.INDUSTRIAL
        );
    }

    public boolean hasProcess() {
        return processType.isPresent();
    }
}
