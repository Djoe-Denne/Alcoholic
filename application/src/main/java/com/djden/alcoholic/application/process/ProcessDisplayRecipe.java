package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ProcessDisplaySpec;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Catalog process plus its {@link ProcessDisplaySpec} for recipe viewers.
 */
public record ProcessDisplayRecipe(
        ResourceId id,
        ResourceId processType,
        ProcessDisplaySpec spec
) {
    public ProcessDisplayRecipe {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(processType, "processType");
        Objects.requireNonNull(spec, "spec");
    }

    public boolean visible() {
        return spec.visible();
    }

    public List<ProcessDisplaySpec.ItemPart> itemInputs() {
        return spec.itemInputs();
    }

    public List<ProcessDisplaySpec.FluidPart> fluidInputs() {
        return spec.fluidInputs();
    }

    public List<ProcessDisplaySpec.ItemPart> itemOutputs() {
        return spec.itemOutputs();
    }

    public List<ProcessDisplaySpec.FluidPart> fluidOutputs() {
        return spec.fluidOutputs();
    }

    public OptionalInt durationTicks() {
        return spec.durationTicks();
    }

    public Optional<ProcessDisplaySpec.TemperatureRange> preferredTemperature() {
        return spec.preferredTemperature();
    }
}
