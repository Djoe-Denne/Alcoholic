package com.djden.alcoholic.api.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.ingredient.IngredientSelector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessDisplaySpecTest {
    @Test
    void rejectsInventedCountsAndVolumes() {
        IngredientSelector selector = new IngredientSelector.Item(ResourceId.parse("minecraft:apple"));
        assertThrows(IllegalArgumentException.class, () -> new ProcessDisplaySpec.ItemPart(selector, 0));
        assertThrows(
                IllegalArgumentException.class,
                () -> ProcessDisplaySpec.FluidPart.of(ResourceId.parse("minecraft:water"), 0)
        );
    }

    @Test
    void omitsNonPositiveVolumes() {
        assertTrue(ProcessDisplaySpec.millibuckets(0).isEmpty());
        assertTrue(ProcessDisplaySpec.millibuckets(Double.NaN).isEmpty());
        assertEquals(1000, ProcessDisplaySpec.millibuckets(1000.4).orElseThrow());
    }

    @Test
    void fromAcceptingUsesSolidInputOnly() {
        SolidAccepting config = () -> java.util.Optional.of(
                new IngredientSelector.Tag(ResourceId.parse("alcoholic:barley"))
        );
        ProcessDisplaySpec spec = ProcessDisplaySpec.fromAccepting(config);
        assertEquals(1, spec.itemInputs().size());
        assertTrue(spec.fluidInputs().isEmpty());
    }
}
