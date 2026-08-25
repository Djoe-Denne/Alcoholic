package com.djden.alcoholic.minecraft.viticulture;

import com.djden.alcoholic.application.compatibility.CompatibilitySnapshot;
import com.djden.alcoholic.application.viticulture.GrapeProviderPort;
import com.djden.alcoholic.application.viticulture.GrowVineUseCase;
import com.djden.alcoholic.application.viticulture.HarvestVineResult;
import com.djden.alcoholic.application.viticulture.HarvestVineUseCase;
import com.djden.alcoholic.application.viticulture.PruneVineUseCase;
import com.djden.alcoholic.application.viticulture.ResolveGrapeProviderUseCase;
import com.djden.alcoholic.domain.viticulture.GrapeHarvest;
import com.djden.alcoholic.domain.viticulture.GrapeHarvestParameters;
import com.djden.alcoholic.domain.viticulture.PruningLevel;
import com.djden.alcoholic.domain.viticulture.Vine;
import com.djden.alcoholic.domain.viticulture.VineEnvironment;
import com.djden.alcoholic.domain.viticulture.VineGrowthParameters;
import com.djden.alcoholic.domain.viticulture.VineVariety;
import com.djden.alcoholic.domain.viticulture.VineyardGrowthService;
import com.djden.alcoholic.domain.viticulture.VineyardHarvestService;
import com.djden.alcoholic.api.ResourceId;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Minecraft-side composition root for viticulture use cases.
 *
 * <p>Each operation captures one immutable settings snapshot, so a resource
 * reload cannot mix old and new configuration during a growth or harvest.</p>
 */
public final class ViticultureRuntime {
    private static final ViticultureRuntime SHARED = new ViticultureRuntime();

    private final ViticultureSettingsStore settings;
    private final PruneVineUseCase pruneVine = new PruneVineUseCase();
    private final AtomicReference<ResolveGrapeProviderUseCase> providerResolver;

    public ViticultureRuntime() {
        this(new ViticultureSettingsStore(), defaultProviderResolver());
    }

    public ViticultureRuntime(
            ViticultureSettingsStore settings,
            ResolveGrapeProviderUseCase providerResolver
    ) {
        this.settings = Objects.requireNonNull(settings, "settings");
        this.providerResolver = new AtomicReference<>(
                Objects.requireNonNull(providerResolver, "providerResolver")
        );
    }

    public static ViticultureRuntime shared() {
        return SHARED;
    }

    public ViticultureSettings settings() {
        return settings.snapshot();
    }

    public ViticultureSettingsStore settingsStore() {
        return settings;
    }

    public void configureProviders(
            CompatibilitySnapshot compatibility,
            GrapeProviderPort internalProvider,
            GrapeProviderPort vineryProvider
    ) {
        providerResolver.set(new ResolveGrapeProviderUseCase(
                compatibility,
                internalProvider,
                vineryProvider
        ));
    }

    public VineVariety<ResourceId> variety(ResourceId id) {
        return settings().forVariety(id).variety();
    }

    public Vine<ResourceId> grow(
            Vine<ResourceId> vine,
            VineEnvironment environment,
            boolean trained,
            double roll
    ) {
        ViticultureSettings snapshot = settings();
        ViticultureSettings.VarietySettings variety = snapshot.forVariety(vine.variety());
        GrowVineUseCase growVine = new GrowVineUseCase(
                new VineyardGrowthService(variety.growth())
        );
        double trellising = snapshot.training(trained).yield();
        return growVine.grow(
                vine,
                new VineGrowthParameters(environment, trellising, roll)
        );
    }

    public Vine<ResourceId> prune(Vine<ResourceId> vine, PruningLevel level) {
        return pruneVine.prune(vine, level);
    }

    public HarvestVineResult harvest(
            Vine<ResourceId> vine,
            VineEnvironment environment,
            boolean trained,
            long harvestTime
    ) {
        ViticultureSettings snapshot = settings();
        ViticultureSettings.VarietySettings variety = snapshot.forVariety(vine.variety());
        HarvestVineUseCase harvestVine = new HarvestVineUseCase(
                new VineyardHarvestService(variety.harvest()),
                providerResolver.get()
        );
        HarvestVineResult raw = harvestVine.harvest(
                vine,
                new GrapeHarvestParameters(environment, 1.0, harvestTime)
        );
        ViticultureSettings.TrainingMultipliers multipliers =
                snapshot.training(trained);
        GrapeHarvest<ResourceId> adjusted = new GrapeHarvest<>(
                raw.harvest().vine(),
                raw.harvest().quantity() * multipliers.yield(),
                clampUnit(raw.harvest().quality() * multipliers.quality()),
                raw.harvest().sugar(),
                raw.harvest().acidity()
        );
        return new HarvestVineResult(adjusted, raw.harvestItem());
    }

    private static ResolveGrapeProviderUseCase defaultProviderResolver() {
        return new ResolveGrapeProviderUseCase(
                new CompatibilitySnapshot(Set.of()),
                new InternalGrapeProvider(),
                UnavailableGrapeProvider.INSTANCE
        );
    }

    private static double clampUnit(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private enum UnavailableGrapeProvider implements GrapeProviderPort {
        INSTANCE;

        @Override
        public ResourceId getPlantingMaterial(VineVariety<ResourceId> variety) {
            throw new IllegalStateException("external grape provider is unavailable");
        }

        @Override
        public ResourceId getHarvestItem(VineVariety<ResourceId> variety) {
            throw new IllegalStateException("external grape provider is unavailable");
        }

        @Override
        public boolean isAvailable(VineVariety<ResourceId> variety) {
            return false;
        }
    }
}
