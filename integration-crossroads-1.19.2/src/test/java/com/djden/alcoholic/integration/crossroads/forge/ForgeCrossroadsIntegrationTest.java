package com.djden.alcoholic.integration.crossroads.forge;

import com.djden.alcoholic.application.compatibility.CompatibilitySnapshot;
import com.djden.alcoholic.application.compatibility.KnownMod;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeCrossroadsIntegrationTest {
    @Test
    void activatesOnlyWhenCrossroadsIsPresent() {
        assertFalse(ForgeCrossroadsIntegration.shouldActivate(
                new CompatibilitySnapshot(Set.of())
        ));
        assertTrue(ForgeCrossroadsIntegration.shouldActivate(
                new CompatibilitySnapshot(Set.of(KnownMod.CROSSROADS))
        ));
        assertFalse(ForgeCrossroadsIntegration.shouldActivate(
                new CompatibilitySnapshot(Set.of(KnownMod.CREATE))
        ));
    }
}
