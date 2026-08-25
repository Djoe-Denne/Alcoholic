package com.djden.alcoholic.integration.create.forge;

import com.djden.alcoholic.minecraft.environment.HeatSources;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;

import java.util.OptionalDouble;

/**
 * Optional Create heat mapping. Lives in the Create Forge adapter so
 * minecraft-common stays loader-neutral.
 */
public final class CreateHeatProbe {
    private CreateHeatProbe() {
    }

    public static void install() {
        HeatSources.register((level, heatPos, state) -> {
            if (!(state.getBlock() instanceof BlazeBurnerBlock)
                    || !state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
                return OptionalDouble.empty();
            }
            return switch (state.getValue(BlazeBurnerBlock.HEAT_LEVEL)) {
                case SEETHING -> OptionalDouble.of(110.0);
                case KINDLED -> OptionalDouble.of(100.0);
                case FADING -> OptionalDouble.of(90.0);
                case SMOULDERING -> OptionalDouble.of(80.0);
                case NONE -> OptionalDouble.empty();
            };
        });
    }
}
