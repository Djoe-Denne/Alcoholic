package com.djden.alcoholic.forge.jei;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.minecraft.content.AlcoholicIds;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JeiIngredientGuidesTest {
    @Test
    void grapevineGuideCoversFruitCuttingsAndShears() {
        JeiIngredientGuides.Guide guide = guideWith(AlcoholicIds.RED_GRAPE_CUTTING);
        assertTrue(guide.items().contains(AlcoholicIds.WHITE_GRAPE_CUTTING));
        assertTrue(guide.items().contains(AlcoholicIds.RED_GRAPES));
        assertTrue(guide.items().contains(AlcoholicIds.WHITE_GRAPES));
        assertTrue(guide.items().contains(AlcoholicIds.PRUNING_SHEARS));
        assertEquals(
                List.of(
                        "jei.alcoholic.info.grapevine.find",
                        "jei.alcoholic.info.grapevine.grow",
                        "jei.alcoholic.info.grapevine.harvest"
                ),
                guide.keys()
        );
    }

    @Test
    void hopsGuideCoversConeAndRhizome() {
        JeiIngredientGuides.Guide guide = guideWith(AlcoholicIds.HOP_RHIZOME);
        assertTrue(guide.items().contains(AlcoholicIds.HOPS));
        assertEquals(
                List.of(
                        "jei.alcoholic.info.hops.find",
                        "jei.alcoholic.info.hops.grow",
                        "jei.alcoholic.info.hops.harvest"
                ),
                guide.keys()
        );
    }

    @Test
    void barleyGuideCoversGrainAndSeeds() {
        JeiIngredientGuides.Guide guide = guideWith(AlcoholicIds.BARLEY);
        assertTrue(guide.items().contains(AlcoholicIds.BARLEY_SEEDS));
        assertEquals(
                List.of(
                        "jei.alcoholic.info.barley.find",
                        "jei.alcoholic.info.barley.grow"
                ),
                guide.keys()
        );
    }

    @Test
    void trellisGuideIsSharedByPostsAndSpool() {
        JeiIngredientGuides.Guide guide = guideWith(AlcoholicIds.TRELLIS_SPOOL);
        assertTrue(guide.items().contains(AlcoholicIds.VINEYARD_POST));
        assertTrue(guide.items().contains(AlcoholicIds.END_POST));
        assertEquals(List.of("jei.alcoholic.info.trellis"), guide.keys());
    }

    private static JeiIngredientGuides.Guide guideWith(ResourceId id) {
        return JeiIngredientGuides.all().stream()
                .filter(guide -> guide.items().contains(id))
                .findFirst()
                .orElseThrow();
    }
}
