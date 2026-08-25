package com.djden.alcoholic.application.beverage.builtin;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.api.property.LiquidProperty;
import com.djden.alcoholic.api.property.PropertyMerge;
import com.djden.alcoholic.api.registry.RegistrationException;
import com.djden.alcoholic.application.beverage.BeverageCatalog;
import com.djden.alcoholic.application.process.AgingConfig;
import com.djden.alcoholic.application.process.AgingProcessor;
import com.djden.alcoholic.application.process.BlendConfig;
import com.djden.alcoholic.application.process.BlendProcessor;
import com.djden.alcoholic.application.process.BottleConfig;
import com.djden.alcoholic.application.process.BottleProcessor;
import com.djden.alcoholic.application.process.FermentConfig;
import com.djden.alcoholic.application.process.FermentProcessor;
import com.djden.alcoholic.application.process.PressConfig;
import com.djden.alcoholic.application.process.PressProcessor;
import com.djden.alcoholic.application.process.PropertyMerges;
import com.djden.alcoholic.domain.vessel.VesselProfile;

import java.util.function.Supplier;

/**
 * Content bootstrap for process capabilities and typed properties shipped by Alcoholic.
 * This is not generic engine logic: it only registers data, never branches on a drink family.
 */
public final class BuiltinRegistrations {
    public static final ResourceId PRESS = new ResourceId("alcoholic", "press");
    public static final ResourceId FERMENT = new ResourceId("alcoholic", "ferment");
    public static final ResourceId AGE = new ResourceId("alcoholic", "age");
    public static final ResourceId BLEND = new ResourceId("alcoholic", "blend");
    public static final ResourceId BOTTLE = new ResourceId("alcoholic", "bottle");

    private static final String[] STUB_PROCESS_PATHS = {
            "mill",
            "malt",
            "mash",
            "boil",
            "distill",
            "infuse"
    };
    private static final String[] DOUBLE_PROPERTY_PATHS = {
            "sugar",
            "ethanol",
            "acidity",
            "tannin",
            "bitterness",
            "carbonation",
            "temperature",
            "quality",
            "fermentation_stress",
            "wood_exposure",
            "oxidation_exposure"
    };

    private BuiltinRegistrations() {
    }

    public static void install(AlcoholicApi api) {
        install(api, BeverageCatalog::empty);
    }

    public static void install(AlcoholicApi api, Supplier<BeverageCatalog> catalog) {
        registerPress(api, catalog);
        registerFerment(api);
        registerAge(api);
        registerBlend(api);
        registerBottle(api);
        for (String path : STUB_PROCESS_PATHS) {
            registerStubProcess(api, path);
        }
        for (String path : DOUBLE_PROPERTY_PATHS) {
            registerDouble(api, path, PropertyMerge.WEIGHTED_AVERAGE);
        }
        registerDouble(api, "maturity", PropertyMerge.WEIGHTED_AVERAGE);
        registerString(api, "variety", PropertyMerge.MATCH_OR_BLENDED);
        registerOakVessel(api);
    }

    private static void registerPress(AlcoholicApi api, Supplier<BeverageCatalog> catalog) {
        if (api.processes().contains(PRESS)) {
            return;
        }
        try {
            api.processes().register(PRESS, PressConfig.CODEC, new PressProcessor(catalog));
        } catch (RegistrationException ignored) {
            // A racing installer already registered the same capability.
        }
    }

    private static void registerFerment(AlcoholicApi api) {
        if (api.processes().contains(FERMENT)) {
            return;
        }
        try {
            api.processes().register(FERMENT, FermentConfig.CODEC, new FermentProcessor());
        } catch (RegistrationException ignored) {
            // A racing installer already registered the same capability.
        }
    }

    private static void registerAge(AlcoholicApi api) {
        if (api.processes().contains(AGE)) {
            return;
        }
        try {
            api.processes().register(AGE, AgingConfig.CODEC, new AgingProcessor());
        } catch (RegistrationException ignored) {
            // A racing installer already registered the same capability.
        }
    }

    private static void registerBlend(AlcoholicApi api) {
        if (api.processes().contains(BLEND)) {
            return;
        }
        try {
            api.processes().register(BLEND, BlendConfig.CODEC, new BlendProcessor(
                    PropertyMerges.from(api),
                    PropertyMerges.aggregators(api)
            ));
        } catch (RegistrationException ignored) {
            // A racing installer already registered the same capability.
        }
    }

    private static void registerBottle(AlcoholicApi api) {
        if (api.processes().contains(BOTTLE)) {
            return;
        }
        try {
            api.processes().register(BOTTLE, BottleConfig.CODEC, new BottleProcessor());
        } catch (RegistrationException ignored) {
            // A racing installer already registered the same capability.
        }
    }

    private static void registerStubProcess(AlcoholicApi api, String path) {
        ResourceId id = new ResourceId("alcoholic", path);
        if (api.processes().contains(id)) {
            return;
        }
        try {
            api.processes().register(id, DataCodecs.UNIT, (request, config, context) -> ProcessResult.unsupported(id));
        } catch (RegistrationException ignored) {
            // A racing installer already registered the same capability.
        }
    }

    private static void registerDouble(AlcoholicApi api, String path, PropertyMerge merge) {
        ResourceId id = new ResourceId("alcoholic", path);
        if (api.properties().contains(id)) {
            return;
        }
        try {
            api.properties().register(LiquidProperty.of(id, Double.class, DataCodecs.DOUBLE, merge));
        } catch (RegistrationException ignored) {
            // A racing installer already registered the same property.
        }
    }

    private static void registerString(AlcoholicApi api, String path, PropertyMerge merge) {
        ResourceId id = new ResourceId("alcoholic", path);
        if (api.properties().contains(id)) {
            return;
        }
        try {
            api.properties().register(LiquidProperty.of(id, String.class, DataCodecs.STRING, merge));
        } catch (RegistrationException ignored) {
            // A racing installer already registered the same property.
        }
    }

    private static void registerOakVessel(AlcoholicApi api) {
        VesselProfile oak = VesselProfile.oakBarrel();
        if (api.vessels().contains(oak.id())) {
            return;
        }
        try {
            api.vessels().register(oak);
        } catch (RegistrationException ignored) {
            // A racing installer already registered the same vessel.
        }
    }
}
