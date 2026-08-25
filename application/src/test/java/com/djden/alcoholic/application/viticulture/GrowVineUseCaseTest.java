package com.djden.alcoholic.application.viticulture;

import com.djden.alcoholic.domain.viticulture.ClimateProfile;
import com.djden.alcoholic.domain.viticulture.Vine;
import com.djden.alcoholic.domain.viticulture.VineGrowthConfig;
import com.djden.alcoholic.domain.viticulture.VineGrowthParameters;
import com.djden.alcoholic.domain.viticulture.VineyardGrowthService;
import com.djden.alcoholic.api.ResourceId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GrowVineUseCaseTest {
    @Test
    void delegatesGrowthUnchangedToDomainService() {
        VineyardGrowthService service = new VineyardGrowthService(
                new VineGrowthConfig(1.0, ClimateProfile.TEMPERATE, 0.4)
        );
        GrowVineUseCase useCase = new GrowVineUseCase(service);
        Vine<ResourceId> vine = Vine.planted(VineVarieties.WHITE_GRAPE);
        VineGrowthParameters parameters = new VineGrowthParameters(
                ClimateProfile.TEMPERATE.idealEnvironment(),
                1.25,
                0.0
        );

        assertEquals(service.grow(vine, parameters), useCase.grow(vine, parameters));
    }
}
