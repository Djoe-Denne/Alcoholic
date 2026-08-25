package com.djden.alcoholic.domain.vessel;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.vessel.BarrelHistoryView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class BarrelHistory implements BarrelHistoryView {
    public static final int MAX_PREVIOUS = 4;

    private final int usageCount;
    private final List<ResourceId> previousContents;
    private final Optional<Integer> toastLevel;
    private final Optional<Integer> charLevel;
    private final Optional<Double> woodExtractionRemaining;

    public BarrelHistory(
            int usageCount,
            List<ResourceId> previousContents,
            Optional<Integer> toastLevel,
            Optional<Integer> charLevel,
            Optional<Double> woodExtractionRemaining
    ) {
        this.usageCount = Math.max(0, usageCount);
        List<ResourceId> copy = new ArrayList<>();
        if (previousContents != null) {
            previousContents.stream().filter(Objects::nonNull).forEach(copy::add);
        }
        if (copy.size() > MAX_PREVIOUS) {
            copy = new ArrayList<>(copy.subList(copy.size() - MAX_PREVIOUS, copy.size()));
        }
        this.previousContents = List.copyOf(copy);
        this.toastLevel = toastLevel == null ? Optional.empty() : toastLevel;
        this.charLevel = charLevel == null ? Optional.empty() : charLevel;
        this.woodExtractionRemaining = woodExtractionRemaining == null
                ? Optional.empty()
                : woodExtractionRemaining;
    }

    public static BarrelHistory empty() {
        return new BarrelHistory(0, List.of(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public BarrelHistory recordEmptying(ResourceId previous) {
        List<ResourceId> next = new ArrayList<>(previousContents);
        if (previous != null) {
            next.add(previous);
        }
        return new BarrelHistory(
                usageCount + 1,
                next,
                toastLevel,
                charLevel,
                woodExtractionRemaining
        );
    }

    @Override
    public int usageCount() {
        return usageCount;
    }

    @Override
    public List<ResourceId> previousContents() {
        return previousContents;
    }

    @Override
    public Optional<Integer> toastLevel() {
        return toastLevel;
    }

    @Override
    public Optional<Integer> charLevel() {
        return charLevel;
    }

    @Override
    public Optional<Double> woodExtractionRemaining() {
        return woodExtractionRemaining;
    }

    public boolean used() {
        return usageCount > 0 || !previousContents.isEmpty();
    }
}
