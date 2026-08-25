package com.djden.alcoholic.forge.datagen;

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
        addEmptyLoot(sink, "trellis_wire");
        addRecipes(sink);
        addProcessing(sink);
        addIndustrial(sink);
        addViticultureSettings(sink);
        addWildGrapevines(sink, "red", 18);
        addWildGrapevines(sink, "white", 22);

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
        addEmptyTag(sink, "barley");
        addEmptyTag(sink, "hops");
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

    private static void addVanillaTags(JsonSink sink) {
        sink.add(
                "data/minecraft/tags/blocks/crops.json",
                """
                        {
                          "replace": false,
                          "values": [
                            "alcoholic:red_grapevine",
                            "alcoholic:white_grapevine"
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
                            "alcoholic:white_grape_cutting"
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
                            "alcoholic:white_grapevine"
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
        addShaped(sink, "industrial_press_controller", "IPI", "ICI", "III", "I", "minecraft:iron_ingot", "P", "alcoholic:artisanal_press", "C", "alcoholic:industrial_casing", 1);
        addShaped(sink, "industrial_vat_controller", "IFI", "ICI", "III", "I", "minecraft:iron_ingot", "F", "alcoholic:artisanal_fermenter", "C", "alcoholic:industrial_casing", 1);
        addShaped(sink, "industrial_tank_controller", "IBI", "ICI", "III", "I", "minecraft:iron_ingot", "B", "minecraft:bucket", "C", "alcoholic:industrial_casing", 1);
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
                            "alcoholic:industrial_tank_controller"
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
                                        "trained": "false"
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

    @Override
    public String getName() {
        return "Alcoholic grape server data";
    }
}
