package com.djden.alcoholic.forge.datagen;

final class MaltMillAssetData {
    private MaltMillAssetData() {
    }

    static void add(AlcoholicJsonProvider.JsonSink sink) {
        sink.add(
                "assets/alcoholic/blockstates/malt_mill.json",
                """
                        {
                          "variants": {
                            "facing=north": { "model": "alcoholic:block/malt_mill" },
                            "facing=south": { "model": "alcoholic:block/malt_mill", "y": 180 },
                            "facing=west": { "model": "alcoholic:block/malt_mill", "y": 270 },
                            "facing=east": { "model": "alcoholic:block/malt_mill", "y": 90 }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/item/malt_mill.json",
                """
                        {
                          "parent": "alcoholic:block/malt_mill"
                        }
                        """
        );
    }
}
