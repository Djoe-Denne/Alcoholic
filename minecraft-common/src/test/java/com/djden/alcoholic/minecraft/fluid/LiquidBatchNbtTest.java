package com.djden.alcoholic.minecraft.fluid;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.BatchProvenance;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiquidBatchNbtTest {
    private static final ResourceId MUST = ResourceId.parse("test:must");
    private static final ResourceId SUGAR = ResourceId.parse("alcoholic:sugar");
    private static final ResourceId ORIGIN = ResourceId.parse("test:variety_a");

    @Test
    void writesVersionTwoAndRoundTripsProvenance() {
        LiquidBatch batch = LiquidBatch.of(
                MUST,
                1250,
                PropertyBag.empty().with(SUGAR, 0.42),
                BatchProvenance.ofOrigin(ORIGIN, 1.0)
        );
        CompoundTag tag = LiquidBatchNbt.toTag(batch);
        assertEquals(2, tag.getInt("Version"));
        LiquidBatch restored = LiquidBatchNbt.fromTag(tag).orElseThrow();
        assertEquals(MUST, restored.baseLiquid().orElseThrow());
        assertEquals(1250.0, restored.volume(), 1e-9);
        assertEquals(0.42, restored.number(SUGAR, 0.0), 1e-3);
        assertEquals(1.0, restored.batchProvenance().originComposition().get(ORIGIN), 1e-3);
    }

    @Test
    void migratesVersionOneToEmptyProvenance() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Version", 1);
        tag.putString("Definition", MUST.toString());
        tag.putDouble("Volume", 500);
        CompoundTag properties = new CompoundTag();
        properties.putInt(SUGAR.toString(), 250);
        tag.put("Properties", properties);

        LiquidBatch restored = LiquidBatchNbt.fromTag(tag).orElseThrow();
        assertEquals(MUST, restored.baseLiquid().orElseThrow());
        assertTrue(restored.batchProvenance().originComposition().isEmpty());
        assertEquals(0.25, restored.number(SUGAR, 0.0), 1e-9);
    }

    @Test
    void unknownVersionIsRejected() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Version", 99);
        tag.putString("Definition", MUST.toString());
        assertTrue(LiquidBatchNbt.fromTag(tag).isEmpty());
    }
}
