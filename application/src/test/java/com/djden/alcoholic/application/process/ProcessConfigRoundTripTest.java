package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessConfigRoundTripTest {
    @Test
    void maltConfigRoundTripsKilnAndTemperature() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        MaltConfig original = MaltConfig.CODEC.decode(
                loaded.catalog().process(GrainProcessHarness.MALT_PALE).orElseThrow().config()
        );
        MaltConfig restored = MaltConfig.CODEC.decode(MaltConfig.CODEC.encode(original));
        assertEquals(original.processingTicks(), restored.processingTicks());
        assertEquals(original.moistureRequirement(), restored.moistureRequirement(), 1e-9);
        assertEquals(original.kiln(), restored.kiln());
        assertEquals(original.temperature(), restored.temperature());
        assertEquals(original.inputSelector(), restored.inputSelector());
        assertEquals(original.outputItem(), restored.outputItem());
    }

    @Test
    void mashConfigRoundTripsPortsAndTemperature() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        MashConfig original = MashConfig.CODEC.decode(
                loaded.catalog().process(ResourceId.parse("alcoholic:mash_wort")).orElseThrow().config()
        );
        MashConfig restored = MashConfig.CODEC.decode(MashConfig.CODEC.encode(original));
        assertEquals(original.processingTicks(), restored.processingTicks());
        assertEquals(original.inputLiquid(), restored.inputLiquid());
        assertEquals(original.inputLiquidVolume(), restored.inputLiquidVolume(), 1e-9);
        assertEquals(original.outputLiquid(), restored.outputLiquid());
        assertEquals(original.outputVolume(), restored.outputVolume(), 1e-9);
        assertEquals(original.temperature(), restored.temperature());
        assertEquals(original.inputSelector(), restored.inputSelector());
    }

    @Test
    void boilConfigRoundTripsAdditionsAndHopProfile() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        BoilConfig original = BoilConfig.CODEC.decode(
                loaded.catalog().process(ResourceId.parse("alcoholic:boil_wort")).orElseThrow().config()
        );
        BoilConfig restored = BoilConfig.CODEC.decode(BoilConfig.CODEC.encode(original));
        assertEquals(original.processingTicks(), restored.processingTicks());
        assertEquals(original.inputLiquid(), restored.inputLiquid());
        assertEquals(original.outputLiquid(), restored.outputLiquid());
        assertEquals(original.additionSelector(), restored.additionSelector());
        assertEquals(original.additions(), restored.additions());
        assertEquals(original.hopProfile(), restored.hopProfile());
        assertEquals(original.temperature(), restored.temperature());
    }

    @Test
    void conditionConfigRoundTripsKineticsAndProperties() {
        GrainProcessHarness.Loaded loaded = GrainProcessHarness.load();
        ConditionConfig original = ConditionConfig.CODEC.decode(
                loaded.catalog().process(ResourceId.parse("alcoholic:condition_beer")).orElseThrow().config()
        );
        ConditionConfig restored = ConditionConfig.CODEC.decode(ConditionConfig.CODEC.encode(original));
        assertEquals(original.inputLiquid(), restored.inputLiquid());
        assertEquals(original.outputLiquid(), restored.outputLiquid());
        assertEquals(original.processingTicks(), restored.processingTicks());
        assertEquals(original.temperature(), restored.temperature());
        assertEquals(original.kinetics(), restored.kinetics());
        assertEquals(original.maturityProperty(), restored.maturityProperty());
        assertEquals(original.sugarProperty(), restored.sugarProperty());
        assertEquals(original.carbonationProperty(), restored.carbonationProperty());
    }
}
