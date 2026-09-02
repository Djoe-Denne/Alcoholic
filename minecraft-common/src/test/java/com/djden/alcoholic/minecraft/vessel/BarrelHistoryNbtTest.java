package com.djden.alcoholic.minecraft.vessel;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.domain.vessel.BarrelHistory;
import com.djden.alcoholic.domain.vessel.CaskImprint;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BarrelHistoryNbtTest {
    private static final ResourceId PREVIOUS = ResourceId.parse("test:young");

    @Test
    void roundTripsCaskImprint() {
        BarrelHistory history = BarrelHistory.empty().recordEmptying(
                PREVIOUS,
                PropertyBag.empty().with(CaskImprint.ACIDITY, 0.40).with(CaskImprint.ROAST, 0.25)
        );
        BarrelHistory restored = BarrelHistoryNbt.fromTag(BarrelHistoryNbt.toTag(history));
        assertEquals(1, restored.usageCount());
        assertEquals(PREVIOUS, restored.previousContents().get(0));
        assertEquals(0.40, restored.caskImprint().get(CaskImprint.ACIDITY), 1e-3);
        assertEquals(0.25, restored.caskImprint().get(CaskImprint.ROAST), 1e-3);
    }

    @Test
    void missingImprintTagLoadsEmpty() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("UsageCount", 2);
        BarrelHistory restored = BarrelHistoryNbt.fromTag(tag);
        assertEquals(2, restored.usageCount());
        assertTrue(restored.caskImprint().isEmpty());
    }
}
