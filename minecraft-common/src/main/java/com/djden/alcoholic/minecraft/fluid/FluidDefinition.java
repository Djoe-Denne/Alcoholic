package com.djden.alcoholic.minecraft.fluid;

import com.djden.alcoholic.api.ResourceId;

import java.util.Objects;

/**
 * Complete loader-neutral definition of a built-in world fluid.
 */
public record FluidDefinition(
        ResourceId id,
        ResourceId stillTexture,
        ResourceId flowingTexture,
        int tintArgb,
        FluidFlowProfile flowProfile
) {
    public FluidDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(stillTexture, "stillTexture");
        Objects.requireNonNull(flowingTexture, "flowingTexture");
        Objects.requireNonNull(flowProfile, "flowProfile");
    }
}
