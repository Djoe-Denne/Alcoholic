package com.djden.alcoholic.addon.test;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.api.property.LiquidProperty;

/**
 * Example Java addon that registers a new process type and liquid property
 * exclusively through {@code alcoholic-api}.
 */
@PublicApi
public final class TestAddonBootstrap {
    public static final ResourceId RICE_POLISHING = ResourceId.parse("testaddon:rice_polishing");
    public static final ResourceId POLISHING_RATIO = ResourceId.parse("testaddon:polishing_ratio");

    private TestAddonBootstrap() {
    }

    public static void install(AlcoholicApi api) {
        if (!api.processes().contains(RICE_POLISHING)) {
            api.processes().register(
                    RICE_POLISHING,
                    RicePolishingConfig.CODEC,
                    (request, config, context) -> ProcessResult.unsupported(RICE_POLISHING)
            );
        }
        if (!api.properties().contains(POLISHING_RATIO)) {
            api.properties().register(LiquidProperty.of(
                    POLISHING_RATIO,
                    Double.class,
                    DataCodecs.DOUBLE
            ));
        }
    }
}
