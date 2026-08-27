package com.djden.alcoholic.api.process;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.ingredient.IngredientSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Loader-neutral IO view of a decoded process config. Recipe viewers adapt
 * this; they must not invent volumes or counts that the config omitted.
 */
@PublicApi
public record ProcessDisplaySpec(
        List<ItemPart> itemInputs,
        List<FluidPart> fluidInputs,
        List<ItemPart> itemOutputs,
        List<FluidPart> fluidOutputs,
        OptionalInt durationTicks,
        Optional<TemperatureRange> preferredTemperature
) {
    public ProcessDisplaySpec {
        itemInputs = List.copyOf(new ArrayList<>(Objects.requireNonNull(itemInputs, "itemInputs")));
        fluidInputs = List.copyOf(new ArrayList<>(Objects.requireNonNull(fluidInputs, "fluidInputs")));
        itemOutputs = List.copyOf(new ArrayList<>(Objects.requireNonNull(itemOutputs, "itemOutputs")));
        fluidOutputs = List.copyOf(new ArrayList<>(Objects.requireNonNull(fluidOutputs, "fluidOutputs")));
        durationTicks = durationTicks == null ? OptionalInt.empty() : durationTicks;
        preferredTemperature = preferredTemperature == null ? Optional.empty() : preferredTemperature;
        if (durationTicks.isPresent() && durationTicks.getAsInt() < 1) {
            throw new IllegalArgumentException("durationTicks must be >= 1");
        }
    }

    public boolean visible() {
        return !itemInputs.isEmpty()
                || !fluidInputs.isEmpty()
                || !itemOutputs.isEmpty()
                || !fluidOutputs.isEmpty();
    }

    public static ProcessDisplaySpec fromAccepting(Object config) {
        Builder builder = builder();
        if (config instanceof SolidAccepting accepting) {
            accepting.inputSelector().ifPresent(selector -> builder.itemIn(selector, 1));
        }
        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static OptionalInt millibuckets(double volume) {
        if (!Double.isFinite(volume) || volume <= 0.0) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(Math.max(1, (int) Math.round(volume)));
    }

    @PublicApi
    public record ItemPart(IngredientSelector selector, int count, Optional<String> hint) {
        public ItemPart {
            Objects.requireNonNull(selector, "selector");
            if (count < 1) {
                throw new IllegalArgumentException("count must be >= 1");
            }
            hint = hint == null ? Optional.empty() : hint;
        }

        public ItemPart(IngredientSelector selector, int count) {
            this(selector, count, Optional.empty());
        }
    }

    @PublicApi
    public record FluidPart(ResourceId fluid, OptionalInt millibuckets) {
        public FluidPart {
            Objects.requireNonNull(fluid, "fluid");
            millibuckets = millibuckets == null ? OptionalInt.empty() : millibuckets;
            if (millibuckets.isPresent() && millibuckets.getAsInt() < 1) {
                throw new IllegalArgumentException("millibuckets must be >= 1");
            }
        }

        public static FluidPart of(ResourceId fluid, int millibuckets) {
            return new FluidPart(fluid, OptionalInt.of(millibuckets));
        }

        public static FluidPart unknownVolume(ResourceId fluid) {
            return new FluidPart(fluid, OptionalInt.empty());
        }
    }

    @PublicApi
    public record TemperatureRange(double min, double max) {
    }

    @PublicApi
    public static final class Builder {
        private final List<ItemPart> itemInputs = new ArrayList<>();
        private final List<FluidPart> fluidInputs = new ArrayList<>();
        private final List<ItemPart> itemOutputs = new ArrayList<>();
        private final List<FluidPart> fluidOutputs = new ArrayList<>();
        private OptionalInt durationTicks = OptionalInt.empty();
        private Optional<TemperatureRange> preferredTemperature = Optional.empty();

        private Builder() {
        }

        public Builder itemIn(IngredientSelector selector, int count) {
            itemInputs.add(new ItemPart(selector, count));
            return this;
        }

        public Builder itemIn(IngredientSelector selector, int count, String hint) {
            itemInputs.add(new ItemPart(selector, count, Optional.of(hint)));
            return this;
        }

        public Builder itemOut(ResourceId item, int count) {
            itemOutputs.add(new ItemPart(new IngredientSelector.Item(item), count));
            return this;
        }

        public Builder fluidIn(ResourceId fluid, OptionalInt millibuckets) {
            fluidInputs.add(new FluidPart(fluid, millibuckets));
            return this;
        }

        public Builder fluidOut(ResourceId fluid, OptionalInt millibuckets) {
            fluidOutputs.add(new FluidPart(fluid, millibuckets));
            return this;
        }

        public Builder duration(int ticks) {
            if (ticks > 0) {
                durationTicks = OptionalInt.of(ticks);
            }
            return this;
        }

        public Builder preferred(double min, double max) {
            preferredTemperature = Optional.of(new TemperatureRange(min, max));
            return this;
        }

        public ProcessDisplaySpec build() {
            return new ProcessDisplaySpec(
                    itemInputs,
                    fluidInputs,
                    itemOutputs,
                    fluidOutputs,
                    durationTicks,
                    preferredTemperature
            );
        }
    }
}
