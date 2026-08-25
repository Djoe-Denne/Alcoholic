package com.djden.alcoholic.minecraft.content;

import com.djden.alcoholic.api.ResourceId;

public final class AlcoholicIds {
    public static final String MOD_ID = "alcoholic";

    public static final ResourceId RED_GRAPES = id("red_grapes");
    public static final ResourceId WHITE_GRAPES = id("white_grapes");
    public static final ResourceId RED_GRAPE_CUTTING = id("red_grape_cutting");
    public static final ResourceId WHITE_GRAPE_CUTTING = id("white_grape_cutting");
    public static final ResourceId RED_GRAPEVINE = id("red_grapevine");
    public static final ResourceId WHITE_GRAPEVINE = id("white_grapevine");
    public static final ResourceId VINEYARD_POST = id("vineyard_post");
    public static final ResourceId END_POST = id("end_post");
    public static final ResourceId TRELLIS_WIRE = id("trellis_wire");
    public static final ResourceId TRELLIS_SPOOL = id("trellis_spool");
    public static final ResourceId PRUNING_SHEARS = id("pruning_shears");
    public static final ResourceId VINE_BLOCK_ENTITY = id("vine");
    public static final ResourceId WILD_RED_GRAPEVINES = id("wild_red_grapevines");
    public static final ResourceId WILD_WHITE_GRAPEVINES = id("wild_white_grapevines");
    public static final ResourceId ARTISANAL_PRESS = id("artisanal_press");
    public static final ResourceId ARTISANAL_FERMENTER = id("artisanal_fermenter");
    public static final ResourceId YEAST = id("yeast");
    public static final ResourceId GRAPE_POMACE = id("grape_pomace");
    public static final ResourceId ARTISANAL_PRESS_ENTITY = id("artisanal_press");
    public static final ResourceId ARTISANAL_FERMENTER_ENTITY = id("artisanal_fermenter");
    public static final ResourceId RED_GRAPE_MUST = id("red_grape_must");
    public static final ResourceId WHITE_GRAPE_MUST = id("white_grape_must");
    public static final ResourceId YOUNG_RED_WINE = id("young_red_wine");
    public static final ResourceId YOUNG_WHITE_WINE = id("young_white_wine");
    public static final ResourceId RED_WINE = id("red_wine");
    public static final ResourceId WHITE_WINE = id("white_wine");
    public static final ResourceId OAK_BARREL = id("oak_barrel");
    public static final ResourceId OAK_BARREL_ENTITY = id("oak_barrel");
    public static final ResourceId ARTISANAL_BLENDING_CROCK = id("artisanal_blending_crock");
    public static final ResourceId ARTISANAL_BLENDING_CROCK_ENTITY = id("artisanal_blending_crock");
    public static final ResourceId EMPTY_BOTTLE = id("empty_bottle");
    public static final ResourceId BEVERAGE_BOTTLE = id("beverage_bottle");
    public static final ResourceId INDUSTRIAL_CASING = id("industrial_casing");
    public static final ResourceId MACHINE_WINDOW = id("machine_window");
    public static final ResourceId ACCESS_HATCH = id("access_hatch");
    public static final ResourceId FLUID_PORT = id("fluid_port");
    public static final ResourceId ITEM_PORT = id("item_port");
    public static final ResourceId KINETIC_PORT = id("kinetic_port");
    public static final ResourceId INDUSTRIAL_PRESS_CONTROLLER = id("industrial_press_controller");
    public static final ResourceId INDUSTRIAL_VAT_CONTROLLER = id("industrial_vat_controller");
    public static final ResourceId INDUSTRIAL_TANK_CONTROLLER = id("industrial_tank_controller");
    public static final ResourceId FLUID_PORT_ENTITY = id("fluid_port");
    public static final ResourceId ITEM_PORT_ENTITY = id("item_port");
    public static final ResourceId KINETIC_PORT_ENTITY = id("kinetic_port");
    public static final ResourceId INDUSTRIAL_PRESS_ENTITY = id("industrial_press_controller");
    public static final ResourceId INDUSTRIAL_VAT_ENTITY = id("industrial_vat_controller");
    public static final ResourceId INDUSTRIAL_TANK_ENTITY = id("industrial_tank_controller");

    private AlcoholicIds() {
    }

    public static ResourceId id(String path) {
        return new ResourceId(MOD_ID, path);
    }
}
