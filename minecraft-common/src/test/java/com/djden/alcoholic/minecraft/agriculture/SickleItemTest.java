package com.djden.alcoholic.minecraft.agriculture;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SickleItemTest {
    @Test
    void fortuneZeroKeepsTheBaseCount() {
        assertEquals(3, SickleHarvest.fortuneAdjustedCount(3, 0, bound -> 0));
    }

    @Test
    void fortuneAddsAtMostTheEnchantmentLevel() {
        Random random = new Random(42L);
        for (int i = 0; i < 80; i++) {
            int count = SickleHarvest.fortuneAdjustedCount(3, 3, random::nextInt);
            assertTrue(count >= 3 && count <= 6, "Fortune III left the range: " + count);
        }
    }

    @Test
    void fortuneThreeCanExceedTheBaseCount() {
        Random random = new Random(1L);
        boolean sawBonus = false;
        for (int i = 0; i < 40; i++) {
            if (SickleHarvest.fortuneAdjustedCount(3, 3, random::nextInt) > 3) {
                sawBonus = true;
                break;
            }
        }
        assertTrue(sawBonus, "Fortune III never added a hop");
    }
}
