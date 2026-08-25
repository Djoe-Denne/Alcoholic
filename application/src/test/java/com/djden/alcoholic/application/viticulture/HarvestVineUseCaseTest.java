package com.djden.alcoholic.application.viticulture;

import com.djden.alcoholic.application.compatibility.CompatibilitySnapshot;
import com.djden.alcoholic.application.compatibility.KnownMod;
import com.djden.alcoholic.domain.viticulture.ClimateProfile;
import com.djden.alcoholic.domain.viticulture.GrapeHarvest;
import com.djden.alcoholic.domain.viticulture.GrapeHarvestParameters;
import com.djden.alcoholic.domain.viticulture.Vine;
import com.djden.alcoholic.domain.viticulture.VineGrowthStage;
import com.djden.alcoholic.domain.viticulture.VineHealth;
import com.djden.alcoholic.domain.viticulture.VineVariety;
import com.djden.alcoholic.domain.viticulture.VineyardHarvestService;
import com.djden.alcoholic.api.ResourceId;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HarvestVineUseCaseTest {
    private static final ResourceId INTERNAL_GRAPES =
            new ResourceId("alcoholic", "red_grape");
    private static final ResourceId VINERY_GRAPES =
            new ResourceId("vinery", "red_grape");
    private static final GrapeHarvestParameters PARAMETERS = new GrapeHarvestParameters(
            ClimateProfile.TEMPERATE.idealEnvironment(),
            1.25,
            1_234L
    );

    @Test
    void returnsDomainHarvestWithPreferredProviderItem() {
        VineyardHarvestService service = new VineyardHarvestService();
        Vine<ResourceId> vine = readyVine();
        HarvestVineUseCase useCase = new HarvestVineUseCase(
                service,
                resolver(Set.of(KnownMod.VINERY), true)
        );

        HarvestVineResult result = useCase.harvest(vine, PARAMETERS);
        GrapeHarvest<ResourceId> expected = service.harvest(vine, PARAMETERS);

        assertEquals(expected, result.harvest());
        assertEquals(VINERY_GRAPES, result.harvestItem());
    }

    @Test
    void usesInternalHarvestItemWhenVineryIsUnavailable() {
        HarvestVineUseCase useCase = new HarvestVineUseCase(
                resolver(Set.of(KnownMod.VINERY), false)
        );

        HarvestVineResult result = useCase.harvest(readyVine(), PARAMETERS);

        assertEquals(INTERNAL_GRAPES, result.harvestItem());
        assertEquals(VineGrowthStage.DORMANT, result.harvest().vine().growthStage());
        assertEquals(1_234L, result.harvest().vine().lastHarvest());
    }

    private static ResolveGrapeProviderUseCase resolver(
            Set<KnownMod> mods,
            boolean vineryAvailable
    ) {
        return new ResolveGrapeProviderUseCase(
                new CompatibilitySnapshot(mods),
                new FixedProvider(INTERNAL_GRAPES, true),
                new FixedProvider(VINERY_GRAPES, vineryAvailable)
        );
    }

    private static Vine<ResourceId> readyVine() {
        return new Vine<>(
                VineVarieties.RED_GRAPE,
                VineGrowthStage.HARVEST_READY,
                0,
                false,
                VineHealth.HEALTHY
        );
    }

    private record FixedProvider(ResourceId harvestItem, boolean available)
            implements GrapeProviderPort {
        @Override
        public ResourceId getPlantingMaterial(VineVariety<ResourceId> variety) {
            return harvestItem;
        }

        @Override
        public ResourceId getHarvestItem(VineVariety<ResourceId> variety) {
            return harvestItem;
        }

        @Override
        public boolean isAvailable(VineVariety<ResourceId> variety) {
            return available;
        }
    }
}
