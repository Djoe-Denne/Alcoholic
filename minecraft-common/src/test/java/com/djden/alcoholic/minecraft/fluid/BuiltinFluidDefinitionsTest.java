package com.djden.alcoholic.minecraft.fluid;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.platform.api.registry.RegistryRef;
import net.minecraft.world.level.material.FlowingFluid;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BuiltinFluidDefinitionsTest {
    @Test
    void catalogContainsNineUniqueDefinitionsWithExpectedProfiles() {
        List<FluidDefinition> definitions = BuiltinFluidDefinitions.all();
        assertEquals(9, definitions.size());
        assertEquals(9, new HashSet<>(definitions.stream().map(FluidDefinition::id).toList()).size());

        assertPainted(definitions, AlcoholicIds.RED_GRAPE_MUST, BuiltinFluidDefinitions.SUGAR_RICH);
        assertPainted(definitions, AlcoholicIds.WHITE_GRAPE_MUST, BuiltinFluidDefinitions.SUGAR_RICH);
        assertPainted(definitions, AlcoholicIds.HOPPED_WORT, BuiltinFluidDefinitions.SUGAR_RICH);
        assertPainted(definitions, AlcoholicIds.BEER, BuiltinFluidDefinitions.FERMENTED);
        assertTintedWater(definitions, AlcoholicIds.WORT, BuiltinFluidDefinitions.SUGAR_RICH, 0xFFC9A227);
        assertTintedWater(definitions, AlcoholicIds.YOUNG_RED_WINE, BuiltinFluidDefinitions.FERMENTED, 0xFF5A1226);
        assertTintedWater(definitions, AlcoholicIds.YOUNG_WHITE_WINE, BuiltinFluidDefinitions.FERMENTED, 0xFFE8D36B);
        assertTintedWater(definitions, AlcoholicIds.RED_WINE, BuiltinFluidDefinitions.FERMENTED, 0xFF4A0E1C);
        assertTintedWater(definitions, AlcoholicIds.WHITE_WINE, BuiltinFluidDefinitions.FERMENTED, 0xFFE6C85A);
        assertFalse(BuiltinFluidDefinitions.SUGAR_RICH.renewableSources());
        assertFalse(BuiltinFluidDefinitions.FERMENTED.renewableSources());
    }

    @Test
    void profileRejectsInvalidPhysicalValues() {
        assertThrows(IllegalArgumentException.class, () -> profile(0, 300, 1000, 5, 4, 1));
        assertThrows(IllegalArgumentException.class, () -> profile(1000, 0, 1000, 5, 4, 1));
        assertThrows(IllegalArgumentException.class, () -> profile(1000, 300, -1, 5, 4, 1));
        assertThrows(IllegalArgumentException.class, () -> profile(1000, 300, 1000, 0, 4, 1));
        assertThrows(IllegalArgumentException.class, () -> profile(1000, 300, 1000, 5, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> profile(1000, 300, 1000, 5, 4, 0));
    }

    @Test
    void registrarPassesEveryDefinitionThroughThePlatformPort() {
        List<FluidDefinition> registered = new ArrayList<>();
        FluidContent content = FluidContentRegistrar.register(definition -> {
            registered.add(definition);
            return ref(definition.id());
        });

        assertEquals(BuiltinFluidDefinitions.all(), registered);
        assertEquals(
                new HashSet<>(BuiltinFluidDefinitions.all().stream().map(FluidDefinition::id).toList()),
                content.ids()
        );
    }

    private static FluidDefinition require(
            List<FluidDefinition> definitions,
            ResourceId id
    ) {
        return definitions.stream()
                .filter(candidate -> candidate.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private static void assertPainted(
            List<FluidDefinition> definitions,
            ResourceId id,
            FluidFlowProfile expected
    ) {
        FluidDefinition definition = require(definitions, id);
        assertEquals(expected, definition.flowProfile());
        assertEquals(
                new ResourceId(AlcoholicIds.MOD_ID, "block/" + id.path() + "_still"),
                definition.stillTexture()
        );
        assertEquals(
                new ResourceId(AlcoholicIds.MOD_ID, "block/" + id.path() + "_flow"),
                definition.flowingTexture()
        );
        assertEquals(0xFFFFFFFF, definition.tintArgb());
    }

    private static void assertTintedWater(
            List<FluidDefinition> definitions,
            ResourceId id,
            FluidFlowProfile expected,
            int tint
    ) {
        FluidDefinition definition = require(definitions, id);
        assertEquals(expected, definition.flowProfile());
        assertEquals(ResourceId.parse("minecraft:block/water_still"), definition.stillTexture());
        assertEquals(ResourceId.parse("minecraft:block/water_flow"), definition.flowingTexture());
        assertEquals(tint, definition.tintArgb());
    }

    private static FluidFlowProfile profile(
            int density,
            int temperature,
            int viscosity,
            int tickRate,
            int slopeDistance,
            int levelDecrease
    ) {
        return new FluidFlowProfile(
                density,
                temperature,
                viscosity,
                tickRate,
                slopeDistance,
                levelDecrease,
                false
        );
    }

    private static RegistryRef<FlowingFluid> ref(ResourceId id) {
        return new RegistryRef<>() {
            @Override
            public ResourceId id() {
                return id;
            }

            @Override
            public FlowingFluid get() {
                return null;
            }
        };
    }
}
