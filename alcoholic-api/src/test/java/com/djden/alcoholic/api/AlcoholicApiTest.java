package com.djden.alcoholic.api;

import com.djden.alcoholic.api.data.DataCodecs;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.api.data.JsonDataParser;
import com.djden.alcoholic.api.process.ProcessExecutor;
import com.djden.alcoholic.api.process.ProcessResult;
import com.djden.alcoholic.api.registry.RegistrationException;
import com.djden.alcoholic.api.vessel.VesselProfileView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlcoholicApiTest {
    @Test
    void freezesProcessAndPropertyRegistration() {
        AlcoholicApi api = AlcoholicApi.create();
        ResourceId process = ResourceId.parse("addon:custom_process");
        api.processes().register(process, DataCodecs.UNIT, (request, config, context) ->
                ProcessResult.unsupported(process)
        );
        api.freeze();

        assertTrue(api.isFrozen());
        assertThrows(
                RegistrationException.class,
                () -> api.properties().register(
                        ResourceId.parse("addon:custom_property"),
                        Double.class,
                        DataCodecs.DOUBLE
                )
        );
        assertThrows(
                RegistrationException.class,
                () -> api.vessels().register(new VesselProfileView() {
                    @Override
                    public ResourceId id() {
                        return ResourceId.parse("addon:vessel");
                    }

                    @Override
                    public ResourceId material() {
                        return ResourceId.parse("addon:clay");
                    }

                    @Override
                    public int capacityMillibuckets() {
                        return 1000;
                    }

                    @Override
                    public java.util.Set<ResourceId> processCapabilities() {
                        return java.util.Set.of();
                    }

                    @Override
                    public double permeability() {
                        return 0.1;
                    }

                    @Override
                    public double woodExtractionMultiplier() {
                        return 1.0;
                    }

                    @Override
                    public double oxidationMultiplier() {
                        return 0.2;
                    }
                })
        );
    }

    @Test
    void processExecutorTargetsCapabilitiesNotIdentities() {
        ResourceId process = ResourceId.parse("alcoholic:press");
        ProcessExecutor executor = () -> process;
        assertTrue(executor.supports(process));
        assertFalse(executor.supports(ResourceId.parse("addon:other")));
    }

    @Test
    void jsonParserRoundTripsNestedObjects() {
        DataNode node = JsonDataParser.parse("""
                {"name":"press","inputs":["fruit"],"count":2,"ok":true}
                """);
        assertEquals("press", node.asObject("$").require("name", "$").asString("$/name"));
        assertEquals(2, node.asObject("$").require("count", "$").asNumber("$/count").intValue());
        assertTrue(node.asObject("$").require("ok", "$").asBoolean("$/ok"));
        assertEquals(1, node.asObject("$").require("inputs", "$").asList("$/inputs").size());
    }
}
