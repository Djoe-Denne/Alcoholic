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
import com.djden.alcoholic.application.process.BoilConfig;
import com.djden.alcoholic.application.process.BoilProcessor;
import com.djden.alcoholic.application.process.BottleConfig;
import com.djden.alcoholic.application.process.BottleProcessor;
import com.djden.alcoholic.application.process.ConditionConfig;
import com.djden.alcoholic.application.process.ConditionProcessor;
import com.djden.alcoholic.application.process.FermentConfig;
import com.djden.alcoholic.application.process.FermentProcessor;
import com.djden.alcoholic.application.process.MaltConfig;
import com.djden.alcoholic.application.process.MaltProcessor;
import com.djden.alcoholic.application.process.MashConfig;
import com.djden.alcoholic.application.process.MashProcessor;
import com.djden.alcoholic.application.process.MillConfig;
import com.djden.alcoholic.application.process.MillProcessor;
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
    public static final ResourceId MALT = new ResourceId("alcoholic", "malt");
    public static final ResourceId MILL = new ResourceId("alcoholic", "mill");
    public static final ResourceId MASH = new ResourceId("alcoholic", "mash");
    public static final ResourceId BOIL = new ResourceId("alcoholic", "boil");
    public static final ResourceId CONDITION = new ResourceId("alcoholic", "condition");

    private static final String[] STUB_PROCESS_PATHS = {
            "distill",
            "infuse"
    };
    private static final String[] DOUBLE_PROPERTY_PATHS = {
            "sugar",
            "ethanol",
            "acidity",
            "tannin",
            "bitterness",
            "aroma",
            "carbonation",
            "temperature",
            "quality",
            "color",
            "roast_intensity",
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
        registerMalt(api);
        registerMill(api);
        registerMash(api, catalog);
        registerBoil(api);
        registerCondition(api);
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
        registerProcess(api, PRESS, () -> api.processes().register(PRESS, PressConfig.CODEC, new PressProcessor(catalog)));
    }

    private static void registerFerment(AlcoholicApi api) {
        registerProcess(api, FERMENT, () -> api.processes().register(FERMENT, FermentConfig.CODEC, new FermentProcessor()));
    }

    private static void registerAge(AlcoholicApi api) {
        registerProcess(api, AGE, () -> api.processes().register(AGE, AgingConfig.CODEC, new AgingProcessor()));
    }

    private static void registerBlend(AlcoholicApi api) {
        registerProcess(api, BLEND, () -> api.processes().register(BLEND, BlendConfig.CODEC, new BlendProcessor(
                PropertyMerges.from(api),
                PropertyMerges.aggregators(api)
        )));
    }

    private static void registerBottle(AlcoholicApi api) {
        registerProcess(api, BOTTLE, () -> api.processes().register(BOTTLE, BottleConfig.CODEC, new BottleProcessor()));
    }

    private static void registerMalt(AlcoholicApi api) {
        registerProcess(api, MALT, () -> api.processes().register(MALT, MaltConfig.CODEC, new MaltProcessor()));
    }

    private static void registerMill(AlcoholicApi api) {
        registerProcess(api, MILL, () -> api.processes().register(MILL, MillConfig.CODEC, new MillProcessor()));
    }

    private static void registerMash(AlcoholicApi api, Supplier<BeverageCatalog> catalog) {
        registerProcess(api, MASH, () -> api.processes().register(MASH, MashConfig.CODEC, new MashProcessor(catalog)));
    }

    private static void registerBoil(AlcoholicApi api) {
        registerProcess(api, BOIL, () -> api.processes().register(BOIL, BoilConfig.CODEC, new BoilProcessor()));
    }

    private static void registerCondition(AlcoholicApi api) {
        registerProcess(api, CONDITION, () -> api.processes().register(
                CONDITION,
                ConditionConfig.CODEC,
                new ConditionProcessor()
        ));
    }

    private static void registerStubProcess(AlcoholicApi api, String path) {
        ResourceId id = new ResourceId("alcoholic", path);
        registerProcess(api, id, () -> api.processes().register(
                id,
                DataCodecs.UNIT,
                (request, config, context) -> ProcessResult.unsupported(id)
        ));
    }

    private static void registerProcess(AlcoholicApi api, ResourceId id, Runnable register) {
        if (api.processes().contains(id)) {
            return;
        }
        try {
            register.run();
        } catch (RegistrationException exception) {
            if (!api.processes().contains(id)) {
                throw exception;
            }
        }
    }

    private static void registerDouble(AlcoholicApi api, String path, PropertyMerge merge) {
        ResourceId id = new ResourceId("alcoholic", path);
        if (api.properties().contains(id)) {
            return;
        }
        try {
            api.properties().register(LiquidProperty.of(id, Double.class, DataCodecs.DOUBLE, merge));
        } catch (RegistrationException exception) {
            if (!api.properties().contains(id)) {
                throw exception;
            }
        }
    }

    private static void registerString(AlcoholicApi api, String path, PropertyMerge merge) {
        ResourceId id = new ResourceId("alcoholic", path);
        if (api.properties().contains(id)) {
            return;
        }
        try {
            api.properties().register(LiquidProperty.of(id, String.class, DataCodecs.STRING, merge));
        } catch (RegistrationException exception) {
            if (!api.properties().contains(id)) {
                throw exception;
            }
        }
    }

    private static void registerOakVessel(AlcoholicApi api) {
        VesselProfile oak = VesselProfile.oakBarrel();
        if (api.vessels().contains(oak.id())) {
            return;
        }
        try {
            api.vessels().register(oak);
        } catch (RegistrationException exception) {
            if (!api.vessels().contains(oak.id())) {
                throw exception;
            }
        }
    }
}
