package com.djden.alcoholic.forge.datagen;

final class BrewingKettleAssetData {
    private BrewingKettleAssetData() {
    }

    static void add(AlcoholicJsonProvider.JsonSink sink) {
        sink.add(
                "assets/alcoholic/blockstates/brewing_kettle.json",
                """
                        {
                          "variants": {
                            "facing=north": { "model": "alcoholic:block/brewing_kettle" },
                            "facing=south": { "model": "alcoholic:block/brewing_kettle", "y": 180 },
                            "facing=west": { "model": "alcoholic:block/brewing_kettle", "y": 270 },
                            "facing=east": { "model": "alcoholic:block/brewing_kettle", "y": 90 }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/item/brewing_kettle.json",
                """
                        {
                          "parent": "alcoholic:block/brewing_kettle"
                        }
                        """
        );
    }
}
