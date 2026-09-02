package com.djden.alcoholic.minecraft.viticulture;

import com.djden.alcoholic.application.viticulture.VineVarieties;
import com.djden.alcoholic.domain.viticulture.PruningLevel;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HarvestLotNbtTest {
    @Test
    void roundTripsVersionedHarvestData() {
        HarvestLotNbt.HarvestLot expected = new HarvestLotNbt.HarvestLot(
                VineVarieties.RED_GRAPE.id(),
                0.82,
                0.64,
                0.37
        );

        CompoundTag serialized = HarvestLotNbt.toTag(expected);
        HarvestLotNbt.HarvestLot lot =
                HarvestLotNbt.fromTag(serialized).orElseThrow();

        assertEquals(HarvestLotNbt.VERSION, serialized.getInt("Version"));
        assertEquals(820, serialized.getInt("Quality"));
        assertEquals(640, serialized.getInt("Sugar"));
        assertEquals(370, serialized.getInt("Acidity"));
        assertEquals(VineVarieties.RED_GRAPE.id(), lot.variety());
        assertEquals(0.82, lot.quality());
        assertEquals(0.64, lot.sugar());
        assertEquals(0.37, lot.acidity());
    }

    @Test
    void quantizesValuesToStableThousandths() {
        CompoundTag serialized = HarvestLotNbt.toTag(
                new HarvestLotNbt.HarvestLot(
                        VineVarieties.RED_GRAPE.id(),
                        0.8246,
                        0.0004,
                        0.9996
                )
        );

        HarvestLotNbt.HarvestLot lot =
                HarvestLotNbt.fromTag(serialized).orElseThrow();

        assertEquals(825, serialized.getInt("Quality"));
        assertEquals(0, serialized.getInt("Sugar"));
        assertEquals(1_000, serialized.getInt("Acidity"));
        assertEquals(0.825, lot.quality());
        assertEquals(0.0, lot.sugar());
        assertEquals(1.0, lot.acidity());
    }

    @Test
    void readsLegacyVersionOneDoubleLots() {
        CompoundTag legacy = new CompoundTag();
        legacy.putInt("Version", 1);
        legacy.putString("Variety", VineVarieties.WHITE_GRAPE.id().toString());
        legacy.putDouble("Quality", 0.7);
        legacy.putDouble("Sugar", 0.5);
        legacy.putDouble("Acidity", 0.4);

        HarvestLotNbt.HarvestLot lot =
                HarvestLotNbt.fromTag(legacy).orElseThrow();

        assertEquals(0.7, lot.quality());
        assertEquals(0.5, lot.sugar());
        assertEquals(0.4, lot.acidity());
    }

    @Test
    void differentLotsProduceDifferentNbt() {
        CompoundTag first = HarvestLotNbt.toTag(
                new HarvestLotNbt.HarvestLot(
                        VineVarieties.WHITE_GRAPE.id(),
                        0.7,
                        0.5,
                        0.4
                )
        );
        CompoundTag second = HarvestLotNbt.toTag(
                new HarvestLotNbt.HarvestLot(
                        VineVarieties.WHITE_GRAPE.id(),
                        0.8,
                        0.5,
                        0.4
                )
        );

        assertNotEquals(first, second);
        assertTrue(HarvestLotNbt.fromTag(first).isPresent());
    }

    @Test
    void differentPruningKeepsLotsApartEvenWhenNumbersMatch() {
        CompoundTag severe = HarvestLotNbt.toTag(
                new HarvestLotNbt.HarvestLot(
                        VineVarieties.RED_GRAPE.id(),
                        0.7,
                        0.5,
                        0.4,
                        PruningLevel.SEVERE
                )
        );
        CompoundTag balanced = HarvestLotNbt.toTag(
                new HarvestLotNbt.HarvestLot(
                        VineVarieties.RED_GRAPE.id(),
                        0.7,
                        0.5,
                        0.4,
                        PruningLevel.BALANCED
                )
        );

        assertNotEquals(severe, balanced);
        assertEquals(PruningLevel.SEVERE, HarvestLotNbt.fromTag(severe).orElseThrow().pruningLevel());
    }
}
