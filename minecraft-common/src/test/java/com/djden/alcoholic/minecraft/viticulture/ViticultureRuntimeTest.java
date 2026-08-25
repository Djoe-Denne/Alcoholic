package com.djden.alcoholic.minecraft.viticulture;

import com.djden.alcoholic.application.compatibility.CompatibilitySnapshot;
import com.djden.alcoholic.application.viticulture.GrapeProviderPort;
import com.djden.alcoholic.application.viticulture.ResolveGrapeProviderUseCase;
import com.djden.alcoholic.application.viticulture.VineVarieties;
import com.djden.alcoholic.domain.viticulture.Vine;
import com.djden.alcoholic.domain.viticulture.VineEnvironment;
import com.djden.alcoholic.domain.viticulture.VineGrowthConfig;
import com.djden.alcoholic.domain.viticulture.VineGrowthStage;
import com.djden.alcoholic.domain.viticulture.VineVariety;
import com.djden.alcoholic.api.ResourceId;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ViticultureRuntimeTest {
    @Test
    void growthReadsTheLatestAtomicSettingsSnapshot() {
        ViticultureSettings defaults = ViticultureSettings.defaults();
        ViticultureSettings.VarietySettings red =
                defaults.forVariety(VineVarieties.RED_GRAPE);
        ViticultureSettings disabled = defaults.withVariety(
                withGrowthChance(red, 0.0)
        );
        ViticultureSettings enabled = defaults.withVariety(
                withGrowthChance(red, 1.0)
        );
        ViticultureSettingsStore store = new ViticultureSettingsStore(disabled);
        ViticultureRuntime runtime = new ViticultureRuntime(
                store,
                new ResolveGrapeProviderUseCase(
                        new CompatibilitySnapshot(Set.of()),
                        NoopProvider.INSTANCE,
                        NoopProvider.INSTANCE
                )
        );
        Vine<ResourceId> planted = Vine.planted(VineVarieties.RED_GRAPE);
        VineEnvironment ideal = red.growth().climateProfile().idealEnvironment();

        assertEquals(
                VineGrowthStage.PLANTED,
                runtime.grow(planted, ideal, true, 0.0).growthStage()
        );

        store.replace(enabled);

        assertEquals(
                VineGrowthStage.ESTABLISHING,
                runtime.grow(planted, ideal, true, 0.0).growthStage()
        );
    }

    private static ViticultureSettings.VarietySettings withGrowthChance(
            ViticultureSettings.VarietySettings settings,
            double chance
    ) {
        return new ViticultureSettings.VarietySettings(
                settings.variety(),
                new VineGrowthConfig(
                        chance,
                        settings.growth().climateProfile(),
                        1.0
                ),
                settings.harvest()
        );
    }

    private enum NoopProvider implements GrapeProviderPort {
        INSTANCE;

        @Override
        public ResourceId getPlantingMaterial(VineVariety<ResourceId> variety) {
            return new ResourceId("test", "cutting");
        }

        @Override
        public ResourceId getHarvestItem(VineVariety<ResourceId> variety) {
            return new ResourceId("test", "grapes");
        }

        @Override
        public boolean isAvailable(VineVariety<ResourceId> variety) {
            return true;
        }
    }
}
