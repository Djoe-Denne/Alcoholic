package com.djden.alcoholic.application.viticulture;

import com.djden.alcoholic.application.compatibility.CompatibilitySnapshot;
import com.djden.alcoholic.application.compatibility.KnownMod;
import com.djden.alcoholic.domain.viticulture.VineVariety;
import com.djden.alcoholic.api.ResourceId;

import java.util.Objects;

public final class ResolveGrapeProviderUseCase {
    private final CompatibilitySnapshot compatibility;
    private final GrapeProviderPort internalProvider;
    private final GrapeProviderPort vineryProvider;

    public ResolveGrapeProviderUseCase(
            CompatibilitySnapshot compatibility,
            GrapeProviderPort internalProvider,
            GrapeProviderPort vineryProvider
    ) {
        this.compatibility = Objects.requireNonNull(compatibility, "compatibility");
        this.internalProvider = Objects.requireNonNull(internalProvider, "internalProvider");
        this.vineryProvider = Objects.requireNonNull(vineryProvider, "vineryProvider");
    }

    public GrapeProviderPort resolve(VineVariety<ResourceId> variety) {
        Objects.requireNonNull(variety, "variety");
        if (compatibility.isPresent(KnownMod.VINERY)
                && vineryProvider.isAvailable(variety)) {
            return vineryProvider;
        }
        return internalProvider;
    }
}
