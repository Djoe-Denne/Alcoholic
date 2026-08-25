package com.djden.alcoholic.application.viticulture;

import com.djden.alcoholic.domain.ingredient.GrapeColor;
import com.djden.alcoholic.api.ResourceId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VineVarietiesTest {
    @Test
    void exposesStableAlcoholicVarietyIdsAndColors() {
        assertEquals(
                new ResourceId("alcoholic", "red_grape"),
                VineVarieties.RED_GRAPE.id()
        );
        assertEquals(GrapeColor.RED, VineVarieties.RED_GRAPE.grapeColor());
        assertEquals(
                new ResourceId("alcoholic", "white_grape"),
                VineVarieties.WHITE_GRAPE.id()
        );
        assertEquals(GrapeColor.WHITE, VineVarieties.WHITE_GRAPE.grapeColor());
    }
}
