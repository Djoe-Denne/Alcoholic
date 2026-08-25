package com.djden.alcoholic.minecraft.viticulture;

import com.djden.alcoholic.domain.viticulture.ClimateProfile;
import com.djden.alcoholic.domain.viticulture.GrapeHarvestConfig;
import com.djden.alcoholic.domain.viticulture.PruningLevel;
import com.djden.alcoholic.domain.viticulture.PruningProfile;
import com.djden.alcoholic.domain.viticulture.VineEnvironment;
import com.djden.alcoholic.domain.viticulture.VineGrowthConfig;
import com.djden.alcoholic.domain.viticulture.VineVariety;
import com.djden.alcoholic.api.ResourceId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Vanilla data-pack listener for JSON below the {@code viticulture} data
 * directory in every namespace.
 */
public final class ViticultureDataReloadListener
        extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();

    private final ViticultureSettingsStore store;

    public ViticultureDataReloadListener(ViticultureRuntime runtime) {
        this(Objects.requireNonNull(runtime, "runtime").settingsStore());
    }

    public ViticultureDataReloadListener(ViticultureSettingsStore store) {
        super(GSON, "viticulture");
        this.store = Objects.requireNonNull(store, "store");
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> resources,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        try {
            ViticultureSettings replacement = parseSnapshot(resources);
            store.replace(replacement);
            LOGGER.info(
                    "Loaded {} viticulture variety configuration(s)",
                    replacement.varieties().size()
            );
        } catch (RuntimeException exception) {
            LOGGER.error(
                    "Viticulture data reload rejected; keeping the previous snapshot",
                    exception
            );
        }
    }

    public static ViticultureSettings parseSnapshot(
            Map<ResourceLocation, JsonElement> resources
    ) {
        Objects.requireNonNull(resources, "resources");
        ViticultureSettings result = ViticultureSettings.defaults();
        Set<ResourceId> configuredVarieties = new HashSet<>();
        boolean infrastructureConfigured = false;

        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            ResourceLocation source = entry.getKey();
            if (!entry.getValue().isJsonObject()) {
                throw invalid(source, "root must be a JSON object");
            }
            JsonObject object = entry.getValue().getAsJsonObject();
            if (isInfrastructureFile(source, object)) {
                if (infrastructureConfigured) {
                    throw invalid(source, "multiple infrastructure settings files");
                }
                result = parseInfrastructure(source, object, result);
                infrastructureConfigured = true;
                continue;
            }

            ResourceId varietyId = parseVarietyId(source, object);
            if (!configuredVarieties.add(varietyId)) {
                throw invalid(source, "duplicate variety " + varietyId);
            }
            ViticultureSettings.VarietySettings defaults;
            try {
                defaults = result.forVariety(varietyId);
            } catch (IllegalArgumentException exception) {
                throw invalid(source, "unregistered variety " + varietyId);
            }
            result = result.withVariety(parseVariety(source, object, defaults));
        }
        return result;
    }

    private static ViticultureSettings parseInfrastructure(
            ResourceLocation source,
            JsonObject object,
            ViticultureSettings defaults
    ) {
        int maximumDistance = integer(
                object,
                "max_wire_distance",
                defaults.maxWireDistance()
        );
        ViticultureSettings.TrainingMultipliers untrained = multipliers(
                child(object, "untrained"),
                defaults.untrained()
        );
        ViticultureSettings.TrainingMultipliers trained = multipliers(
                child(object, "trained"),
                defaults.trained()
        );
        try {
            return defaults.withInfrastructure(untrained, trained, maximumDistance);
        } catch (IllegalArgumentException exception) {
            throw invalid(source, exception.getMessage());
        }
    }

    private static ViticultureSettings.VarietySettings parseVariety(
            ResourceLocation source,
            JsonObject object,
            ViticultureSettings.VarietySettings defaults
    ) {
        try {
            JsonObject growthObject = child(object, "growth");
            ClimateProfile growthClimate = climate(
                    child(growthObject, "climate"),
                    defaults.growth().climateProfile()
            );
            VineGrowthConfig growth = new VineGrowthConfig(
                    decimal(
                            growthObject,
                            "base_growth_chance",
                            defaults.growth().baseGrowthChance()
                    ),
                    growthClimate,
                    decimal(
                            growthObject,
                            "progress_increment",
                            defaults.growth().progressIncrement()
                    )
            );

            JsonObject harvestObject = child(object, "harvest");
            ClimateProfile harvestClimate = climate(
                    child(harvestObject, "climate"),
                    growthClimate
            );
            Map<PruningLevel, PruningProfile> pruning = pruningProfiles(
                    child(harvestObject, "pruning"),
                    defaults.harvest()
            );
            GrapeHarvestConfig harvestDefaults = defaults.harvest();
            GrapeHarvestConfig harvest = new GrapeHarvestConfig(
                    decimal(harvestObject, "base_quantity", harvestDefaults.baseQuantity()),
                    decimal(
                            harvestObject,
                            "maximum_quantity",
                            harvestDefaults.maximumQuantity()
                    ),
                    decimal(harvestObject, "base_quality", harvestDefaults.baseQuality()),
                    decimal(harvestObject, "base_sugar", harvestDefaults.baseSugar()),
                    decimal(harvestObject, "base_acidity", harvestDefaults.baseAcidity()),
                    decimal(
                            harvestObject,
                            "suitability_quality_bonus",
                            harvestDefaults.suitabilityQualityBonus()
                    ),
                    decimal(
                            harvestObject,
                            "warmth_sugar_effect",
                            harvestDefaults.warmthSugarEffect()
                    ),
                    decimal(
                            harvestObject,
                            "warmth_acidity_effect",
                            harvestDefaults.warmthAcidityEffect()
                    ),
                    decimal(
                            harvestObject,
                            "trellising_quality_effect",
                            harvestDefaults.trellisingQualityEffect()
                    ),
                    harvestClimate,
                    pruning
            );
            VineVariety<ResourceId> variety = defaults.variety();
            return new ViticultureSettings.VarietySettings(variety, growth, harvest);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw invalid(source, exception.getMessage());
        }
    }

    private static ClimateProfile climate(
            JsonObject object,
            ClimateProfile defaults
    ) {
        VineEnvironment idealDefaults = defaults.idealEnvironment();
        VineEnvironment ideal = new VineEnvironment(
                decimal(
                        object,
                        "temperature_celsius",
                        idealDefaults.temperatureCelsius()
                ),
                decimal(object, "humidity", idealDefaults.humidity()),
                decimal(object, "sunlight", idealDefaults.light())
        );
        return new ClimateProfile(
                ideal,
                decimal(
                        object,
                        "temperature_tolerance",
                        defaults.temperatureTolerance()
                ),
                decimal(object, "humidity_tolerance", defaults.humidityTolerance()),
                decimal(object, "sunlight_tolerance", defaults.lightTolerance())
        );
    }

    private static Map<PruningLevel, PruningProfile> pruningProfiles(
            JsonObject object,
            GrapeHarvestConfig defaults
    ) {
        EnumMap<PruningLevel, PruningProfile> result =
                new EnumMap<>(PruningLevel.class);
        for (PruningLevel level : PruningLevel.values()) {
            JsonObject profile = child(object, level.name().toLowerCase());
            PruningProfile fallback = defaults.pruningProfile(level);
            result.put(
                    level,
                    new PruningProfile(
                            decimal(profile, "yield", fallback.yieldMultiplier()),
                            decimal(profile, "quality", fallback.qualityMultiplier())
                    )
            );
        }
        return result;
    }

    private static ViticultureSettings.TrainingMultipliers multipliers(
            JsonObject object,
            ViticultureSettings.TrainingMultipliers defaults
    ) {
        return new ViticultureSettings.TrainingMultipliers(
                decimal(object, "yield", defaults.yield()),
                decimal(object, "quality", defaults.quality())
        );
    }

    private static ResourceId parseVarietyId(
            ResourceLocation source,
            JsonObject object
    ) {
        if (object.has("variety")) {
            return ResourceId.parse(object.get("variety").getAsString());
        }
        String path = source.getPath();
        int separator = path.lastIndexOf('/');
        String fileName = separator >= 0 ? path.substring(separator + 1) : path;
        return new ResourceId(source.getNamespace(), fileName);
    }

    private static boolean isInfrastructureFile(
            ResourceLocation source,
            JsonObject object
    ) {
        if (object.has("type")
                && "settings".equalsIgnoreCase(object.get("type").getAsString())) {
            return true;
        }
        String path = source.getPath();
        return path.endsWith("/settings") || path.endsWith("/_settings")
                || "settings".equals(path) || "_settings".equals(path);
    }

    private static JsonObject child(JsonObject parent, String name) {
        if (!parent.has(name)) {
            return new JsonObject();
        }
        JsonElement value = parent.get(name);
        if (!value.isJsonObject()) {
            throw new IllegalArgumentException(name + " must be an object");
        }
        return value.getAsJsonObject();
    }

    private static double decimal(JsonObject object, String name, double fallback) {
        return object.has(name) ? object.get(name).getAsDouble() : fallback;
    }

    private static int integer(JsonObject object, String name, int fallback) {
        return object.has(name) ? object.get(name).getAsInt() : fallback;
    }

    private static IllegalArgumentException invalid(
            ResourceLocation source,
            String reason
    ) {
        return new IllegalArgumentException(
                "Invalid viticulture resource " + source + ": " + reason
        );
    }
}
