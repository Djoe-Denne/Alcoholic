package com.djden.alcoholic.minecraft.debug;

import com.djden.alcoholic.application.machine.BuiltinMachines;
import com.djden.alcoholic.application.machine.MachineCatalog;
import com.djden.alcoholic.domain.multiblock.HollowCuboidValidator;
import com.djden.alcoholic.domain.multiblock.IndustrialHullPattern;
import com.djden.alcoholic.domain.multiblock.MultiblockDefinition;
import com.djden.alcoholic.domain.multiblock.PartRole;
import com.djden.alcoholic.domain.multiblock.ValidationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    void industrialShowcaseSizesMatchRecipeHull() {
        assertEquals(new BeerLinePlacer.Dimensions(5, 4, 5), BeerLinePlacer.spec("malt_house").orElseThrow().size());
        assertEquals(new BeerLinePlacer.Dimensions(3, 4, 3), BeerLinePlacer.spec("roller_mill").orElseThrow().size());
        assertEquals(
                new BeerLinePlacer.Dimensions(5, 5, 5),
                BeerLinePlacer.spec("industrial_mash_tun").orElseThrow().size()
        );
        assertEquals(
                new BeerLinePlacer.Dimensions(5, 6, 5),
                BeerLinePlacer.spec("industrial_brewing_kettle").orElseThrow().size()
        );
        assertEquals(new BeerLinePlacer.Dimensions(3, 5, 3), BeerLinePlacer.spec("vat").orElseThrow().size());
        assertEquals(new BeerLinePlacer.Dimensions(3, 6, 3), BeerLinePlacer.spec("conditioning").orElseThrow().size());
        assertEquals(new BeerLinePlacer.Dimensions(3, 5, 3), BeerLinePlacer.spec("tank").orElseThrow().size());
        assertTrue(BeerLinePlacer.spec("roller_mill").orElseThrow().engine());
    }

    @Test
    void industrialLineLeavesAirGapBetweenMachines() {
        List<Integer> offsets = BeerLinePlacer.industrialLineOffsets();
        List<String> aliases = BeerLinePlacer.industrialLineAliases();
        assertEquals(aliases.size(), offsets.size());
        for (int i = 0; i < aliases.size() - 1; i++) {
            BeerLinePlacer.MachineSpec spec = BeerLinePlacer.spec(aliases.get(i)).orElseThrow();
            int occupied = spec.size().width() + (spec.engine() ? 1 : 0);
            assertTrue(
                    offsets.get(i + 1) >= offsets.get(i) + occupied + BeerLinePlacer.INDUSTRIAL_GAP,
                    aliases.get(i) + " overlaps " + aliases.get(i + 1)
            );
        }
    }

    @Test
    void industrialShowcaseHullsForm() {
        for (String alias : BeerLinePlacer.industrialLineAliases()) {
            BeerLinePlacer.MachineSpec spec = BeerLinePlacer.spec(alias).orElseThrow();
            MultiblockDefinition definition = MachineCatalog.builtins().get(spec.definitionId()).orElseThrow();
            BeerLinePlacer.Dimensions size = spec.size();
            boolean kinetic = definition.constraints().requiredPorts().contains(PartRole.KINETIC_PORT);
            String casing = definition.constraints().casingTags().iterator().next();
            ValidationResult result = HollowCuboidValidator.validate(
                    definition,
                    IndustrialHullPattern.controller(size.width(), size.height(), size.depth()),
                    IndustrialHullPattern.query(
                            size.width(),
                            size.height(),
                            size.depth(),
                            kinetic,
                            casing,
                            BuiltinMachines.WINDOWS,
                            BuiltinMachines.PORTS,
                            definition.controllerBlockId()
                    ),
                    0
            );
            assertTrue(result.formed(), alias + ": " + result.reason());
        }
    }
}
