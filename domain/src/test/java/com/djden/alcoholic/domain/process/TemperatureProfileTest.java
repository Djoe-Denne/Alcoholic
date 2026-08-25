package com.djden.alcoholic.domain.process;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemperatureProfileTest {
    @Test
    void stalledFollowsNonPositiveRateFactor() {
        TemperatureProfile profile = TemperatureProfile.fermentationDefault();
        for (double celsius : new double[]{-10.0, 5.0, 20.0, 28.0, 40.0}) {
            assertEquals(profile.rateFactor(celsius) <= 0.0, profile.stalled(celsius), "at " + celsius);
        }
    }

    @Test
    void preferredBandIsNeverStalledEvenOutsideOperating() {
        TemperatureProfile profile = new TemperatureProfile(
                new TemperatureBand(40.0, 50.0),
                new TemperatureBand(10.0, 30.0),
                new TemperatureBand(-40.0, 80.0)
        );
        assertEquals(1.0, profile.rateFactor(45.0));
        assertFalse(profile.stalled(45.0));
        assertTrue(profile.stalled(0.0));
    }
}
