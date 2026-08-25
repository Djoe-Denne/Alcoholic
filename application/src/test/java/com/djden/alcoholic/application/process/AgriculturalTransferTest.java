package com.djden.alcoholic.application.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.ingredient.IngredientLot;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AgriculturalTransferTest {
    private static final ResourceId SUGAR = ResourceId.parse("alcoholic:sugar");
    private static final ResourceId ACIDITY = ResourceId.parse("alcoholic:acidity");
    private static final ResourceId APPLE = ResourceId.parse("testpack:apple");

    @Test
    void volumeWeightedLotsStayDistinguishable() {
        PropertyBag highSugar = AgriculturalTransfer.combine(List.of(lot(8, 0.90, 0.20)));
        PropertyBag highAcid = AgriculturalTransfer.combine(List.of(lot(8, 0.20, 0.90)));

        assertNotEquals(highSugar.get(SUGAR), highAcid.get(SUGAR));
        assertNotEquals(highSugar.get(ACIDITY), highAcid.get(ACIDITY));
        assertEquals(0.90, (Double) highSugar.get(SUGAR).orElseThrow(), 1e-9);
        assertEquals(0.90, (Double) highAcid.get(ACIDITY).orElseThrow(), 1e-9);
    }

    @Test
    void mixesLotsByCount() {
        PropertyBag mixed = AgriculturalTransfer.combine(List.of(
                lot(10, 180.0, 1.0),
                lot(5, 150.0, 1.0)
        ));
        assertEquals(170.0, (Double) mixed.get(SUGAR).orElseThrow(), 1e-9);
    }

    private static IngredientLot lot(int count, double sugar, double acidity) {
        return new IngredientLot(
                APPLE,
                count,
                PropertyBag.empty().with(SUGAR, sugar).with(ACIDITY, acidity)
        );
    }
}
