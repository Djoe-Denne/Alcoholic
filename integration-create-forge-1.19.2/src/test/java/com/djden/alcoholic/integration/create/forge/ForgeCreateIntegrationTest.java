package com.djden.alcoholic.integration.create.forge;

import com.djden.alcoholic.application.compatibility.CompatibilitySnapshot;
import com.djden.alcoholic.application.compatibility.KnownMod;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeCreateIntegrationTest {
    @Test
    void activatesOnlyWhenCreateIsPresent() {
        assertFalse(ForgeCreateIntegration.shouldActivate(
                new CompatibilitySnapshot(Set.of())
        ));
        assertTrue(ForgeCreateIntegration.shouldActivate(
                new CompatibilitySnapshot(Set.of(KnownMod.CREATE))
        ));
    }
}
