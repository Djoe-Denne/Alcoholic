package com.djden.alcoholic.application.compatibility;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompatibilityServiceTest {
    @Test
    void snapshotsKnownModsWithoutExposingLoaderApis() {
        Set<String> loaded = Set.of("vinery", "create", "unrelated");

        CompatibilitySnapshot snapshot = new CompatibilityService(loaded::contains).snapshot();

        assertTrue(snapshot.isPresent(KnownMod.VINERY));
        assertTrue(snapshot.isPresent(KnownMod.CREATE));
        assertFalse(snapshot.isPresent(KnownMod.BREWERY));
    }
}
