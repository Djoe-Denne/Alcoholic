package com.djden.alcoholic.minecraft.bottle;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import com.djden.alcoholic.minecraft.process.BottleStandBlockEntity;
import com.djden.alcoholic.minecraft.process.BottleStandNetwork;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BeverageBottleModelTest {
    @Test
    void knownBeverageDefinitionsSelectTheirThreeDimensionalModel() {
        assertEquals(BeverageBottleModel.RED_WINE, BeverageBottleModel.fromDefinition(AlcoholicIds.RED_WINE));
        assertEquals(BeverageBottleModel.RED_WINE, BeverageBottleModel.fromDefinition(AlcoholicIds.YOUNG_RED_WINE));
        assertEquals(BeverageBottleModel.WHITE_WINE, BeverageBottleModel.fromDefinition(AlcoholicIds.WHITE_WINE));
        assertEquals(BeverageBottleModel.WHITE_WINE, BeverageBottleModel.fromDefinition(AlcoholicIds.YOUNG_WHITE_WINE));
        assertEquals(BeverageBottleModel.BEER, BeverageBottleModel.fromDefinition(AlcoholicIds.BEER));
    }

    @Test
    void unknownDefinitionsUseTheSafeFallbackModel() {
        assertEquals(BeverageBottleModel.FALLBACK, BeverageBottleModel.fromDefinition(ResourceId.parse("alcoholic:future_cider")));
    }

    @Test
    void standCapacityIsNineSlotsPerBlockAndFiftyFourPerNetwork() {
        assertEquals(9, BottleStandBlockEntity.SLOT_COUNT);
        assertEquals(3, BottleStandNetwork.MAX_WIDTH);
        assertEquals(2, BottleStandNetwork.MAX_HEIGHT);
        assertEquals(54, BottleStandBlockEntity.SLOT_COUNT * BottleStandNetwork.MAX_WIDTH * BottleStandNetwork.MAX_HEIGHT);
    }
}
