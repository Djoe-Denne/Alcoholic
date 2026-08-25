package com.djden.alcoholic.domain.process;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaltExecutionStageTest {
    @Test
    void progressesThroughInternalStagesWithoutNewProcessTypes() {
        assertEquals(MaltExecutionStage.STEEPING, MaltExecutionStage.at(0.0));
        assertEquals(MaltExecutionStage.STEEPING, MaltExecutionStage.at(0.24));
        assertEquals(MaltExecutionStage.GERMINATION, MaltExecutionStage.at(0.25));
        assertEquals(MaltExecutionStage.GERMINATION, MaltExecutionStage.at(0.69));
        assertEquals(MaltExecutionStage.KILNING, MaltExecutionStage.at(0.70));
        assertEquals(MaltExecutionStage.KILNING, MaltExecutionStage.at(1.0));
        assertEquals(MaltExecutionStage.KILNING, MaltExecutionStage.at(1.4));
        assertEquals(MaltExecutionStage.STEEPING, MaltExecutionStage.at(-1.0));
    }

    @Test
    void stageWeightsSumToOne() {
        double total = 0.0;
        for (MaltExecutionStage stage : MaltExecutionStage.values()) {
            total += stage.weight();
        }
        assertEquals(1.0, total, 1e-9);
    }

    @Test
    void moistureAndKilnHeatAreStageRequirements() {
        assertTrue(MaltExecutionStage.STEEPING.requiresMoisture());
        assertTrue(MaltExecutionStage.GERMINATION.requiresMoisture());
        assertFalse(MaltExecutionStage.KILNING.requiresMoisture());
        assertFalse(MaltExecutionStage.STEEPING.requiresKilnHeat());
        assertTrue(MaltExecutionStage.KILNING.requiresKilnHeat());
    }
}
