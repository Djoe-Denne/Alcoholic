package com.djden.alcoholic.application.agriculture;

import com.djden.alcoholic.application.compatibility.CompatibilitySnapshot;
import com.djden.alcoholic.application.compatibility.KnownMod;

import java.util.Map;

public final class CropProviderSelectionPolicy {
    private static final Map<CropKind, KnownMod> EXTERNAL_PROVIDERS = Map.of(
            CropKind.GRAPES, KnownMod.VINERY,
            CropKind.BARLEY, KnownMod.BREWERY,
            CropKind.HOPS, KnownMod.BREWERY,
            CropKind.YEAST, KnownMod.BREWERY
    );

    @FunctionalInterface
    public interface ExternalCropAvailability {
        boolean isAvailable(CropKind crop);
    }

    private final CompatibilitySnapshot compatibility;
    private final ExternalCropAvailability availability;

    public CropProviderSelectionPolicy(CompatibilitySnapshot compatibility) {
        this(compatibility, crop -> true);
    }

    public CropProviderSelectionPolicy(
            CompatibilitySnapshot compatibility,
            ExternalCropAvailability availability
    ) {
        this.compatibility = compatibility;
        this.availability = availability == null ? crop -> true : availability;
    }

    public CropProvider preferredProvider(CropKind crop) {
        KnownMod externalProvider = EXTERNAL_PROVIDERS.get(crop);
        return externalProvider != null
                && compatibility.isPresent(externalProvider)
                && availability.isAvailable(crop)
                ? CropProvider.EXTERNAL
                : CropProvider.BUILTIN;
    }

    public boolean isBuiltinAcquisitionEnabled(CropKind crop, GameplaySource source) {
        return preferredProvider(crop) == CropProvider.BUILTIN;
    }
}
