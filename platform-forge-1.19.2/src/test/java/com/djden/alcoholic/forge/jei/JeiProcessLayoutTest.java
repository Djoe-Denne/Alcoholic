package com.djden.alcoholic.forge.jei;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.ingredient.IngredientSelector;
import com.djden.alcoholic.api.process.ProcessDisplaySpec;
import com.djden.alcoholic.application.process.ProcessDisplayRecipe;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JeiProcessLayoutTest {
    private static final ResourceId WATER = ResourceId.parse("minecraft:water");
    private static final ResourceId WORT = ResourceId.parse("alcoholic:wort");
    private static final ResourceId MUST = ResourceId.parse("alcoholic:red_grape_must");
    private static final ResourceId GRIST = ResourceId.parse("alcoholic:grist");
    private static final ResourceId GRAPES = ResourceId.parse("alcoholic:red_grapes");

    @Test
    void fluidInputsSitLeftOfFluidOutputs() {
        JeiProcessLayout layout = new JeiProcessLayout(false);
        assertTrue(layout.fluidIn(0).x() < layout.fluidOut(0).x());
        assertEquals(JeiProcessLayout.LEFT, layout.fluidIn(0).x());
        assertEquals(JeiProcessLayout.RIGHT, layout.fluidOut(0).x());
    }

    @Test
    void mashPutsWaterLeftAndWortRight() {
        ProcessDisplayRecipe mash = recipe(
                "alcoholic:mash",
                ProcessDisplaySpec.builder()
                        .itemIn(new IngredientSelector.Item(GRIST), 1)
                        .fluidIn(WATER, OptionalInt.of(1000))
                        .fluidOut(WORT, OptionalInt.of(1000))
                        .duration(80)
                        .build()
        );
        JeiProcessLayout layout = JeiProcessLayout.of(mash);
        assertEquals(JeiProcessLayout.LEFT, layout.fluidIn(0).x());
        assertEquals(JeiProcessLayout.RIGHT, layout.fluidOut(0).x());
        assertEquals(layout.fluidIn(0).y(), layout.fluidOut(0).y());
    }

    @Test
    void pressPutsTheOnlyTankOnTheRight() {
        ProcessDisplayRecipe press = recipe(
                "alcoholic:press",
                ProcessDisplaySpec.builder()
                        .itemIn(new IngredientSelector.Item(GRAPES), 1)
                        .fluidOut(MUST, OptionalInt.of(250))
                        .duration(20)
                        .build()
        );
        JeiProcessLayout layout = JeiProcessLayout.of(press);
        assertEquals(JeiProcessLayout.RIGHT, layout.fluidOut(0).x());
        assertTrue(layout.itemIn(0).x() < layout.fluidOut(0).x());
    }

    @Test
    void itemsSitBelowTanks() {
        JeiProcessLayout withHeader = new JeiProcessLayout(true);
        JeiProcessLayout bare = new JeiProcessLayout(false);
        assertTrue(withHeader.itemIn(0).y() >= withHeader.fluidIn(0).y() + JeiProcessLayout.TANK_HEIGHT);
        assertTrue(bare.itemOut(0).y() >= bare.fluidOut(0).y() + JeiProcessLayout.TANK_HEIGHT);
        assertEquals(8, bare.fluidY());
        assertEquals(24, withHeader.fluidY());
    }

    @Test
    void tankCapacityFillsTheTankInsteadOfASliver() {
        assertEquals(1, JeiIngredients.tankCapacity(ProcessDisplaySpec.FluidPart.unknownVolume(WORT)));
        assertEquals(250, JeiIngredients.tankCapacity(ProcessDisplaySpec.FluidPart.of(MUST, 250)));
        assertEquals(1000, JeiIngredients.tankCapacity(ProcessDisplaySpec.FluidPart.of(WATER, 1000)));
    }

    private static ProcessDisplayRecipe recipe(String id, ProcessDisplaySpec spec) {
        ResourceId resource = ResourceId.parse(id);
        return new ProcessDisplayRecipe(resource, resource, spec);
    }
}
