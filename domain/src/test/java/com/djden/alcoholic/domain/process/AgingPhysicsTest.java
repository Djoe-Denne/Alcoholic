package com.djden.alcoholic.domain.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.domain.vessel.BarrelHistory;
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
