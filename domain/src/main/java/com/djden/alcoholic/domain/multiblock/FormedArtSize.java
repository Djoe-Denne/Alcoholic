package com.djden.alcoholic.domain.multiblock;

import com.djden.alcoholic.api.ResourceId;

import java.util.Map;
import java.util.Optional;

/**
 * Sculpted mega-mesh overlay is drawn only when the formed cuboid matches
 * the Blockbench art size. Other legal sizes keep the 9-slice hull alone.
 */
public record FormedArtSize(int width, int height, int depth) {
    public static final FormedArtSize MALT_HOUSE = new FormedArtSize(5, 4, 5);
    public static final FormedArtSize ROLLER_MILL = new FormedArtSize(3, 4, 3);
    public static final FormedArtSize MASH_TUN = new FormedArtSize(5, 5, 5);
    public static final FormedArtSize BREWING_KETTLE = new FormedArtSize(5, 6, 5);
    public static final FormedArtSize FERMENTATION_VAT = new FormedArtSize(3, 5, 3);
    public static final FormedArtSize CONDITIONING_VESSEL = new FormedArtSize(3, 6, 3);
    public static final FormedArtSize AGING_VESSEL = new FormedArtSize(3, 6, 3);
    public static final FormedArtSize STORAGE_TANK = new FormedArtSize(3, 5, 3);
    public static final FormedArtSize PRESS = new FormedArtSize(3, 4, 3);
    public static final FormedArtSize CRAFT_MALT_HOUSE = new FormedArtSize(3, 3, 3);
    public static final FormedArtSize CRAFT_MILL = new FormedArtSize(3, 3, 3);
    public static final FormedArtSize CRAFT_MASH_TUN = new FormedArtSize(3, 3, 3);
    public static final FormedArtSize CRAFT_BREWING_KETTLE = new FormedArtSize(3, 3, 3);
    public static final FormedArtSize CRAFT_VAT = new FormedArtSize(3, 3, 3);

    public static final ResourceId MALT_HOUSE_ID = ResourceId.parse("alcoholic:industrial_malt_house");
    public static final ResourceId ROLLER_MILL_ID = ResourceId.parse("alcoholic:industrial_roller_mill");
    public static final ResourceId MASH_TUN_ID = ResourceId.parse("alcoholic:industrial_mash_tun");
    public static final ResourceId BREWING_KETTLE_ID = ResourceId.parse("alcoholic:industrial_brewing_kettle");
    public static final ResourceId FERMENTATION_VAT_ID = ResourceId.parse("alcoholic:industrial_fermentation_vat");
    public static final ResourceId CONDITIONING_VESSEL_ID =
            ResourceId.parse("alcoholic:industrial_conditioning_vessel");
    public static final ResourceId AGING_VESSEL_ID = ResourceId.parse("alcoholic:industrial_aging_vessel");
    public static final ResourceId STORAGE_TANK_ID = ResourceId.parse("alcoholic:industrial_storage_tank");
    public static final ResourceId PRESS_ID = ResourceId.parse("alcoholic:industrial_press");
    public static final ResourceId CRAFT_MALT_HOUSE_ID = ResourceId.parse("alcoholic:craft_malt_house");
    public static final ResourceId CRAFT_MILL_ID = ResourceId.parse("alcoholic:craft_mill");
    public static final ResourceId CRAFT_MASH_TUN_ID = ResourceId.parse("alcoholic:craft_mash_tun");
    public static final ResourceId CRAFT_BREWING_KETTLE_ID = ResourceId.parse("alcoholic:craft_brewing_kettle");
    public static final ResourceId CRAFT_VAT_ID = ResourceId.parse("alcoholic:craft_vat");

    private static final Map<ResourceId, FormedArtSize> BY_DEFINITION = Map.ofEntries(
            Map.entry(MALT_HOUSE_ID, MALT_HOUSE),
            Map.entry(ROLLER_MILL_ID, ROLLER_MILL),
            Map.entry(MASH_TUN_ID, MASH_TUN),
            Map.entry(BREWING_KETTLE_ID, BREWING_KETTLE),
            Map.entry(FERMENTATION_VAT_ID, FERMENTATION_VAT),
            Map.entry(CONDITIONING_VESSEL_ID, CONDITIONING_VESSEL),
            Map.entry(AGING_VESSEL_ID, AGING_VESSEL),
            Map.entry(STORAGE_TANK_ID, STORAGE_TANK),
            Map.entry(PRESS_ID, PRESS),
            Map.entry(CRAFT_MALT_HOUSE_ID, CRAFT_MALT_HOUSE),
            Map.entry(CRAFT_MILL_ID, CRAFT_MILL),
            Map.entry(CRAFT_MASH_TUN_ID, CRAFT_MASH_TUN),
            Map.entry(CRAFT_BREWING_KETTLE_ID, CRAFT_BREWING_KETTLE),
            Map.entry(CRAFT_VAT_ID, CRAFT_VAT)
    );

    public FormedArtSize {
        if (width < 1 || height < 1 || depth < 1) {
            throw new IllegalArgumentException("art size must be positive");
        }
    }

    public static Optional<FormedArtSize> of(ResourceId definitionId) {
        return Optional.ofNullable(BY_DEFINITION.get(definitionId));
    }

    public static Map<ResourceId, FormedArtSize> all() {
        return BY_DEFINITION;
    }

    public boolean matches(int width, int height, int depth) {
        return this.width == width && this.height == height && this.depth == depth;
    }

    public boolean matches(AxisBox bounds) {
        return matches(bounds.width(), bounds.height(), bounds.depth());
    }

    /**
     * Mega-mesh overlay is a no-op unless the formed cuboid is exactly the art size.
     */
    public static Optional<FormedArtSize> overlayMesh(ResourceId definitionId, AxisBox bounds) {
        return of(definitionId).filter(size -> size.matches(bounds));
    }

    public static Optional<FormedArtSize> overlayMesh(
            ResourceId definitionId,
            int width,
            int height,
            int depth
    ) {
        return of(definitionId).filter(size -> size.matches(width, height, depth));
    }
}
