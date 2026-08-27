package com.djden.alcoholic.forge.datagen;

final class OakBarrelAssetData {
    private OakBarrelAssetData() {
    }

    static void add(AlcoholicJsonProvider.JsonSink sink) {
        sink.add(
                "assets/alcoholic/blockstates/oak_barrel.json",
                """
                        {
                          "variants": {
                            "facing=north": { "model": "alcoholic:block/oak_barrel" },
                            "facing=south": { "model": "alcoholic:block/oak_barrel", "y": 180 },
                            "facing=west": { "model": "alcoholic:block/oak_barrel", "y": 270 },
                            "facing=east": { "model": "alcoholic:block/oak_barrel", "y": 90 }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/item/oak_barrel.json",
                """
                        {
                          "parent": "alcoholic:block/oak_barrel"
                        }
                        """
        );
    }
}
