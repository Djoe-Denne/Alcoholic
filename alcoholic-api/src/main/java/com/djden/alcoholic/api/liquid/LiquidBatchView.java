package com.djden.alcoholic.api.liquid;

import com.djden.alcoholic.api.PublicApi;
import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.beverage.BeverageIdentity;
import com.djden.alcoholic.api.property.LiquidProperty;

import java.util.Optional;
import java.util.Set;

@PublicApi
public interface LiquidBatchView {
    Optional<BeverageIdentity> identity();

    Optional<ResourceId> baseLiquid();

    double volume();

    Set<ResourceId> propertyIds();

    Optional<Object> property(ResourceId id);

    default <T> Optional<T> property(LiquidProperty<T> property) {
        return property(property.id()).map(property.valueType()::cast);
    }

    default BatchProvenanceView provenance() {
        return EmptyProvenance.INSTANCE;
    }

    enum EmptyProvenance implements BatchProvenanceView {
        INSTANCE;

        @Override
        public int schemaVersion() {
            return 1;
        }

        @Override
        public java.util.Map<ResourceId, Double> originComposition() {
            return java.util.Map.of();
        }

        @Override
        public java.util.Map<ResourceId, Double> blendComposition() {
            return java.util.Map.of();
        }

        @Override
        public double fermentationStress() {
            return 0.0;
        }

        @Override
        public double totalAgingTime() {
            return 0.0;
        }

        @Override
        public double woodExposure() {
            return 0.0;
        }

        @Override
        public double oxidationExposure() {
            return 0.0;
        }
    }
}
