package com.djden.alcoholic.minecraft.viticulture;

import com.djden.alcoholic.application.viticulture.VineVarieties;
import com.djden.alcoholic.domain.viticulture.PruningLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ViticultureSettingsTest {
    @Test
    void defaultsExposeDistinctRedAndWhiteClimateProfiles() {
        ViticultureSettings settings = ViticultureSettings.defaults();

        assertEquals(
                25.0,
                settings.forVariety(VineVarieties.RED_GRAPE)
                        .growth()
                        .climateProfile()
                        .idealEnvironment()
                        .temperatureCelsius()
        );
        assertEquals(
                18.0,
                settings.forVariety(VineVarieties.WHITE_GRAPE)
                        .growth()
                        .climateProfile()
                        .idealEnvironment()
                        .temperatureCelsius()
        );
        assertEquals(
                0.50,
                settings.forVariety(VineVarieties.RED_GRAPE)
                        .growth()
                        .climateProfile()
                        .idealEnvironment()
                        .humidity()
        );
        assertEquals(
                0.65,
                settings.forVariety(VineVarieties.WHITE_GRAPE)
                        .growth()
                        .climateProfile()
                        .idealEnvironment()
                        .humidity()
        );
        assertEquals(
                1.20,
                settings.forVariety(VineVarieties.RED_GRAPE)
                        .harvest()
                        .pruningProfile(PruningLevel.LIGHT)
                        .yieldMultiplier()
        );
        assertEquals(
                0.92,
                settings.forVariety(VineVarieties.RED_GRAPE)
                        .harvest()
                        .pruningProfile(PruningLevel.LIGHT)
                        .qualityMultiplier()
        );
        assertEquals(
                0.75,
                settings.forVariety(VineVarieties.WHITE_GRAPE)
                        .harvest()
                        .pruningProfile(PruningLevel.SEVERE)
                        .yieldMultiplier()
        );
        assertEquals(
                1.12,
                settings.forVariety(VineVarieties.WHITE_GRAPE)
                        .harvest()
                        .pruningProfile(PruningLevel.SEVERE)
                        .qualityMultiplier()
        );
        assertEquals(0.70, settings.untrained().yield());
        assertEquals(0.85, settings.untrained().quality());
        assertEquals(1.0, settings.trained().yield());
        assertEquals(1.0, settings.trained().quality());
        assertEquals(32, settings.maxWireDistance());
    }

    @Test
    void storePublishesWholeSnapshots() {
        ViticultureSettings initial = ViticultureSettings.defaults();
        ViticultureSettings replacement = initial.withInfrastructure(
                initial.untrained(),
                initial.trained(),
                24
        );
        ViticultureSettingsStore store = new ViticultureSettingsStore(initial);

        store.replace(replacement);

        assertSame(replacement, store.snapshot());
        assertEquals(24, store.snapshot().maxWireDistance());
    }
}
