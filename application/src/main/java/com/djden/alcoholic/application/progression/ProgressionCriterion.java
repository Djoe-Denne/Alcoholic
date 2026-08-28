package com.djden.alcoholic.application.progression;

import com.djden.alcoholic.api.ResourceId;

import java.util.Objects;
import java.util.Optional;

public final class ProgressionCriterion {
    private final String name;
    private final ResourceId item;
    private final ResourceId crop;
    private final ResourceId process;
    private final ResourceId liquid;
    private final ResourceId machine;

    private ProgressionCriterion(
            String name,
            ResourceId item,
            ResourceId crop,
            ResourceId process,
            ResourceId liquid,
            ResourceId machine
    ) {
        this.name = Objects.requireNonNull(name, "name");
        this.item = item;
        this.crop = crop;
        this.process = process;
        this.liquid = liquid;
        this.machine = machine;
    }

    public static ProgressionCriterion inventory(String name, ResourceId item) {
        return new ProgressionCriterion(name, Objects.requireNonNull(item, "item"), null, null, null, null);
    }

    public static ProgressionCriterion harvest(String name, ResourceId crop) {
        return new ProgressionCriterion(name, null, Objects.requireNonNull(crop, "crop"), null, null, null);
    }

    public static ProgressionCriterion process(String name, ResourceId process) {
        return process(name, process, null);
    }

    public static ProgressionCriterion process(String name, ResourceId process, ResourceId liquid) {
        return new ProgressionCriterion(
                name,
                null,
                null,
                Objects.requireNonNull(process, "process"),
                liquid,
                null
        );
    }

    public static ProgressionCriterion formed(String name, ResourceId machine) {
        return new ProgressionCriterion(name, null, null, null, null, Objects.requireNonNull(machine, "machine"));
    }

    public String name() {
        return name;
    }

    public Optional<ResourceId> item() {
        return Optional.ofNullable(item);
    }

    public Optional<ResourceId> crop() {
        return Optional.ofNullable(crop);
    }

    public Optional<ResourceId> process() {
        return Optional.ofNullable(process);
    }

    public Optional<ResourceId> liquid() {
        return Optional.ofNullable(liquid);
    }

    public Optional<ResourceId> machine() {
        return Optional.ofNullable(machine);
    }
}
