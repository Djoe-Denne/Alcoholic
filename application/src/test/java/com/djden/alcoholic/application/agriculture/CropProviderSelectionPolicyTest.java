package com.djden.alcoholic.application.agriculture;

import com.djden.alcoholic.application.compatibility.CompatibilitySnapshot;
import com.djden.alcoholic.application.compatibility.KnownMod;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CropProviderSelectionPolicyTest {
    @Test
    void usesBuiltinGrapesWhenVineryIsAbsent() {
        CropProviderSelectionPolicy policy = policyWith();

        assertEquals(CropProvider.BUILTIN, policy.preferredProvider(CropKind.GRAPES));
        assertTrue(policy.isBuiltinAcquisitionEnabled(
                CropKind.GRAPES,
                GameplaySource.WORLD_GENERATION
        ));
        assertTrue(policy.isBuiltinAcquisitionEnabled(
                CropKind.GRAPES,
                GameplaySource.CREATIVE_DISCOVERY
        ));
    }

    @Test
    void keepsBuiltinIdsButDisablesNewAcquisitionWhenVineryIsPresent() {
        CropProviderSelectionPolicy policy = policyWith(KnownMod.VINERY);

        assertEquals(CropProvider.EXTERNAL, policy.preferredProvider(CropKind.GRAPES));
        assertFalse(policy.isBuiltinAcquisitionEnabled(
                CropKind.GRAPES,
                GameplaySource.WORLD_GENERATION
        ));
        assertFalse(policy.isBuiltinAcquisitionEnabled(
                CropKind.GRAPES,
                GameplaySource.LOOT_INJECTION
        ));
    }

    private static CropProviderSelectionPolicy policyWith(KnownMod... mods) {
        return new CropProviderSelectionPolicy(new CompatibilitySnapshot(Set.of(mods)));
    }
}
