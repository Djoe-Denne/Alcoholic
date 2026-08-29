package com.djden.alcoholic.domain.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.process.ExecutorModifiers;
import com.djden.alcoholic.domain.liquid.BatchProvenance;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QualityProfileTest {
    private static final ResourceId MUST = ResourceId.parse("alcoholic:red_grape_must");

    @Test
    void ethanolDoesNotRaiseTheSummary() {
        LiquidBatch weak = batch(0.70, 0.10, 0.0, 0.0);
        LiquidBatch strong = batch(0.70, 0.10, 0.55, 0.0);
        QualityProfile a = QualityProfile.derive(weak);
        QualityProfile b = QualityProfile.derive(strong);
        assertEquals(a.summary(), b.summary(), 1e-9);
        assertEquals(a.complexity(), b.complexity(), 1e-9);
    }

    @Test
    void industrialCapClampsComplexityAndSummary() {
        LiquidBatch excellent = batch(0.90, 0.20, 0.12, 0.0);
        QualityProfile artisanal = QualityProfile.derive(excellent, ExecutorModifiers.artisanal());
        QualityProfile industrial = QualityProfile.derive(excellent, ExecutorModifiers.industrialVat());
        assertTrue(artisanal.complexity() > 0.70);
        assertEquals(0.55, industrial.complexity(), 1e-9);
        assertTrue(industrial.summary() <= 0.55 + 1e-9);
        assertTrue(industrial.defects() >= ExecutorModifiers.industrialVat().purityFloor() - 1e-9);
    }

    @Test
    void stampedCapSurvivesIdentityDerivation() {
        LiquidBatch stamped = QualityProfile.stampCap(
                batch(0.90, 0.20, 0.12, 0.0),
                ExecutorModifiers.industrialPress()
        );
        QualityProfile profile = QualityProfile.derive(stamped, ExecutorModifiers.identity());
        assertTrue(profile.complexity() <= 0.55 + 1e-9);
        assertTrue(profile.summary() <= 0.55 + 1e-9);
        assertTrue(profile.defects() >= ExecutorModifiers.industrialPress().purityFloor() - 1e-9);
        assertEquals(0.15, stamped.number(QualityProfile.PURITY_FLOOR, 0.0), 1e-9);
    }

    @Test
    void oxygenCurveShapesDefectsAndComplexity() {
        LiquidBatch reductive = aged(0.45, 0.01, 8_000);
        LiquidBatch microOx = aged(0.45, 0.18, 8_000);
        LiquidBatch oxidized = aged(0.45, 0.80, 8_000);
        QualityProfile low = QualityProfile.derive(reductive);
        QualityProfile mid = QualityProfile.derive(microOx);
        QualityProfile high = QualityProfile.derive(oxidized);
        assertTrue(low.defects() > 0.0);
        assertTrue(mid.complexity() > low.complexity());
        assertTrue(high.defects() > mid.defects());
    }

    @Test
    void fermentationStressRaisesDefects() {
        LiquidBatch clean = batch(0.60, 0.10, 0.10, 0.0);
        LiquidBatch stressed = new LiquidBatch(
                clean.identity(),
                clean.baseLiquid(),
                clean.volume(),
                clean.properties(),
                BatchProvenance.empty().withSummaries(0.40, 0.0, 0.0, 0.0)
        );
        assertTrue(QualityProfile.derive(stressed).defects() > QualityProfile.derive(clean).defects());
        assertTrue(QualityProfile.derive(stressed).purity() < QualityProfile.derive(clean).purity());
    }

    @Test
    void fermentationStressPropertyIsReadWithoutProvenance() {
        LiquidBatch clean = batch(0.60, 0.10, 0.10, 0.0);
        LiquidBatch stressed = clean.withProperty(QualityProfile.FERMENTATION_STRESS, 0.40);
        assertTrue(QualityProfile.derive(stressed).defects() > QualityProfile.derive(clean).defects());
    }

    @Test
    void tanninRaisesComplexityOnWine() {
        LiquidBatch plain = batch(0.70, 0.45, 0.12, 0.20);
        LiquidBatch tannic = plain.withProperty(QualityProfile.TANNIN, 0.50);
        assertTrue(QualityProfile.derive(tannic).complexity() > QualityProfile.derive(plain).complexity());
    }

    @Test
    void wineBalanceDoesNotDiluteMissingHopAxes() {
        LiquidBatch wine = batch(0.70, 0.45, 0.12, 0.20);
        QualityProfile profile = QualityProfile.derive(wine);
        double sugarAcid = 1.0 - (Math.abs(0.50 - 0.35) + Math.abs(0.45 - 0.45)) / 2.0;
        assertEquals(sugarAcid, profile.balance(), 1e-9);
    }

    @Test
    void craftCapClampsBelowArtisanal() {
        LiquidBatch excellent = batch(0.90, 0.20, 0.12, 0.0);
        QualityProfile craft = QualityProfile.derive(excellent, ExecutorModifiers.craftVat());
        QualityProfile artisanal = QualityProfile.derive(excellent, ExecutorModifiers.artisanal());
        assertTrue(craft.complexity() <= 0.82 + 1e-9);
        assertTrue(artisanal.complexity() > craft.complexity());
    }

    private static LiquidBatch aged(double harvest, double oxidation, double agingTicks) {
        LiquidBatch clean = batch(harvest, 0.20, 0.12, 0.40);
        return new LiquidBatch(
                clean.identity(),
                clean.baseLiquid(),
                clean.volume(),
                clean.properties().with(QualityProfile.OXIDATION, oxidation),
                BatchProvenance.empty().withSummaries(0.0, agingTicks, 0.35, oxidation)
        );
    }

    private static LiquidBatch batch(double harvest, double acid, double ethanol, double maturity) {
        return LiquidBatch.of(
                MUST,
                1000,
                PropertyBag.empty()
                        .with(QualityProfile.HARVEST_QUALITY, harvest)
                        .with(QualityProfile.ACIDITY, acid)
                        .with(QualityProfile.SUGAR, 0.50)
                        .with(ResourceId.parse("alcoholic:ethanol"), ethanol)
                        .with(QualityProfile.MATURITY, maturity)
        );
    }
}
