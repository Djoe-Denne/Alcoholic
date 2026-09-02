package com.djden.alcoholic.domain.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.vessel.BarrelHistoryView;
import com.djden.alcoholic.domain.liquid.BatchProvenance;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.liquid.PropertyBag;
import com.djden.alcoholic.domain.vessel.BarrelHistory;
import com.djden.alcoholic.domain.vessel.CaskImprint;
import com.djden.alcoholic.domain.vessel.EnvironmentProfile;
import com.djden.alcoholic.domain.vessel.VesselProfile;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Progressive property evolution for {@code alcoholic:age}. Beverage identity is not consulted.
 * Oxidation is accumulated linearly; {@link OxygenCurve} interprets that exposure when
 * deriving {@link QualityProfile}.
 */
public final class AgingPhysics {
    private AgingPhysics() {
    }

    public static AgingState step(
            LiquidBatch batch,
            AgingKinetics kinetics,
            TemperatureProfile temperature,
            VesselProfile vessel,
            EnvironmentProfile environment,
            ResourceId maturityProperty,
            ResourceId woodProperty,
            ResourceId oxidationProperty,
            Optional<ResourceId> outputLiquid,
            double deltaTicks
    ) {
        return step(
                batch,
                kinetics,
                temperature,
                vessel,
                environment,
                maturityProperty,
                woodProperty,
                oxidationProperty,
                outputLiquid,
                deltaTicks,
                CaskImprint.DEFAULT_TRANSFER,
                CaskImprint.defaultProperties()
        );
    }

    public static AgingState step(
            LiquidBatch batch,
            AgingKinetics kinetics,
            TemperatureProfile temperature,
            VesselProfile vessel,
            EnvironmentProfile environment,
            ResourceId maturityProperty,
            ResourceId woodProperty,
            ResourceId oxidationProperty,
            Optional<ResourceId> outputLiquid,
            double deltaTicks,
            double imprintTransfer,
            Set<ResourceId> imprintProperties
    ) {
        Objects.requireNonNull(batch, "batch");
        Objects.requireNonNull(kinetics, "kinetics");
        Objects.requireNonNull(temperature, "temperature");
        Objects.requireNonNull(vessel, "vessel");
        Objects.requireNonNull(environment, "environment");
        Objects.requireNonNull(maturityProperty, "maturityProperty");
        if (deltaTicks <= 0.0) {
            return new AgingState(batch, batch.number(maturityProperty, 0.0) >= kinetics.completionThreshold());
        }

        double rate = temperature.rateFactor(environment.temperature())
                * environment.agingRateFactor()
                * vessel.seasoningMultiplier();
        double imprintRate = temperature.rateFactor(environment.temperature()) * environment.agingRateFactor();
        double maturity = batch.number(maturityProperty, 0.0)
                + kinetics.maturityRatePerTick() * rate * deltaTicks;
        double wood = batch.number(woodProperty, 0.0)
                + kinetics.woodRatePerTick() * vessel.woodExtractionMultiplier() * rate * deltaTicks;
        double oxidation = batch.number(oxidationProperty, 0.0)
                + kinetics.oxidationRatePerTick()
                * vessel.oxidationMultiplier()
                * environment.oxidationFactor()
                * vessel.permeability()
                * deltaTicks;

        LiquidBatch next = batch
                .withProperty(maturityProperty, maturity)
                .withProperty(woodProperty, wood)
                .withProperty(oxidationProperty, oxidation);
        double threshold = kinetics.completionThreshold();
        double unseasonedDelta = kinetics.maturityRatePerTick() * imprintRate * deltaTicks;
        next = CaskImprint.leak(
                next,
                imprintOf(vessel),
                imprintTransfer,
                unseasonedDelta,
                threshold,
                imprintProperties
        );
        BatchProvenance provenance = next.batchProvenance().withSummaries(
                next.batchProvenance().fermentationStress(),
                next.batchProvenance().totalAgingTime() + deltaTicks,
                wood,
                oxidation
        );
        next = next.withProvenance(provenance);
        boolean complete = maturity >= threshold;
        if (complete && outputLiquid.isPresent()) {
            next = next.withBaseLiquid(outputLiquid.get());
        }
        return new AgingState(next, complete);
    }

    private static PropertyBag imprintOf(VesselProfile vessel) {
        return vessel.history()
                .map(AgingPhysics::imprintOf)
                .orElseGet(PropertyBag::empty);
    }

    private static PropertyBag imprintOf(BarrelHistoryView history) {
        if (history instanceof BarrelHistory barrel) {
            return barrel.imprint();
        }
        return CaskImprint.fromMap(history.caskImprint());
    }
}
