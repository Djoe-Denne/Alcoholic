package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.DataNode;
import com.djden.alcoholic.domain.process.AgingKinetics;
import com.djden.alcoholic.domain.process.TemperatureProfile;
import com.djden.alcoholic.domain.vessel.CaskImprint;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgingConfigImprintTest {
    @Test
    void omittedImprintPropertiesKeepDefaults() {
        AgingConfig config = AgingConfig.CODEC.decode(DataNode.objectBuilder().build(), "age");
        assertEquals(CaskImprint.defaultProperties(), config.imprintProperties());
        assertEquals(CaskImprint.DEFAULT_TRANSFER, config.imprintTransfer(), 1e-9);
    }

    @Test
    void emptyImprintPropertiesDisableAxes() {
        DataNode node = DataNode.objectBuilder()
                .put("imprint_properties", DataNode.list(java.util.List.of()))
                .build();
        AgingConfig config = AgingConfig.CODEC.decode(node, "age");
        assertTrue(config.imprintProperties().isEmpty());
    }

    @Test
    void encodeWritesEmptyAxesAndOmitsDefaults() {
        AgingConfig emptyAxes = new AgingConfig(
                Optional.empty(),
                Optional.empty(),
                TemperatureProfile.fermentationDefault(),
                AgingKinetics.simplified(),
                ResourceId.parse("alcoholic:maturity"),
                ResourceId.parse("alcoholic:wood_exposure"),
                ResourceId.parse("alcoholic:oxidation_exposure"),
                CaskImprint.DEFAULT_TRANSFER,
                Set.of()
        );
        DataNode.ObjectNode encodedEmpty = AgingConfig.CODEC.encode(emptyAxes).asObject("age");
        assertTrue(encodedEmpty.has("imprint_properties"));
        AgingConfig roundTrip = AgingConfig.CODEC.decode(AgingConfig.CODEC.encode(emptyAxes), "age");
        assertTrue(roundTrip.imprintProperties().isEmpty());

        DataNode.ObjectNode encodedDefaults = AgingConfig.CODEC.encode(AgingConfig.incomplete()).asObject("age");
        assertFalse(encodedDefaults.has("imprint_properties"));
    }
}
