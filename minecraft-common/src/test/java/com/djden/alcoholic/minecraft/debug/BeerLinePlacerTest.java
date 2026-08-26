package com.djden.alcoholic.minecraft.debug;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeerLinePlacerTest {
    @Test
    void aliasesCoverArtisanalAndIndustrialBeerLine() {
        assertTrue(BeerLinePlacer.spec("malting_floor").isPresent());
        assertTrue(BeerLinePlacer.spec("malt_mill").isPresent());
        assertTrue(BeerLinePlacer.spec("mash_tun").isPresent());
        assertTrue(BeerLinePlacer.spec("brewing_kettle").isPresent());
        assertTrue(BeerLinePlacer.spec("fermenter").isPresent());
        assertTrue(BeerLinePlacer.spec("malt_house").isPresent());
        assertTrue(BeerLinePlacer.spec("roller_mill").isPresent());
        assertTrue(BeerLinePlacer.spec("industrial_mash_tun").isPresent());
        assertTrue(BeerLinePlacer.spec("industrial_brewing_kettle").isPresent());
        assertTrue(BeerLinePlacer.spec("vat").isPresent());
        assertTrue(BeerLinePlacer.spec("conditioning").isPresent());
        assertTrue(BeerLinePlacer.spec("tank").isPresent());
        assertTrue(BeerLinePlacer.spec("unknown").isEmpty());
        assertEquals(5, BeerLinePlacer.artisanalLineAliases().size());
        assertEquals(7, BeerLinePlacer.industrialLineAliases().size());
        assertFalse(BeerLinePlacer.spec("malting_floor").orElseThrow().industrial());
        assertTrue(BeerLinePlacer.spec("malt_house").orElseThrow().industrial());
    }
}
