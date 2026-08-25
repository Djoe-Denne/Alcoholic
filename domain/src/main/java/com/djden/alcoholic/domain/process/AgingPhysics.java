package com.djden.alcoholic.domain.process;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.domain.liquid.BatchProvenance;
import com.djden.alcoholic.domain.liquid.LiquidBatch;
import com.djden.alcoholic.domain.vessel.EnvironmentProfile;
import com.djden.alcoholic.domain.vessel.VesselProfile;

import java.util.Objects;
import java.util.Optional;

/**
 * Progressive property evolution for {@code alcoholic:age}. Beverage identity is not consulted.
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
        BatchProvenance provenance = next.batchProvenance().withSummaries(
                next.batchProvenance().fermentationStress(),
                next.batchProvenance().totalAgingTime() + deltaTicks,
                wood,
                oxidation
        );
        next = next.withProvenance(provenance);
        boolean complete = maturity >= kinetics.completionThreshold();
        if (complete && outputLiquid.isPresent()) {
            next = next.withBaseLiquid(outputLiquid.get());
        }
        return new AgingState(next, complete);
    }
}
