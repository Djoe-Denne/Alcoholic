package com.djden.alcoholic.minecraft.agriculture;

import com.djden.alcoholic.domain.viticulture.VineGrowthStage;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;
import java.util.Objects;

/**
 * Block-state serialization counterpart of the domain growth stage.
 */
public enum VineStage implements StringRepresentable {
    PLANTED(VineGrowthStage.PLANTED),
    ESTABLISHING(VineGrowthStage.ESTABLISHING),
    VEGETATIVE(VineGrowthStage.VEGETATIVE),
    FLOWERING(VineGrowthStage.FLOWERING),
    GREEN_FRUIT(VineGrowthStage.GREEN_FRUIT),
    RIPENING(VineGrowthStage.RIPENING),
    HARVEST_READY(VineGrowthStage.HARVEST_READY),
    DORMANT(VineGrowthStage.DORMANT);

    private final VineGrowthStage domainStage;
    private final String serializedName;

    VineStage(VineGrowthStage domainStage) {
        this.domainStage = domainStage;
        serializedName = domainStage.name().toLowerCase(Locale.ROOT);
    }

    public VineGrowthStage domainStage() {
        return domainStage;
    }

    public static VineStage fromDomain(VineGrowthStage stage) {
        Objects.requireNonNull(stage, "stage");
        return valueOf(stage.name());
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
