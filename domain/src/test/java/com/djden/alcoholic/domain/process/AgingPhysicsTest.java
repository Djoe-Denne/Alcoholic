package com.djden.alcoholic.domain.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.domain.vessel.BarrelHistory;
import com.djden.alcoholic.domain.vessel.CaskImprint;
import com.djden.alcoholic.domain.vessel.EnvironmentProfile;
import com.djden.alcoholic.domain.vessel.VesselProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgingPhysicsTest {
    private static final ResourceId SOURCE = ResourceId.parse("test:young");
    private static final ResourceId FINISHED = ResourceId.parse("test:finished");
    private static final ResourceId MATURITY = ResourceId.parse("alcoholic:maturity");
    private static final ResourceId WOOD = ResourceId.parse("alcoholic:wood_exposure");
    private static final ResourceId OXIDATION = ResourceId.parse("alcoholic:oxidation_exposure");

    @Test
    void catchUpMatchesRepeatedSingleTicks() {
        LiquidBatch start = batch(0.0);
        AgingKinetics kinetics = new AgingKinetics(0.01, 0.01, 0.002, 1.0);
        AgingState once = step(start, kinetics, VesselProfile.oakBarrel(), EnvironmentProfile.temperateCellar(), 20.0);
        AgingState repeated = new AgingState(start, false);
        for (int tick = 0; tick < 20; tick++) {
            repeated = step(
                    repeated.batch(),
                    kinetics,
                    VesselProfile.oakBarrel(),
                    EnvironmentProfile.temperateCellar(),
                    1.0
            );
        }
        assertEquals(once.batch().number(MATURITY, 0.0), repeated.batch().number(MATURITY, 0.0), 1e-9);
        assertEquals(once.batch().number(WOOD, 0.0), repeated.batch().number(WOOD, 0.0), 1e-9);
        assertEquals(once.batch().number(OXIDATION, 0.0), repeated.batch().number(OXIDATION, 0.0), 1e-9);
        assertEquals(20.0, once.batch().batchProvenance().totalAgingTime(), 1e-9);
    }

    @Test
    void usedVesselAgesFasterThanFresh() {
        AgingKinetics kinetics = new AgingKinetics(0.01, 0.01, 0.002, 1.0);
        VesselProfile used = VesselProfile.oakBarrel().withHistory(
                new BarrelHistory(1, List.of(SOURCE), Optional.empty(), Optional.empty(), Optional.empty())
        );
        double fresh = step(batch(0.0), kinetics, VesselProfile.oakBarrel(), EnvironmentProfile.temperateCellar(), 10.0)
                .batch()
                .number(MATURITY, 0.0);
        double seasoned = step(batch(0.0), kinetics, used, EnvironmentProfile.temperateCellar(), 10.0)
                .batch()
                .number(MATURITY, 0.0);
        assertTrue(seasoned > fresh);
        assertEquals(fresh * 1.15, seasoned, 1e-9);
    }

    @Test
    void shelteredEnvironmentOxidizesLessThanExposed() {
        AgingKinetics kinetics = new AgingKinetics(0.0, 0.0, 0.01, 1.0);
        double sheltered = step(
                batch(0.0),
                kinetics,
                VesselProfile.oakBarrel(),
                EnvironmentProfile.temperateCellar(),
                10.0
        ).batch().number(OXIDATION, 0.0);
        double exposed = step(
                batch(0.0),
                kinetics,
                VesselProfile.oakBarrel(),
                EnvironmentProfile.exposed(14.0),
                10.0
        ).batch().number(OXIDATION, 0.0);
        assertTrue(exposed > sheltered);
    }

    @Test
    void completionRenamesWhenOutputIsPresent() {
        AgingKinetics kinetics = new AgingKinetics(1.0, 0.0, 0.0, 1.0);
        AgingState done = AgingPhysics.step(
                batch(0.0),
                kinetics,
                new TemperatureProfile(
                        new TemperatureBand(10.0, 16.0),
                        new TemperatureBand(0.0, 36.0),
                        new TemperatureBand(-20.0, 40.0)
                ),
                VesselProfile.oakBarrel(),
                EnvironmentProfile.temperateCellar(),
                MATURITY,
                WOOD,
                OXIDATION,
                Optional.of(FINISHED),
                2.0
        );
        assertTrue(done.complete());
        assertEquals(FINISHED, done.batch().baseLiquid().orElseThrow());
    }

    @Test
    void freshVesselDoesNotLeakImprintAxes() {
        AgingKinetics kinetics = new AgingKinetics(0.01, 0.0, 0.0, 1.0);
        LiquidBatch aged = step(
                batch(0.0),
                kinetics,
                VesselProfile.oakBarrel(),
                EnvironmentProfile.temperateCellar(),
                10.0
        ).batch();
        assertEquals(0.0, aged.number(CaskImprint.ACIDITY, 0.0), 1e-9);
        assertEquals(0.0, aged.number(CaskImprint.SUGAR, 0.0), 1e-9);
    }

    @Test
    void imprintLeaksAcidityButNotAbsentSugar() {
        AgingKinetics kinetics = new AgingKinetics(0.05, 0.0, 0.0, 1.0);
        VesselProfile stained = vesselWithImprint(PropertyBag.empty().with(CaskImprint.ACIDITY, 0.40));
        LiquidBatch aged = step(batch(0.0), kinetics, stained, EnvironmentProfile.temperateCellar(), 10.0).batch();
        assertTrue(aged.number(CaskImprint.ACIDITY, 0.0) > 0.0);
        assertEquals(0.0, aged.number(CaskImprint.SUGAR, 0.0), 1e-9);
    }

    @Test
    void imprintDoesNotLowerAnAxisAlreadyHigher() {
        AgingKinetics kinetics = new AgingKinetics(0.05, 0.0, 0.0, 1.0);
        VesselProfile stained = vesselWithImprint(PropertyBag.empty().with(CaskImprint.ACIDITY, 0.20));
        LiquidBatch start = batch(0.0).withProperty(CaskImprint.ACIDITY, 0.50);
        LiquidBatch aged = step(start, kinetics, stained, EnvironmentProfile.temperateCellar(), 10.0).batch();
        assertEquals(0.50, aged.number(CaskImprint.ACIDITY, 0.0), 1e-9);
    }

    @Test
    void imprintCatchUpMatchesRepeatedSingleTicks() {
        AgingKinetics kinetics = new AgingKinetics(0.01, 0.0, 0.0, 1.0);
        VesselProfile stained = vesselWithImprint(PropertyBag.empty().with(CaskImprint.ACIDITY, 0.40));
        LiquidBatch start = batch(0.0);
        double once = step(start, kinetics, stained, EnvironmentProfile.temperateCellar(), 20.0)
                .batch()
                .number(CaskImprint.ACIDITY, 0.0);
        LiquidBatch repeated = start;
        for (int tick = 0; tick < 20; tick++) {
            repeated = step(repeated, kinetics, stained, EnvironmentProfile.temperateCellar(), 1.0).batch();
        }
        assertEquals(once, repeated.number(CaskImprint.ACIDITY, 0.0), 1e-9);
    }

    @Test
    void completedBatchKeepsLeakingTowardEquilibrium() {
        AgingKinetics kinetics = new AgingKinetics(0.05, 0.0, 0.0, 1.0);
        VesselProfile stained = vesselWithImprint(PropertyBag.empty().with(CaskImprint.ACIDITY, 0.40));
        LiquidBatch aged = step(batch(1.0), kinetics, stained, EnvironmentProfile.temperateCellar(), 20.0).batch();
        assertTrue(aged.number(CaskImprint.ACIDITY, 0.0) > 0.0);
        assertTrue(aged.number(CaskImprint.ACIDITY, 0.0) <= 0.40 + 1e-9);
    }

    @Test
    void leakContinuesAfterCompletionUntilCapped() {
        AgingKinetics kinetics = new AgingKinetics(0.01, 0.0, 0.0, 1.0);
        VesselProfile stained = vesselWithImprint(PropertyBag.empty().with(CaskImprint.ACIDITY, 0.40));
        AgingState first = step(batch(1.0), kinetics, stained, EnvironmentProfile.temperateCellar(), 20.0);
        double more = step(first.batch(), kinetics, stained, EnvironmentProfile.temperateCellar(), 20.0)
                .batch()
                .number(CaskImprint.ACIDITY, 0.0);
        assertTrue(first.batch().number(CaskImprint.ACIDITY, 0.0) > 0.0);
        assertTrue(more > first.batch().number(CaskImprint.ACIDITY, 0.0));
        assertTrue(more <= 0.40 + 1e-9);
    }

    @Test
    void secondEmptyingLeaksLessThanFirst() {
        AgingKinetics kinetics = new AgingKinetics(0.05, 0.0, 0.0, 1.0);
        BarrelHistory first = BarrelHistory.empty().recordEmptying(
                SOURCE,
                PropertyBag.empty().with(CaskImprint.ACIDITY, 0.40)
        );
        BarrelHistory second = first.recordEmptying(
                FINISHED,
                PropertyBag.empty().with(CaskImprint.ACIDITY, 0.40)
        );
        double firstFill = step(
                batch(0.0),
                kinetics,
                VesselProfile.oakBarrel().withHistory(first),
                EnvironmentProfile.temperateCellar(),
                10.0
        ).batch().number(CaskImprint.ACIDITY, 0.0);
        double refill = step(
                batch(0.0),
                kinetics,
                VesselProfile.oakBarrel().withHistory(second),
                EnvironmentProfile.temperateCellar(),
                10.0
        ).batch().number(CaskImprint.ACIDITY, 0.0);
        assertTrue(refill < firstFill);
    }

    private static VesselProfile vesselWithImprint(PropertyBag imprint) {
        return VesselProfile.oakBarrel().withHistory(
                new BarrelHistory(1, List.of(SOURCE), Optional.empty(), Optional.empty(), Optional.empty(), imprint)
        );
    }

    private static AgingState step(
            LiquidBatch batch,
            AgingKinetics kinetics,
            VesselProfile vessel,
            EnvironmentProfile environment,
            double delta
    ) {
        return AgingPhysics.step(
                batch,
                kinetics,
                new TemperatureProfile(
                        new TemperatureBand(10.0, 16.0),
                        new TemperatureBand(0.0, 36.0),
                        new TemperatureBand(-20.0, 40.0)
                ),
                vessel,
                environment,
                MATURITY,
                WOOD,
                OXIDATION,
                Optional.empty(),
                delta
        );
    }

    private static LiquidBatch batch(double maturity) {
        return LiquidBatch.of(
                SOURCE,
                1000,
                PropertyBag.empty()
                        .with(MATURITY, maturity)
                        .with(WOOD, 0.0)
                        .with(OXIDATION, 0.0)
        );
    }
}
