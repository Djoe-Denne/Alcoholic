package com.djden.alcoholic.integration.create.forge;

import com.djden.alcoholic.application.compatibility.CompatibilitySnapshot;
import com.djden.alcoholic.application.compatibility.KnownMod;

/**
 * Loader-specific activation seam for the Forge Create adapter.
 *
 * <p>Create Mechanical Press recipes are data: generic PRESS definitions marked
 * {@code create_compatible} are translated into {@code create:compacting}
 * JSON. This class only answers whether Create is present at runtime.</p>
 */
public final class ForgeCreateIntegration {
    private ForgeCreateIntegration() {
    }

    public static boolean shouldActivate(CompatibilitySnapshot compatibility) {
        return compatibility.isPresent(KnownMod.CREATE);
    }
}
