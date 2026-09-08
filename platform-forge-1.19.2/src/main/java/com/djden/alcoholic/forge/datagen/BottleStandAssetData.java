package com.djden.alcoholic.forge.datagen;

/** Generated blockstates and item-model dispatch for the bottle display feature. */
final class BottleStandAssetData {
    private BottleStandAssetData() {
    }

    static void add(AlcoholicJsonProvider.JsonSink sink) {
        addStand(sink, "bottle_rack");
        addStand(sink, "bottle_shelf");
        sink.add(
                "assets/alcoholic/models/item/beverage_bottle.json",
                """
                        {
                          "parent": "alcoholic:item/beverage_bottle_generic",
                          "overrides": [
                            { "predicate": { "alcoholic:bottle_style": 1.0 }, "model": "alcoholic:item/beverage_bottle_red_wine" },
                            { "predicate": { "alcoholic:bottle_style": 2.0 }, "model": "alcoholic:item/beverage_bottle_white_wine" },
                            { "predicate": { "alcoholic:bottle_style": 3.0 }, "model": "alcoholic:item/beverage_bottle_beer" }
                          ]
                        }
                        """
        );
        addBottleModel(sink, "beverage_bottle_generic", "minecraft:block/water_still", "minecraft:block/black_concrete");
        addBottleModel(sink, "beverage_bottle_red_wine", "minecraft:block/redstone_block", "minecraft:block/dark_oak_planks");
        addBottleModel(sink, "beverage_bottle_white_wine", "minecraft:block/yellow_concrete", "minecraft:block/birch_planks");
        addBottleModel(sink, "beverage_bottle_beer", "minecraft:block/orange_concrete", "minecraft:block/oak_planks");
    }

    private static void addStand(AlcoholicJsonProvider.JsonSink sink, String id) {
        sink.add(
                "assets/alcoholic/blockstates/" + id + ".json",
                """
                        {
                          "variants": {
                            "facing=north": { "model": "alcoholic:block/%1$s" },
                            "facing=east": { "model": "alcoholic:block/%1$s", "y": 90 },
                            "facing=south": { "model": "alcoholic:block/%1$s", "y": 180 },
                            "facing=west": { "model": "alcoholic:block/%1$s", "y": 270 }
                          }
                        }
                        """.formatted(id)
        );
        sink.add(
                "assets/alcoholic/models/item/" + id + ".json",
                """
                        { "parent": "alcoholic:block/%s" }
                        """.formatted(id)
        );
    }

    private static void addBottleModel(
            AlcoholicJsonProvider.JsonSink sink,
            String id,
            String liquidTexture,
            String corkTexture
    ) {
        sink.add(
                "assets/alcoholic/models/item/" + id + ".json",
                """
                        {
                          "parent": "minecraft:block/block",
                          "textures": {
                            "liquid": "%1$s",
                            "glass": "minecraft:block/glass",
                            "cork": "%2$s",
                            "particle": "%1$s"
                          },
                          "display": {
                            "thirdperson_righthand": { "rotation": [ 0, 90, 0 ], "translation": [ 0, 1, 0 ], "scale": [ 0.55, 0.55, 0.55 ] },
                            "thirdperson_lefthand": { "rotation": [ 0, 90, 0 ], "translation": [ 0, 1, 0 ], "scale": [ 0.55, 0.55, 0.55 ] },
                            "firstperson_righthand": { "rotation": [ 0, -90, 25 ], "translation": [ 1.13, 3.2, 1.13 ], "scale": [ 0.68, 0.68, 0.68 ] },
                            "firstperson_lefthand": { "rotation": [ 0, 90, -25 ], "translation": [ 1.13, 3.2, 1.13 ], "scale": [ 0.68, 0.68, 0.68 ] },
                            "ground": { "translation": [ 0, 2, 0 ], "scale": [ 0.32, 0.32, 0.32 ] },
                            "gui": { "rotation": [ 25, 225, 0 ], "translation": [ 0, 1, 0 ], "scale": [ 0.75, 0.75, 0.75 ] },
                            "fixed": { "translation": [ 0, 0, 0 ], "scale": [ 0.52, 0.52, 0.52 ] }
                          },
                          "elements": [
                            { "from": [ 5, 0, 5 ], "to": [ 11, 10, 11 ], "faces": { "north": { "texture": "#liquid" }, "south": { "texture": "#liquid" }, "east": { "texture": "#liquid" }, "west": { "texture": "#liquid" }, "up": { "texture": "#liquid" }, "down": { "texture": "#liquid" } } },
                            { "from": [ 4.5, 0, 4.5 ], "to": [ 11.5, 10.5, 11.5 ], "faces": { "north": { "texture": "#glass" }, "south": { "texture": "#glass" }, "east": { "texture": "#glass" }, "west": { "texture": "#glass" } } },
                            { "from": [ 6.5, 10, 6.5 ], "to": [ 9.5, 15, 9.5 ], "faces": { "north": { "texture": "#glass" }, "south": { "texture": "#glass" }, "east": { "texture": "#glass" }, "west": { "texture": "#glass" }, "up": { "texture": "#glass" } } },
                            { "from": [ 6, 15, 6 ], "to": [ 10, 16, 10 ], "faces": { "north": { "texture": "#cork" }, "south": { "texture": "#cork" }, "east": { "texture": "#cork" }, "west": { "texture": "#cork" }, "up": { "texture": "#cork" }, "down": { "texture": "#cork" } } }
                          ]
                        }
                        """.formatted(liquidTexture, corkTexture)
        );
    }
}
