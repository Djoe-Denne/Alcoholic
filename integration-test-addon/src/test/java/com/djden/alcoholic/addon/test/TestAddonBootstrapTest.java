package com.djden.alcoholic.addon.test;

import com.djden.alcoholic.api.AlcoholicApi;
import com.djden.alcoholic.api.registry.RegistrationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestAddonBootstrapTest {
    @Test
    void registersProcessAndPropertyThroughPublicApiOnly() {
        AlcoholicApi api = AlcoholicApi.create();
        TestAddonBootstrap.install(api);
        api.freeze();

        assertTrue(api.processes().contains(TestAddonBootstrap.RICE_POLISHING));
        assertTrue(api.properties().contains(TestAddonBootstrap.POLISHING_RATIO));
        assertThrows(
                RegistrationException.class,
                () -> api.processes().register(
                        com.djden.alcoholic.api.ResourceId.parse("testaddon:late"),
                        com.djden.alcoholic.api.data.DataCodecs.UNIT,
                        (request, config, context) ->
                                com.djden.alcoholic.api.process.ProcessResult.unsupported(
                                        com.djden.alcoholic.api.ResourceId.parse("testaddon:late")
                                )
                )
        );
    }
}
