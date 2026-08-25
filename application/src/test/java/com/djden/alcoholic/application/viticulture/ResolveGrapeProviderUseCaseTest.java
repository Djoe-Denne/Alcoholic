package com.djden.alcoholic.application.viticulture;

import com.djden.alcoholic.application.compatibility.CompatibilitySnapshot;
import com.djden.alcoholic.application.compatibility.KnownMod;
import com.djden.alcoholic.domain.viticulture.VineVariety;
import com.djden.alcoholic.api.ResourceId;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;

class ResolveGrapeProviderUseCaseTest {
    private static final TestProvider INTERNAL = new TestProvider("alcoholic", true);

    @Test
    void usesInternalProviderWhenVineryIsAbsent() {
        TestProvider vinery = new TestProvider("vinery", true);
        ResolveGrapeProviderUseCase useCase = resolver(Set.of(), vinery);

        assertSame(INTERNAL, useCase.resolve(VineVarieties.RED_GRAPE));
    }

    @Test
    void prefersAvailableVineryProviderWhenModIsPresent() {
        TestProvider vinery = new TestProvider("vinery", true);
        ResolveGrapeProviderUseCase useCase = resolver(Set.of(KnownMod.VINERY), vinery);

        assertSame(vinery, useCase.resolve(VineVarieties.RED_GRAPE));
    }

    @Test
    void fallsBackToInternalProviderWhenVineryMappingIsUnavailable() {
        TestProvider vinery = new TestProvider("vinery", false);
        ResolveGrapeProviderUseCase useCase = resolver(Set.of(KnownMod.VINERY), vinery);

        assertSame(INTERNAL, useCase.resolve(VineVarieties.WHITE_GRAPE));
    }

    private static ResolveGrapeProviderUseCase resolver(
            Set<KnownMod> mods,
            GrapeProviderPort vinery
    ) {
        return new ResolveGrapeProviderUseCase(
                new CompatibilitySnapshot(mods),
                INTERNAL,
                vinery
        );
    }

    private record TestProvider(String namespace, boolean available)
            implements GrapeProviderPort {
        @Override
        public ResourceId getPlantingMaterial(VineVariety<ResourceId> variety) {
            return new ResourceId(namespace, variety.id().path() + "_seeds");
        }

        @Override
        public ResourceId getHarvestItem(VineVariety<ResourceId> variety) {
            return new ResourceId(namespace, variety.id().path());
        }

        @Override
        public boolean isAvailable(VineVariety<ResourceId> variety) {
            return available;
        }
    }
}
