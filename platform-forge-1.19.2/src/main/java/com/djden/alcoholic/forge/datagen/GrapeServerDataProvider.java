package com.djden.alcoholic.forge.datagen;

import com.djden.alcoholic.api.ResourceId;
import com.djden.alcoholic.api.data.JsonDataParser;
import com.djden.alcoholic.application.beverage.codec.ProcessDefinitionCodec;
import com.djden.alcoholic.integration.create.CreateMillRecipeTranslator;
import com.djden.alcoholic.integration.vinery.VineryIntegration;

import java.nio.file.Path;
import java.util.Locale;

final class GrapeServerDataProvider extends AlcoholicJsonProvider {
    GrapeServerDataProvider(Path outputRoot) {
        super(outputRoot);
    }

    @Override
    protected void collectJson(JsonSink sink) {
        addSemanticTags(sink);
        addVanillaTags(sink);
        addLootTable(sink, "red");
        addLootTable(sink, "white");
        addSelfDropLoot(sink, "vineyard_post");
        addSelfDropLoot(sink, "end_post");
        addSelfDropLoot(sink, "artisanal_press");
        addSelfDropLoot(sink, "artisanal_fermenter");
        addSelfDropLoot(sink, "oak_barrel");
        addSelfDropLoot(sink, "artisanal_blending_crock");
        addSelfDropLoot(sink, "malting_floor");
        addSelfDropLoot(sink, "mash_tun");
        addSelfDropLoot(sink, "brewing_kettle");
        addSelfDropLoot(sink, "malt_mill");
        addSelfDropLoot(sink, "primitive_combustion_engine");
        addSelfDropLoot(sink, "electric_motor");
        addEmptyLoot(sink, "trellis_wire");
        addEmptyLoot(sink, "red_grapevine_stem");
        addEmptyLoot(sink, "white_grapevine_stem");
        addEmptyLoot(sink, "red_grapevine_canopy");
        addEmptyLoot(sink, "white_grapevine_canopy");
        addBarleyLoot(sink);
        addHopBineLoot(sink);
        addWildHopsLoot(sink);
        addRecipes(sink);
        addProcessing(sink);
        addIndustrial(sink);
        addViticultureSettings(sink);
        addWildGrapevines(sink, "red", 18);
        addWildGrapevines(sink, "white", 22);
        addWildBarley(sink);
        addWildHops(sink);

        sink.add(
                "data/alcoholic/tags/worldgen/biome/has_wild_grapevines.json",
                """
                        {
                          "replace": false,
                          "values": [
                            "minecraft:plains",
                            "minecraft:sunflower_plains",
                            "minecraft:forest",
                            "minecraft:flower_forest",
                            "minecraft:birch_forest",
                            "minecraft:old_growth_birch_forest"
                          ]
                        }
                        """
        );
    }

    private static void addSemanticTags(JsonSink sink) {
        sink.add(
                "data/alcoholic/tags/items/grapes.json",
                """
                        {
                          "replace": false,
                          "values": [
                            "#alcoholic:grapes/red",
                            "#alcoholic:grapes/white"
                          ]
                        }
                        """
        );
        sink.add(
                "data/alcoholic/tags/items/grapes/red.json",
                """
                        {
                          "replace": false,
                          "values": [
                            "alcoholic:red_grapes",
                            {
                              "id": "#%s",
                              "required": false
                            }
                          ]
                        }
                        """.formatted(VineryIntegration.RED_GRAPES_TAG)
        );
        sink.add(
                "data/alcoholic/tags/items/grapes/white.json",
                """
                        {
                          "replace": false,
                          "values": [
                            "alcoholic:white_grapes",
                            {
                              "id": "#%s",
                              "required": false
                            }
                          ]
                        }
                        """.formatted(VineryIntegration.WHITE_GRAPES_TAG)
        );
        addSemanticItemTag(
                sink,
                "barley",
                "alcoholic:barley",
                optionalItem("brewery:barley")
        );
        addSemanticItemTag(
                sink,
                "barley/seeds",
                "alcoholic:barley_seeds",
                optionalItem("brewery:barley_seeds")
        );
        addSemanticItemTag(
                sink,
                "malted_barley",
                "alcoholic:malted_barley"
        );
        addSemanticItemTag(
                sink,
                "malted_grain",
                "#alcoholic:malted_barley"
        );
        addSemanticItemTag(
                sink,
                "grist",
                "alcoholic:grist"
        );
        addSemanticItemTag(
                sink,
                "hops",
                "alcoholic:hops",
                optionalItem("brewery:hops")
        );
        addSemanticItemTag(
                sink,
                "spent_grain",
                "alcoholic:spent_grain"
        );
        sink.add(
                "data/alcoholic/tags/items/yeast.json",
                """
                        {
                          "replace": false,
                          "values": [
                            "alcoholic:yeast"
                          ]
                        }
                        """
        );
    }

    private static void addEmptyTag(JsonSink sink, String name) {
        sink.add(
                "data/alcoholic/tags/items/" + name + ".json",
                """
                        {
                          "replace": false,
                          "values": []
                        }
                        """
        );
    }

    private static String optionalItem(String id) {
        return "{ \"id\": \"" + id + "\", \"required\": false }";
    }

    private static void addSemanticItemTag(JsonSink sink, String name, String... values) {
        StringBuilder json = new StringBuilder("{\n  \"replace\": false,\n  \"values\": [\n");
        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                json.append(",\n");
            }
            String value = values[index];
            if (value.startsWith("{")) {
                json.append("    ").append(value);
            } else {
                json.append("    \"").append(value).append('"');
            }
        }
        json.append("\n  ]\n}");
        sink.add("data/alcoholic/tags/items/" + name + ".json", json.toString());
    }

    private static void addVanillaTags(JsonSink sink) {
        sink.add(
                "data/minecraft/tags/blocks/crops.json",
                """
                        {
                          "replace": false,
                          "values": [
                            "alcoholic:red_grapevine",
                            "alcoholic:white_grapevine",
                            "alcoholic:barley_crop",
                            "alcoholic:hop_bine",
                            "alcoholic:wild_hops"
                          ]
                        }
                        """
        );
        sink.add(
                "data/minecraft/tags/items/villager_plantable_seeds.json",
                """
                        {
                          "replace": false,
                          "values": [
                            "alcoholic:red_grape_cutting",
                            "alcoholic:white_grape_cutting",
                            "alcoholic:barley_seeds",
                            "alcoholic:hop_rhizome"
                          ]
                        }
                        """
        );
        sink.add(
                "data/forge/tags/blocks/crops.json",
                """
                        {
                          "replace": false,
                          "values": [
                            "alcoholic:red_grapevine",
                            "alcoholic:white_grapevine",
                            "alcoholic:barley_crop",
                            "alcoholic:hop_bine",
                            "alcoholic:wild_hops"
                          ]
                        }
                        """
        );
    }

    private static void addLootTable(JsonSink sink, String color) {
        String vine = color + "_grapevine";
        String cutting = color + "_grape_cutting";
        sink.add(
                "data/alcoholic/loot_tables/blocks/" + vine + ".json",
                """
                        {
                          "type": "minecraft:block",
                          "pools": [
                            {
                              "rolls": 1,
                              "entries": [
                                {
                                  "type": "minecraft:item",
                                  "name": "alcoholic:%s"
                                }
                              ],
                              "conditions": [
                                {
                                  "condition": "minecraft:survives_explosion"
                                }
                              ]
                            }
                          ]
                        }
                        """.formatted(cutting)
        );
    }

    private static void addSelfDropLoot(JsonSink sink, String block) {
        sink.add(
                "data/alcoholic/loot_tables/blocks/" + block + ".json",
                """
                        {
                          "type": "minecraft:block",
                          "pools": [
                            {
                              "rolls": 1,
                              "entries": [
                                {
                                  "type": "minecraft:item",
                                  "name": "alcoholic:%s"
                                }
                              ],
                              "conditions": [
                                {
                                  "condition": "minecraft:survives_explosion"
                                }
                              ]
                            }
                          ]
                        }
                        """.formatted(block)
        );
    }

    private static void addEmptyLoot(JsonSink sink, String block) {
        sink.add(
                "data/alcoholic/loot_tables/blocks/" + block + ".json",
                """
                        {
                          "type": "minecraft:block",
                          "pools": []
                        }
                        """
        );
    }

    private static void addRecipes(JsonSink sink) {
        sink.add(
                "data/alcoholic/recipes/vineyard_post.json",
                """
                        {
                          "type": "minecraft:crafting_shaped",
                          "pattern": [
                            " S ",
                            " P ",
                            " P "
                          ],
                          "key": {
                            "S": { "item": "minecraft:stick" },
                            "P": { "tag": "minecraft:planks" }
                          },
                          "result": {
                            "item": "alcoholic:vineyard_post",
                            "count": 2
                          }
                        }
                        """
        );
        sink.add(
                "data/alcoholic/recipes/end_post.json",
                """
                        {
                          "type": "minecraft:crafting_shaped",
                          "pattern": [
                            "I I",
                            " L ",
                            " L "
                          ],
                          "key": {
                            "I": { "item": "minecraft:iron_nugget" },
                            "L": { "tag": "minecraft:logs" }
                          },
                          "result": {
                            "item": "alcoholic:end_post",
                            "count": 2
                          }
                        }
                        """
        );
        sink.add(
                "data/alcoholic/recipes/trellis_spool.json",
                """
                        {
                          "type": "minecraft:crafting_shaped",
                          "pattern": [
                            "III",
                            "SIS",
                            "III"
                          ],
                          "key": {
                            "I": { "item": "minecraft:iron_nugget" },
                            "S": { "item": "minecraft:string" }
                          },
                          "result": {
                            "item": "alcoholic:trellis_spool"
                          }
                        }
                        """
        );
        sink.add(
                "data/alcoholic/recipes/pruning_shears.json",
                """
                        {
                          "type": "minecraft:crafting_shaped",
                          "pattern": [
                            " I",
                            "SI"
                          ],
                          "key": {
                            "I": { "item": "minecraft:iron_ingot" },
                            "S": { "item": "minecraft:stick" }
                          },
                          "result": {
                            "item": "alcoholic:pruning_shears"
                          }
                        }
                        """
        );
        sink.add(
                "data/alcoholic/recipes/artisanal_press.json",
                """
                        {
                          "type": "minecraft:crafting_shaped",
                          "pattern": [
                            " I ",
                            "PBP",
                            "PPP"
                          ],
                          "key": {
                            "I": { "item": "minecraft:iron_ingot" },
                            "P": { "tag": "minecraft:planks" },
                            "B": { "item": "minecraft:bowl" }
                          },
                          "result": {
                            "item": "alcoholic:artisanal_press"
                          }
                        }
                        """
        );
        sink.add(
                "data/alcoholic/recipes/artisanal_fermenter.json",
                """
                        {
                          "type": "minecraft:crafting_shaped",
                          "pattern": [
                            "P P",
                            "P P",
                            "PPP"
                          ],
                          "key": {
                            "P": { "tag": "minecraft:planks" }
                          },
                          "result": {
                            "item": "alcoholic:artisanal_fermenter"
                          }
                        }
                        """
        );
        sink.add(
                "data/alcoholic/recipes/yeast.json",
                """
                        {
                          "type": "minecraft:crafting_shapeless",
                          "ingredients": [
                            { "item": "minecraft:brown_mushroom" },
                            { "item": "minecraft:sugar" }
                          ],
                          "result": {
                            "item": "alcoholic:yeast",
                            "count": 2
                          }
                        }
                        """
        );
        sink.add(
                "data/alcoholic/recipes/oak_barrel.json",
                """
                        {
                          "type": "minecraft:crafting_shaped",
                          "pattern": [
                            "P P",
                            "PIP",
                            "PPP"
                          ],
                          "key": {
                            "P": { "item": "minecraft:oak_planks" },
                            "I": { "item": "minecraft:iron_ingot" }
                          },
                          "result": {
                            "item": "alcoholic:oak_barrel"
                          }
                        }
                        """
        );
        sink.add(
                "data/alcoholic/recipes/artisanal_blending_crock.json",
                """
                        {
                          "type": "minecraft:crafting_shaped",
                          "pattern": [
                            "P P",
                            "PBP",
                            " P "
                          ],
                          "key": {
                            "P": { "tag": "minecraft:planks" },
                            "B": { "item": "minecraft:bowl" }
                          },
                          "result": {
                            "item": "alcoholic:artisanal_blending_crock"
                          }
                        }
                        """
        );
        sink.add(
                "data/alcoholic/recipes/malting_floor.json",
                """
                        {
                          "type": "minecraft:crafting_shaped",
                          "pattern": [
                            "SSS",
                            "PPP"
                          ],
                          "key": {
                            "S": { "item": "minecraft:wheat_seeds" },
                            "P": { "tag": "minecraft:planks" }
                          },
                          "result": {
                            "item": "alcoholic:malting_floor"
                          }
                        }
                        """
        );
        sink.add(
                "data/alcoholic/recipes/mash_tun.json",
                """
                        {
                          "type": "minecraft:crafting_shaped",
                          "pattern": [
                            "P P",
                            "PBP",
                            "PPP"
                          ],
                          "key": {
                            "P": { "tag": "minecraft:planks" },
                            "B": { "item": "minecraft:bucket" }
                          },
                          "result": {
                            "item": "alcoholic:mash_tun"
                          }
                        }
                        """
        );
        sink.add(
                "data/alcoholic/recipes/brewing_kettle.json",
                """
                        {
                          "type": "minecraft:crafting_shaped",
                          "pattern": [
                            "I I",
                            "IBI",
                            "III"
                          ],
                          "key": {
                            "I": { "item": "minecraft:iron_ingot" },
                            "B": { "item": "minecraft:bucket" }
                          },
                          "result": {
                            "item": "alcoholic:brewing_kettle"
                          }
                        }
                        """
        );
        sink.add(
                "data/alcoholic/recipes/empty_bottle.json",
                """
                        {
                          "type": "minecraft:crafting_shaped",
                          "pattern": [
                            " G ",
                            "G G",
                            " G "
                          ],
                          "key": {
                            "G": { "item": "minecraft:glass" }
                          },
                          "result": {
                            "item": "alcoholic:empty_bottle",
                            "count": 4
                          }
                        }
                        """
        );
        addShaped(sink, "industrial_casing", "III", "I I", "III", "I", "minecraft:iron_ingot", 4);
        addShaped(sink, "machine_window", "IGI", "G G", "IGI", "I", "minecraft:iron_ingot", "G", "minecraft:glass", 4);
        addShaped(sink, "access_hatch", " I ", "IHI", " I ", "I", "minecraft:iron_ingot", "H", "minecraft:iron_trapdoor", 1);
        addShaped(sink, "fluid_port", " I ", "IBI", " I ", "I", "minecraft:iron_ingot", "B", "minecraft:bucket", 1);
        addShaped(sink, "item_port", " I ", "IHI", " I ", "I", "minecraft:iron_ingot", "H", "minecraft:hopper", 1);
        addShaped(sink, "kinetic_port", " I ", "ISI", " I ", "I", "minecraft:iron_ingot", "S", "minecraft:iron_nugget", 1);
        sink.add(
                "data/alcoholic/recipes/malt_mill.json",
                """
                        {
                          "type": "minecraft:crafting_shaped",
                          "pattern": [
                            "S S",
                            "PSP",
                            "PPP"
                          ],
                          "key": {
                            "S": { "item": "minecraft:smooth_stone_slab" },
                            "P": { "tag": "minecraft:planks" }
                          },
                          "result": {
                            "item": "alcoholic:malt_mill"
                          }
                        }
                        """
        );
        sink.add(
                "data/alcoholic/recipes/primitive_combustion_engine.json",
                """
                        {
                          "type": "minecraft:crafting_shaped",
                          "pattern": [
                            "III",
                            "IFI",
                            "III"
                          ],
                          "key": {
                            "I": { "item": "minecraft:iron_ingot" },
                            "F": { "item": "minecraft:furnace" }
                          },
                          "result": {
                            "item": "alcoholic:primitive_combustion_engine"
                          }
                        }
                        """
        );
        sink.add(
                "data/alcoholic/recipes/electric_motor.json",
                """
                        {
                          "type": "minecraft:crafting_shaped",
                          "pattern": [
                            "IRI",
                            "RCR",
                            "IRI"
                          ],
                          "key": {
                            "I": { "item": "minecraft:iron_ingot" },
                            "R": { "item": "minecraft:redstone" },
                            "C": { "item": "minecraft:copper_ingot" }
                          },
                          "result": {
                            "item": "alcoholic:electric_motor"
                          }
                        }
                        """
        );
        sink.add(
                "data/alcoholic/recipes/electric_motor_ie.json",
                """
                        {
                          "type": "minecraft:crafting_shaped",
                          "pattern": [
                            "CIC",
                            "IMI",
                            "CIC"
                          ],
                          "key": {
                            "C": { "item": "immersiveengineering:component_iron" },
                            "I": { "item": "minecraft:iron_ingot" },
                            "M": { "item": "immersiveengineering:coil_lv" }
                          },
                          "result": {
                            "item": "alcoholic:electric_motor"
                          },
                          "conditions": [
                            {
                              "type": "alcoholic:item_present",
                              "item": "immersiveengineering:coil_lv"
                            }
                          ]
                        }
                        """
        );
        addShaped(sink, "industrial_press_controller", "IPI", "ICI", "III", "I", "minecraft:iron_ingot", "P", "alcoholic:artisanal_press", "C", "alcoholic:industrial_casing", 1);
        addShaped(sink, "industrial_vat_controller", "IFI", "ICI", "III", "I", "minecraft:iron_ingot", "F", "alcoholic:artisanal_fermenter", "C", "alcoholic:industrial_casing", 1);
        addShaped(sink, "industrial_tank_controller", "IBI", "ICI", "III", "I", "minecraft:iron_ingot", "B", "minecraft:bucket", "C", "alcoholic:industrial_casing", 1);
        addShaped(sink, "industrial_malt_house_controller", "IMI", "ICI", "III", "I", "minecraft:iron_ingot", "M", "alcoholic:malting_floor", "C", "alcoholic:industrial_casing", 1);
        addShaped(sink, "industrial_roller_mill_controller", "IRI", "ICI", "III", "I", "minecraft:iron_ingot", "R", "alcoholic:malt_mill", "C", "alcoholic:industrial_casing", 1);
        addShaped(sink, "industrial_mash_tun_controller", "ITI", "ICI", "III", "I", "minecraft:iron_ingot", "T", "alcoholic:mash_tun", "C", "alcoholic:industrial_casing", 1);
        addShaped(sink, "industrial_brewing_kettle_controller", "IKI", "ICI", "III", "I", "minecraft:iron_ingot", "K", "alcoholic:brewing_kettle", "C", "alcoholic:industrial_casing", 1);
        addShaped(sink, "industrial_conditioning_vessel_controller", "IFI", "ICI", "III", "I", "minecraft:iron_ingot", "F", "alcoholic:oak_barrel", "C", "alcoholic:industrial_casing", 1);
    }

    private static void addShaped(
            JsonSink sink,
            String result,
            String row1,
            String row2,
            String row3,
            String keyA,
            String itemA,
            int count
    ) {
        sink.add(
                "data/alcoholic/recipes/" + result + ".json",
                """
                        {
                          "type": "minecraft:crafting_shaped",
                          "pattern": ["%s", "%s", "%s"],
                          "key": { "%s": { "item": "%s" } },
                          "result": { "item": "alcoholic:%s", "count": %s }
                        }
                        """.formatted(row1, row2, row3, keyA, itemA, result, count)
        );
    }

    private static void addShaped(
            JsonSink sink,
            String result,
            String row1,
            String row2,
            String row3,
            String keyA,
            String itemA,
            String keyB,
            String itemB,
            int count
    ) {
        sink.add(
                "data/alcoholic/recipes/" + result + ".json",
                """
                        {
                          "type": "minecraft:crafting_shaped",
                          "pattern": ["%s", "%s", "%s"],
                          "key": {
                            "%s": { "item": "%s" },
                            "%s": { "item": "%s" }
                          },
                          "result": { "item": "alcoholic:%s", "count": %s }
                        }
                        """.formatted(row1, row2, row3, keyA, itemA, keyB, itemB, result, count)
        );
    }

    private static void addShaped(
            JsonSink sink,
            String result,
            String row1,
            String row2,
            String row3,
            String keyA,
            String itemA,
            String keyB,
            String itemB,
            String keyC,
            String itemC,
            int count
    ) {
        sink.add(
                "data/alcoholic/recipes/" + result + ".json",
                """
                        {
                          "type": "minecraft:crafting_shaped",
                          "pattern": ["%s", "%s", "%s"],
                          "key": {
                            "%s": { "item": "%s" },
                            "%s": { "item": "%s" },
                            "%s": { "item": "%s" }
                          },
                          "result": { "item": "alcoholic:%s", "count": %s }
                        }
                        """.formatted(row1, row2, row3, keyA, itemA, keyB, itemB, keyC, itemC, result, count)
        );
    }

    private static void addIndustrial(JsonSink sink) {
        addBlockTag(
                sink,
                "industrial_tank_casing",
                "alcoholic:industrial_casing",
                "alcoholic:access_hatch"
        );
        addBlockTag(
                sink,
                "fermenter_casing",
                "alcoholic:industrial_casing",
                "alcoholic:access_hatch"
        );
        addBlockTag(
                sink,
                "pressure_safe_casing",
                "alcoholic:industrial_casing",
                "alcoholic:access_hatch"
        );
        addBlockTag(sink, "valid_machine_windows", "alcoholic:machine_window");
        addBlockTag(
                sink,
                "industrial_ports",
                "alcoholic:fluid_port",
                "alcoholic:item_port",
                "alcoholic:kinetic_port"
        );
        for (String part : new String[]{
                "industrial_casing",
                "machine_window",
                "access_hatch",
                "fluid_port",
                "item_port",
                "kinetic_port"
        }) {
            addSelfDropLoot(sink, part);
        }
        addEmptyLoot(sink, "industrial_press_controller");
        addEmptyLoot(sink, "industrial_vat_controller");
        addEmptyLoot(sink, "industrial_tank_controller");
        addEmptyLoot(sink, "industrial_malt_house_controller");
        addEmptyLoot(sink, "industrial_roller_mill_controller");
        addEmptyLoot(sink, "industrial_mash_tun_controller");
        addEmptyLoot(sink, "industrial_brewing_kettle_controller");
        addEmptyLoot(sink, "industrial_conditioning_vessel_controller");
        addMachine(
                sink,
                "industrial_press",
                """
                        {
                          "id": "alcoholic:industrial_press",
                          "kind": "press",
                          "process": "alcoholic:press",
                          "min_exterior": { "x": 3, "y": 4, "z": 3 },
                          "max_exterior": { "x": 7, "y": 8, "z": 7 },
                          "required_controllers": 1,
                          "casing_tags": ["alcoholic:pressure_safe_casing"],
                          "window_tags": ["alcoholic:valid_machine_windows"],
                          "port_tags": ["alcoholic:industrial_ports"],
                          "required_ports": ["kinetic_port"],
                          "hollow_interior": true,
                          "capacity_per_internal_block": 4000,
                          "controller": "alcoholic:industrial_press_controller",
                          "modifiers": { "yield": 1.05, "speed": 2.0, "thermal_stability": 1.0, "max_batch_units": 2147483647 },
                          "kinetic": { "min_rpm": 16, "max_rpm": 256, "required": true }
                        }
                        """
        );
        addMachine(
                sink,
                "industrial_fermentation_vat",
                """
                        {
                          "id": "alcoholic:industrial_fermentation_vat",
                          "kind": "ferment",
                          "process": "alcoholic:ferment",
                          "min_exterior": { "x": 3, "y": 4, "z": 3 },
                          "max_exterior": { "x": 9, "y": 16, "z": 9 },
                          "required_controllers": 1,
                          "casing_tags": ["alcoholic:fermenter_casing"],
                          "window_tags": ["alcoholic:valid_machine_windows"],
                          "port_tags": ["alcoholic:industrial_ports"],
                          "required_ports": [],
                          "hollow_interior": true,
                          "capacity_per_internal_block": 8000,
                          "controller": "alcoholic:industrial_vat_controller",
                          "modifiers": { "yield": 1.0, "speed": 1.0, "thermal_stability": 4.0, "max_batch_units": 1 }
                        }
                        """
        );
        sink.add(
                "data/minecraft/tags/blocks/mineable/pickaxe.json",
                """
                        {
                          "replace": false,
                          "values": [
                            "alcoholic:industrial_casing",
                            "alcoholic:machine_window",
                            "alcoholic:access_hatch",
                            "alcoholic:fluid_port",
                            "alcoholic:item_port",
                            "alcoholic:kinetic_port",
                            "alcoholic:industrial_press_controller",
                            "alcoholic:industrial_vat_controller",
                            "alcoholic:industrial_tank_controller",
                            "alcoholic:industrial_malt_house_controller",
                            "alcoholic:industrial_roller_mill_controller",
                            "alcoholic:industrial_mash_tun_controller",
                            "alcoholic:industrial_brewing_kettle_controller",
                            "alcoholic:industrial_conditioning_vessel_controller"
                          ]
                        }
                        """
        );
        addMachine(
                sink,
                "industrial_storage_tank",
                """
                        {
                          "id": "alcoholic:industrial_storage_tank",
                          "kind": "storage",
                          "min_exterior": { "x": 3, "y": 4, "z": 3 },
                          "max_exterior": { "x": 9, "y": 16, "z": 9 },
                          "required_controllers": 1,
                          "casing_tags": ["alcoholic:industrial_tank_casing"],
                          "window_tags": ["alcoholic:valid_machine_windows"],
                          "port_tags": ["alcoholic:industrial_ports"],
                          "required_ports": [],
                          "hollow_interior": true,
                          "capacity_per_internal_block": 16000,
                          "controller": "alcoholic:industrial_tank_controller"
                        }
                        """
        );
        addMachine(
                sink,
                "industrial_malt_house",
                """
                        {
                          "id": "alcoholic:industrial_malt_house",
                          "kind": "malt",
                          "process": "alcoholic:malt",
                          "min_exterior": { "x": 3, "y": 4, "z": 3 },
                          "max_exterior": { "x": 7, "y": 8, "z": 7 },
                          "required_controllers": 1,
                          "casing_tags": ["alcoholic:fermenter_casing"],
                          "window_tags": ["alcoholic:valid_machine_windows"],
                          "port_tags": ["alcoholic:industrial_ports"],
                          "required_ports": [],
                          "hollow_interior": true,
                          "capacity_per_internal_block": 2000,
                          "controller": "alcoholic:industrial_malt_house_controller",
                          "modifiers": { "yield": 1.0, "speed": 2.0, "thermal_stability": 2.0, "max_batch_units": 2147483647 }
                        }
                        """
        );
        addMachine(
                sink,
                "industrial_roller_mill",
                """
                        {
                          "id": "alcoholic:industrial_roller_mill",
                          "kind": "mill",
                          "process": "alcoholic:mill",
                          "min_exterior": { "x": 3, "y": 4, "z": 3 },
                          "max_exterior": { "x": 5, "y": 6, "z": 5 },
                          "required_controllers": 1,
                          "casing_tags": ["alcoholic:pressure_safe_casing"],
                          "window_tags": ["alcoholic:valid_machine_windows"],
                          "port_tags": ["alcoholic:industrial_ports"],
                          "required_ports": ["kinetic_port"],
                          "hollow_interior": true,
                          "capacity_per_internal_block": 1000,
                          "controller": "alcoholic:industrial_roller_mill_controller",
                          "modifiers": { "yield": 1.0, "speed": 4.0, "thermal_stability": 1.0, "max_batch_units": 2147483647 },
                          "mechanical": { "min_speed": 16, "max_speed": 256, "required_capacity": 4.0, "required": true }
                        }
                        """
        );
        addMachine(
                sink,
                "industrial_mash_tun",
                """
                        {
                          "id": "alcoholic:industrial_mash_tun",
                          "kind": "mash",
                          "process": "alcoholic:mash",
                          "min_exterior": { "x": 3, "y": 4, "z": 3 },
                          "max_exterior": { "x": 9, "y": 12, "z": 9 },
                          "required_controllers": 1,
                          "casing_tags": ["alcoholic:fermenter_casing"],
                          "window_tags": ["alcoholic:valid_machine_windows"],
                          "port_tags": ["alcoholic:industrial_ports"],
                          "required_ports": [],
                          "hollow_interior": true,
                          "capacity_per_internal_block": 8000,
                          "controller": "alcoholic:industrial_mash_tun_controller",
                          "modifiers": { "yield": 1.05, "speed": 1.5, "thermal_stability": 6.0, "max_batch_units": 2147483647 }
                        }
                        """
        );
        addMachine(
                sink,
                "industrial_brewing_kettle",
                """
                        {
                          "id": "alcoholic:industrial_brewing_kettle",
                          "kind": "boil",
                          "process": "alcoholic:boil",
                          "min_exterior": { "x": 3, "y": 4, "z": 3 },
                          "max_exterior": { "x": 7, "y": 8, "z": 7 },
                          "required_controllers": 1,
                          "casing_tags": ["alcoholic:pressure_safe_casing"],
                          "window_tags": ["alcoholic:valid_machine_windows"],
                          "port_tags": ["alcoholic:industrial_ports"],
                          "required_ports": [],
                          "hollow_interior": true,
                          "capacity_per_internal_block": 6000,
                          "controller": "alcoholic:industrial_brewing_kettle_controller",
                          "modifiers": { "yield": 1.0, "speed": 1.5, "thermal_stability": 3.0, "max_batch_units": 1 }
                        }
                        """
        );
        addMachine(
                sink,
                "industrial_conditioning_vessel",
                """
                        {
                          "id": "alcoholic:industrial_conditioning_vessel",
                          "kind": "condition",
                          "process": "alcoholic:condition",
                          "min_exterior": { "x": 3, "y": 4, "z": 3 },
                          "max_exterior": { "x": 7, "y": 10, "z": 7 },
                          "required_controllers": 1,
                          "casing_tags": ["alcoholic:fermenter_casing"],
                          "window_tags": ["alcoholic:valid_machine_windows"],
                          "port_tags": ["alcoholic:industrial_ports"],
                          "required_ports": [],
                          "hollow_interior": true,
                          "capacity_per_internal_block": 8000,
                          "controller": "alcoholic:industrial_conditioning_vessel_controller",
                          "modifiers": { "yield": 1.0, "speed": 1.0, "thermal_stability": 3.0, "max_batch_units": 1 }
                        }
                        """
        );
    }

    private static void addBlockTag(JsonSink sink, String name, String... values) {
        StringBuilder json = new StringBuilder("{\n  \"replace\": false,\n  \"values\": [\n");
        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                json.append(",\n");
            }
            json.append("    \"").append(values[index]).append('"');
        }
        json.append("\n  ]\n}");
        sink.add("data/alcoholic/tags/blocks/" + name + ".json", json.toString());
    }

    private static void addMachine(JsonSink sink, String name, String json) {
        sink.add("data/alcoholic/alcoholic/machines/" + name + ".json", json);
    }

    private static void addProcessing(JsonSink sink) {
        addLiquid(sink, "red_grape_must", 0.62, 0.55, 0.0);
        addLiquid(sink, "white_grape_must", 0.58, 0.48, 0.0);
        addLiquid(sink, "young_red_wine", 0.0, 0.55, 0.11);
        addLiquid(sink, "young_white_wine", 0.0, 0.48, 0.10);
        addLiquid(sink, "red_wine", 0.0, 0.55, 0.11);
        addLiquid(sink, "white_wine", 0.0, 0.48, 0.10);
        addPressProcess(sink, "press_red_grapes", "alcoholic:grapes/red", "alcoholic:red_grape_must");
        addPressProcess(sink, "press_white_grapes", "alcoholic:grapes/white", "alcoholic:white_grape_must");
        addFermentProcess(sink, "ferment_red_must", "alcoholic:red_grape_must", "alcoholic:young_red_wine");
        addFermentProcess(sink, "ferment_white_must", "alcoholic:white_grape_must", "alcoholic:young_white_wine");
        addAgeProcess(sink, "age_young_red_wine", "alcoholic:young_red_wine", "alcoholic:red_wine");
        addAgeProcess(sink, "age_young_white_wine", "alcoholic:young_white_wine", "alcoholic:white_wine");
        addBlendProcess(sink, "blend_red_wine", "alcoholic:young_red_wine", "alcoholic:red_wine");
        addBlendProcess(sink, "blend_white_wine", "alcoholic:young_white_wine", "alcoholic:white_wine");
        addWineBeverage(sink, "young_red_wine", "press_red_grapes", "ferment_red_must", "alcoholic:grapes/red");
        addWineBeverage(sink, "young_white_wine", "press_white_grapes", "ferment_white_must", "alcoholic:grapes/white");
        addAgedWineBeverage(
                sink,
                "red_wine",
                "press_red_grapes",
                "ferment_red_must",
                "age_young_red_wine",
                "alcoholic:grapes/red"
        );
        addAgedWineBeverage(
                sink,
                "white_wine",
                "press_white_grapes",
                "ferment_white_must",
                "age_young_white_wine",
                "alcoholic:grapes/white"
        );
        addCreatePressing(sink, "press_red_grapes", "alcoholic:grapes/red", "alcoholic:red_grape_must");
        addCreatePressing(sink, "press_white_grapes", "alcoholic:grapes/white", "alcoholic:white_grape_must");
        addGrainProcessing(sink);
    }

    private static void addLiquid(
            JsonSink sink,
            String name,
            double sugar,
            double acidity,
            double ethanol
    ) {
        sink.add(
                "data/alcoholic/alcoholic/liquids/" + name + ".json",
                String.format(Locale.ROOT, """
                        {
                          "id": "alcoholic:%s",
                          "defaults": {
                            "alcoholic:sugar": %.2f,
                            "alcoholic:acidity": %.2f,
                            "alcoholic:ethanol": %.2f,
                            "alcoholic:temperature": 20.0,
                            "alcoholic:maturity": 0.0,
                            "alcoholic:wood_exposure": 0.0,
                            "alcoholic:oxidation_exposure": 0.0
                          }
                        }
                        """, name, sugar, acidity, ethanol)
        );
    }

    private static void addPressProcess(JsonSink sink, String id, String tag, String liquid) {
        sink.add(
                "data/alcoholic/alcoholic/processes/" + id + ".json",
                """
                        {
                          "id": "alcoholic:%s",
                          "process": "alcoholic:press",
                          "config": {
                            "input": { "tag": "%s", "amount": 8 },
                            "output": { "liquid": "%s", "volume": 1000 },
                            "byproduct": { "item": "alcoholic:grape_pomace", "amount": 1 },
                            "processing_time": 20,
                            "yield": 1.0,
                            "create_compatible": true
                          },
                          "inputs": { "fruit": { "tag": "%s" } },
                          "outputs": ["must"]
                        }
                        """.formatted(id, tag, liquid, tag)
        );
    }

    private static void addFermentProcess(JsonSink sink, String id, String input, String output) {
        sink.add(
                "data/alcoholic/alcoholic/processes/" + id + ".json",
                """
                        {
                          "id": "alcoholic:%s",
                          "process": "alcoholic:ferment",
                          "config": {
                            "input_liquid": "%s",
                            "output": { "liquid": "%s" },
                            "yeast": { "tag": "alcoholic:yeast" },
                            "require_yeast": true,
                            "preferred_temperature": { "min": 18, "max": 24 },
                            "operating_temperature": { "min": 10, "max": 30 },
                            "ticks_to_complete": 80,
                            "kinetics": {
                              "sugar_to_ethanol": 0.47,
                              "completion_threshold": 0.02,
                              "co2_per_sugar": 0.45
                            }
                          },
                          "inputs": {
                            "yeast": { "tag": "alcoholic:yeast" }
                          },
                          "outputs": ["young"]
                        }
                        """.formatted(id, input, output)
        );
    }

    private static void addWineBeverage(
            JsonSink sink,
            String id,
            String press,
            String ferment,
            String grapeTag
    ) {
        sink.add(
                "data/alcoholic/alcoholic/beverages/" + id + ".json",
                """
                        {
                          "id": "alcoholic:%s",
                          "graph": {
                            "nodes": [
                              {
                                "id": "press",
                                "definition": "alcoholic:%s",
                                "inputs": { "fruit": { "tag": "%s" } }
                              },
                              {
                                "id": "ferment",
                                "definition": "alcoholic:%s",
                                "inputs": {
                                  "must": { "node": "press", "port": "must" },
                                  "yeast": { "tag": "alcoholic:yeast" }
                                }
                              }
                            ],
                            "outputs": { "result": { "node": "ferment", "port": "young" } }
                          },
                          "properties": [
                            "alcoholic:sugar",
                            "alcoholic:ethanol",
                            "alcoholic:acidity",
                            "alcoholic:quality"
                          ]
                        }
                        """.formatted(id, press, grapeTag, ferment)
        );
    }

    private static void addAgeProcess(JsonSink sink, String id, String input, String output) {
        sink.add(
                "data/alcoholic/alcoholic/processes/" + id + ".json",
                """
                        {
                          "id": "alcoholic:%s",
                          "process": "alcoholic:age",
                          "config": {
                            "input_liquid": "%s",
                            "output": { "liquid": "%s" },
                            "preferred_temperature": { "min": 10, "max": 16 },
                            "operating_temperature": { "min": 0, "max": 36 },
                            "ticks_to_complete": 80,
                            "kinetics": {
                              "completion_threshold": 1.0
                            }
                          },
                          "inputs": {},
                          "outputs": ["finished"]
                        }
                        """.formatted(id, input, output)
        );
    }

    private static void addBlendProcess(JsonSink sink, String id, String young, String finished) {
        sink.add(
                "data/alcoholic/alcoholic/processes/" + id + ".json",
                """
                        {
                          "id": "alcoholic:%s",
                          "process": "alcoholic:blend",
                          "config": {
                            "accepted_inputs": ["%s", "%s"],
                            "output": { "liquid": "%s" },
                            "min_inputs": 2
                          },
                          "inputs": {},
                          "outputs": ["blended"]
                        }
                        """.formatted(id, young, finished, finished)
        );
    }

    private static void addAgedWineBeverage(
            JsonSink sink,
            String id,
            String press,
            String ferment,
            String age,
            String grapeTag
    ) {
        sink.add(
                "data/alcoholic/alcoholic/beverages/" + id + ".json",
                """
                        {
                          "id": "alcoholic:%s",
                          "graph": {
                            "nodes": [
                              {
                                "id": "press",
                                "definition": "alcoholic:%s",
                                "inputs": { "fruit": { "tag": "%s" } }
                              },
                              {
                                "id": "ferment",
                                "definition": "alcoholic:%s",
                                "inputs": {
                                  "must": { "node": "press", "port": "must" },
                                  "yeast": { "tag": "alcoholic:yeast" }
                                }
                              },
                              {
                                "id": "age",
                                "definition": "alcoholic:%s",
                                "inputs": {
                                  "young": { "node": "ferment", "port": "young" }
                                },
                                "outputs": ["finished"]
                              }
                            ],
                            "outputs": { "result": { "node": "age", "port": "finished" } }
                          },
                          "properties": [
                            "alcoholic:sugar",
                            "alcoholic:ethanol",
                            "alcoholic:acidity",
                            "alcoholic:quality",
                            "alcoholic:maturity"
                          ]
                        }
                        """.formatted(id, press, grapeTag, ferment, age)
        );
    }

    private static void addCreatePressing(JsonSink sink, String id, String tag, String liquid) {
        String ingredients = ("              { \"tag\": \"" + tag + "\" },\n").repeat(7)
                + "              { \"tag\": \"" + tag + "\" }";
        sink.add(
                "data/alcoholic/recipes/" + id + "_create.json",
                """
                        {
                          "type": "forge:conditional",
                          "recipes": [
                            {
                              "conditions": [
                                { "type": "forge:mod_loaded", "modid": "create" }
                              ],
                              "recipe": {
                                "type": "create:compacting",
                                "ingredients": [
                        %s
                                ],
                                "results": [
                                  { "item": "alcoholic:grape_pomace" },
                                  { "fluid": "%s", "amount": 1000 }
                                ]
                              }
                            }
                          ]
                        }
                        """.formatted(ingredients, liquid)
        );
    }

    private static void addViticultureSettings(JsonSink sink) {
        sink.add(
                "data/alcoholic/viticulture/settings.json",
                """
                        {
                          "type": "settings",
                          "max_wire_distance": 32,
                          "untrained": {
                            "yield": 0.70,
                            "quality": 0.85
                          },
                          "trained": {
                            "yield": 1.0,
                            "quality": 1.0
                          }
                        }
                        """
        );
        addVarietySettings(
                sink,
                "red",
                25.0,
                0.50,
                0.78,
                11.0,
                0.35,
                0.50
        );
        addVarietySettings(
                sink,
                "white",
                18.0,
                0.65,
                0.72,
                9.0,
                0.30,
                0.50
        );
    }

    private static void addVarietySettings(
            JsonSink sink,
            String color,
            double temperature,
            double humidity,
            double sunlight,
            double temperatureTolerance,
            double humidityTolerance,
            double sunlightTolerance
    ) {
        sink.add(
                "data/alcoholic/viticulture/" + color + "_grape.json",
                String.format(Locale.ROOT, """
                        {
                          "variety": "alcoholic:%s_grape",
                          "growth": {
                            "base_growth_chance": 0.35,
                            "progress_increment": 0.25,
                            "climate": {
                              "temperature_celsius": %.2f,
                              "humidity": %.2f,
                              "sunlight": %.2f,
                              "temperature_tolerance": %.2f,
                              "humidity_tolerance": %.2f,
                              "sunlight_tolerance": %.2f
                            }
                          },
                          "harvest": {
                            "base_quantity": 12.0,
                            "maximum_quantity": 30.0,
                            "base_quality": 0.45,
                            "base_sugar": 0.50,
                            "base_acidity": 0.50,
                            "suitability_quality_bonus": 0.25,
                            "warmth_sugar_effect": 0.20,
                            "warmth_acidity_effect": 0.20,
                            "trellising_quality_effect": 0.08,
                            "climate": {
                              "temperature_celsius": %.2f,
                              "humidity": %.2f,
                              "sunlight": %.2f,
                              "temperature_tolerance": %.2f,
                              "humidity_tolerance": %.2f,
                              "sunlight_tolerance": %.2f
                            },
                            "pruning": {
                              "light": {
                                "yield": 1.20,
                                "quality": 0.92
                              },
                              "balanced": {
                                "yield": 1.0,
                                "quality": 1.0
                              },
                              "severe": {
                                "yield": 0.75,
                                "quality": 1.12
                              }
                            }
                          }
                        }
                        """,
                        color,
                        temperature,
                        humidity,
                        sunlight,
                        temperatureTolerance,
                        humidityTolerance,
                        sunlightTolerance,
                        temperature,
                        humidity,
                        sunlight,
                        temperatureTolerance,
                        humidityTolerance,
                        sunlightTolerance
                )
        );
    }

    private static void addWildGrapevines(JsonSink sink, String color, int rarity) {
        String feature = "wild_" + color + "_grapevines";
        String vine = color + "_grapevine";
        sink.add(
                "data/alcoholic/worldgen/configured_feature/" + feature + ".json",
                """
                        {
                          "type": "minecraft:random_patch",
                          "config": {
                            "tries": 18,
                            "xz_spread": 7,
                            "y_spread": 3,
                            "feature": {
                              "feature": {
                                "type": "minecraft:simple_block",
                                "config": {
                                  "to_place": {
                                    "type": "minecraft:simple_state_provider",
                                    "state": {
                                      "Name": "alcoholic:%s",
                                      "Properties": {
                                        "age": "4",
                                        "stage": "harvest_ready",
                                        "trained": "false",
                                        "extended": "false"
                                      }
                                    }
                                  }
                                }
                              },
                              "placement": [
                                {
                                  "type": "minecraft:block_predicate_filter",
                                  "predicate": {
                                    "type": "minecraft:matching_blocks",
                                    "blocks": "minecraft:air"
                                  }
                                }
                              ]
                            }
                          }
                        }
                        """.formatted(vine)
        );
        sink.add(
                "data/alcoholic/worldgen/placed_feature/" + feature + ".json",
                """
                        {
                          "feature": "alcoholic:%s",
                          "placement": [
                            {
                              "type": "minecraft:rarity_filter",
                              "chance": %d
                            },
                            {
                              "type": "minecraft:in_square"
                            },
                            {
                              "type": "minecraft:heightmap",
                              "heightmap": "WORLD_SURFACE_WG"
                            },
                            {
                              "type": "minecraft:biome"
                            }
                          ]
                        }
                        """.formatted(feature, rarity)
        );
    }

    private static void addGrainProcessing(JsonSink sink) {
        addLiquidDefaults(sink, "wort", 0.0, 0.12, 0.0);
        addLiquidDefaults(sink, "hopped_wort", 0.0, 0.12, 0.0);
        addLiquidDefaults(sink, "beer", 0.0, 0.12, 0.0);
        addMaltProcess(sink, "malt_pale", 0.12, 0.85, 0.15);
        addMaltProcess(sink, "malt_amber", 0.35, 0.78, 0.45);
        addMaltProcess(sink, "malt_dark", 0.62, 0.70, 0.80);
        sink.add(
                "data/alcoholic/alcoholic/processes/mill_malted_grain.json",
                MILL_MALTED_GRAIN
        );
        sink.add(
                "data/alcoholic/alcoholic/processes/mash_wort.json",
                """
                        {
                          "id": "alcoholic:mash_wort",
                          "process": "alcoholic:mash",
                          "config": {
                            "solid": { "tag": "alcoholic:grist", "amount": 1 },
                            "liquid": { "fluid": "minecraft:water", "volume": 1000 },
                            "output": { "liquid": "alcoholic:wort", "volume": 1000 },
                            "byproduct": { "item": "alcoholic:spent_grain", "amount": 1 },
                            "processing_time": 40,
                            "preferred_temperature": { "min": 62, "max": 68 },
                            "operating_temperature": { "min": 52, "max": 78 }
                          },
                          "inputs": {
                            "grist": { "tag": "alcoholic:grist" },
                            "water": { "item": "minecraft:water_bucket" }
                          },
                          "outputs": ["wort"]
                        }
                        """
        );
        sink.add(
                "data/alcoholic/alcoholic/processes/boil_wort.json",
                """
                        {
                          "id": "alcoholic:boil_wort",
                          "process": "alcoholic:boil",
                          "config": {
                            "input_liquid": "alcoholic:wort",
                            "output": { "liquid": "alcoholic:hopped_wort" },
                            "addition": { "tag": "alcoholic:hops", "amount": 1 },
                            "processing_time": 40,
                            "preferred_temperature": { "min": 98, "max": 105 },
                            "operating_temperature": { "min": 90, "max": 110 },
                            "hop_profile": {
                              "bitterness_potential": 0.55,
                              "aroma_potential": 0.40
                            },
                            "additions": [
                              { "tag": "alcoholic:hops", "at_progress": 0.0, "role": "bittering" }
                            ]
                          },
                          "inputs": { "hops": { "tag": "alcoholic:hops" } },
                          "outputs": ["hopped_wort"]
                        }
                        """
        );
        addFermentProcess(sink, "ferment_hopped_wort", "alcoholic:hopped_wort", "alcoholic:beer");
        sink.add(
                "data/alcoholic/alcoholic/processes/condition_beer.json",
                """
                        {
                          "id": "alcoholic:condition_beer",
                          "process": "alcoholic:condition",
                          "config": {
                            "input_liquid": "alcoholic:beer",
                            "output": { "liquid": "alcoholic:beer" },
                            "processing_time": 80,
                            "preferred_temperature": { "min": 2, "max": 12 },
                            "operating_temperature": { "min": 0, "max": 20 },
                            "carbonation_from_residual_sugar": 0.35,
                            "completion_maturity": 0.85
                          },
                          "inputs": {},
                          "outputs": ["finished"]
                        }
                        """
        );
        sink.add(
                "data/alcoholic/alcoholic/beverages/beer.json",
                """
                        {
                          "id": "alcoholic:beer",
                          "graph": {
                            "nodes": [
                              {
                                "id": "malt",
                                "definition": "alcoholic:malt_pale",
                                "inputs": { "grain": { "tag": "alcoholic:barley" } },
                                "outputs": ["malt"]
                              },
                              {
                                "id": "mill",
                                "definition": "alcoholic:mill_malted_grain",
                                "inputs": { "malt": { "node": "malt", "port": "malt" } },
                                "outputs": ["grist"]
                              },
                              {
                                "id": "mash",
                                "definition": "alcoholic:mash_wort",
                                "inputs": {
                                  "grist": { "node": "mill", "port": "grist" },
                                  "water": { "item": "minecraft:water_bucket" }
                                },
                                "outputs": ["wort"]
                              },
                              {
                                "id": "boil",
                                "definition": "alcoholic:boil_wort",
                                "inputs": {
                                  "wort": { "node": "mash", "port": "wort" },
                                  "hops": { "tag": "alcoholic:hops" }
                                },
                                "outputs": ["hopped_wort"]
                              },
                              {
                                "id": "ferment",
                                "definition": "alcoholic:ferment_hopped_wort",
                                "inputs": {
                                  "wort": { "node": "boil", "port": "hopped_wort" },
                                  "yeast": { "tag": "alcoholic:yeast" }
                                },
                                "outputs": ["finished"]
                              }
                            ],
                            "outputs": { "result": { "node": "ferment", "port": "finished" } }
                          },
                          "properties": [
                            "alcoholic:sugar",
                            "alcoholic:ethanol",
                            "alcoholic:bitterness",
                            "alcoholic:color",
                            "alcoholic:aroma"
                          ]
                        }
                        """
        );
        addCreateMillRecipesFromCatalog(sink, MILL_MALTED_GRAIN);
    }

    private static final String MILL_MALTED_GRAIN = """
            {
              "id": "alcoholic:mill_malted_grain",
              "process": "alcoholic:mill",
              "config": {
                "input": { "tag": "alcoholic:malted_grain", "amount": 1 },
                "output": { "item": "alcoholic:grist", "amount": 1 },
                "processing_time": 80,
                "create_compatible": true
              },
              "inputs": { "malt": { "tag": "alcoholic:malted_grain" } },
              "outputs": ["grist"]
            }
            """;

    private static void addLiquidDefaults(JsonSink sink, String name, double sugar, double color, double ethanol) {
        sink.add(
                "data/alcoholic/alcoholic/liquids/" + name + ".json",
                String.format(Locale.ROOT, """
                        {
                          "id": "alcoholic:%s",
                          "defaults": {
                            "alcoholic:sugar": %.2f,
                            "alcoholic:color": %.2f,
                            "alcoholic:ethanol": %.2f,
                            "alcoholic:bitterness": 0.0,
                            "alcoholic:aroma": 0.0,
                            "alcoholic:temperature": 20.0
                          }
                        }
                        """, name, sugar, color, ethanol)
        );
    }

    private static void addMaltProcess(
            JsonSink sink,
            String id,
            double color,
            double fermentable,
            double roast
    ) {
        sink.add(
                "data/alcoholic/alcoholic/processes/" + id + ".json",
                String.format(Locale.ROOT, """
                        {
                          "id": "alcoholic:%s",
                          "process": "alcoholic:malt",
                          "config": {
                            "input": { "tag": "alcoholic:barley", "amount": 1 },
                            "output": { "item": "alcoholic:malted_barley", "amount": 1 },
                            "processing_time": 80,
                            "moisture_requirement": 0.4,
                            "kiln_profile": {
                              "id": "alcoholic:%s",
                              "color_potential": %.2f,
                              "fermentable_potential": %.2f,
                              "roast_intensity": %.2f
                            }
                          },
                          "inputs": { "grain": { "tag": "alcoholic:barley" } },
                          "outputs": ["malt"]
                        }
                        """, id, id.replace("malt_", ""), color, fermentable, roast)
        );
    }

    private static void addCreateMillRecipesFromCatalog(JsonSink sink, String processJson) {
        var definition = ProcessDefinitionCodec.INSTANCE.decode(
                JsonDataParser.parse(processJson),
                "mill",
                ResourceId.parse("alcoholic:mill_malted_grain")
        );
        CreateMillRecipeTranslator.from(definition).ifPresent(spec -> {
            sink.add(
                    "data/alcoholic/recipes/" + spec.id().path() + "_millstone.json",
                    wrapCreate(CreateMillRecipeTranslator.toMillingJson(spec))
            );
            sink.add(
                    "data/alcoholic/recipes/" + spec.id().path() + "_crushing.json",
                    wrapCreate(CreateMillRecipeTranslator.toCrushingJson(spec))
            );
        });
    }

    private static String wrapCreate(String recipeJson) {
        return """
                {
                  "type": "forge:conditional",
                  "recipes": [
                    {
                      "conditions": [ { "type": "forge:mod_loaded", "modid": "create" } ],
                      "recipe": %s
                    }
                  ]
                }
                """.formatted(recipeJson.trim());
    }

    private static void addBarleyLoot(JsonSink sink) {
        sink.add(
                "data/alcoholic/loot_tables/blocks/barley_crop.json",
                """
                        {
                          "type": "minecraft:block",
                          "pools": [
                            {
                              "rolls": 1,
                              "entries": [
                                {
                                  "type": "minecraft:alternatives",
                                  "children": [
                                    {
                                      "type": "minecraft:item",
                                      "name": "alcoholic:barley",
                                      "conditions": [
                                        {
                                          "condition": "minecraft:block_state_property",
                                          "block": "alcoholic:barley_crop",
                                          "properties": { "age": "2" }
                                        }
                                      ]
                                    },
                                    {
                                      "type": "minecraft:item",
                                      "name": "alcoholic:barley_seeds"
                                    }
                                  ]
                                }
                              ]
                            },
                            {
                              "rolls": 1,
                              "entries": [
                                { "type": "minecraft:item", "name": "alcoholic:barley_seeds" }
                              ],
                              "conditions": [
                                {
                                  "condition": "minecraft:block_state_property",
                                  "block": "alcoholic:barley_crop",
                                  "properties": { "age": "2" }
                                }
                              ]
                            }
                          ]
                        }
                        """
        );
    }

    private static void addWildHopsLoot(JsonSink sink) {
        sink.add(
                "data/alcoholic/loot_tables/blocks/wild_hops.json",
                """
                        {
                          "type": "minecraft:block",
                          "pools": [
                            {
                              "rolls": 1,
                              "entries": [
                                { "type": "minecraft:item", "name": "alcoholic:hop_rhizome" }
                              ]
                            },
                            {
                              "rolls": 1,
                              "entries": [
                                { "type": "minecraft:item", "name": "alcoholic:hops" }
                              ]
                            }
                          ]
                        }
                        """
        );
    }

    private static void addHopBineLoot(JsonSink sink) {
        sink.add(
                "data/alcoholic/loot_tables/blocks/hop_bine.json",
                """
                        {
                          "type": "minecraft:block",
                          "pools": [
                            {
                              "rolls": 1,
                              "entries": [
                                { "type": "minecraft:item", "name": "alcoholic:hop_rhizome" }
                              ]
                            },
                            {
                              "rolls": 1,
                              "entries": [
                                { "type": "minecraft:item", "name": "alcoholic:hops" }
                              ],
                              "conditions": [
                                {
                                  "condition": "minecraft:block_state_property",
                                  "block": "alcoholic:hop_bine",
                                  "properties": { "age": "2" }
                                }
                              ]
                            }
                          ]
                        }
                        """
        );
    }

    private static void addWildBarley(JsonSink sink) {
        sink.add(
                "data/alcoholic/tags/worldgen/biome/has_wild_barley.json",
                """
                        {
                          "replace": false,
                          "values": [
                            "minecraft:plains",
                            "minecraft:sunflower_plains",
                            "minecraft:meadow"
                          ]
                        }
                        """
        );
        sink.add(
                "data/alcoholic/worldgen/configured_feature/wild_barley.json",
                """
                        {
                          "type": "minecraft:random_patch",
                          "config": {
                            "tries": 24,
                            "xz_spread": 6,
                            "y_spread": 2,
                            "feature": {
                              "feature": {
                                "type": "minecraft:simple_block",
                                "config": {
                                  "to_place": {
                                    "type": "minecraft:simple_state_provider",
                                    "state": {
                                      "Name": "alcoholic:barley_crop",
                                      "Properties": { "age": "2" }
                                    }
                                  }
                                }
                              },
                              "placement": [
                                {
                                  "type": "minecraft:block_predicate_filter",
                                  "predicate": {
                                    "type": "minecraft:matching_blocks",
                                    "blocks": "minecraft:air"
                                  }
                                }
                              ]
                            }
                          }
                        }
                        """
        );
        sink.add(
                "data/alcoholic/worldgen/placed_feature/wild_barley.json",
                """
                        {
                          "feature": "alcoholic:wild_barley",
                          "placement": [
                            { "type": "minecraft:rarity_filter", "chance": 18 },
                            { "type": "minecraft:in_square" },
                            { "type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG" },
                            { "type": "minecraft:biome" }
                          ]
                        }
                        """
        );
    }

    private static void addWildHops(JsonSink sink) {
        sink.add(
                "data/alcoholic/tags/worldgen/biome/has_wild_hops.json",
                """
                        {
                          "replace": false,
                          "values": [
                            "minecraft:forest",
                            "minecraft:flower_forest",
                            "minecraft:birch_forest",
                            "minecraft:taiga",
                            "minecraft:river"
                          ]
                        }
                        """
        );
        sink.add(
                "data/alcoholic/worldgen/configured_feature/wild_hops.json",
                """
                        {
                          "type": "minecraft:random_patch",
                          "config": {
                            "tries": 10,
                            "xz_spread": 5,
                            "y_spread": 2,
                            "feature": {
                              "feature": {
                                "type": "minecraft:simple_block",
                                "config": {
                                  "to_place": {
                                    "type": "minecraft:simple_state_provider",
                                    "state": {
                                      "Name": "alcoholic:wild_hops"
                                    }
                                  }
                                }
                              },
                              "placement": [
                                {
                                  "type": "minecraft:block_predicate_filter",
                                  "predicate": {
                                    "type": "minecraft:matching_blocks",
                                    "blocks": "minecraft:air"
                                  }
                                }
                              ]
                            }
                          }
                        }
                        """
        );
        sink.add(
                "data/alcoholic/worldgen/placed_feature/wild_hops.json",
                """
                        {
                          "feature": "alcoholic:wild_hops",
                          "placement": [
                            { "type": "minecraft:rarity_filter", "chance": 36 },
                            { "type": "minecraft:in_square" },
                            { "type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG" },
                            { "type": "minecraft:biome" }
                          ]
                        }
                        """
        );
    }

    @Override
    public String getName() {
        return "Alcoholic grape server data";
    }
}
