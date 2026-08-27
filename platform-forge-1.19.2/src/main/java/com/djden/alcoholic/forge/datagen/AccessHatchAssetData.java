package com.djden.alcoholic.forge.datagen;

final class AccessHatchAssetData {
    private AccessHatchAssetData() {
    }

    static void add(AlcoholicJsonProvider.JsonSink sink) {
        sink.add(
                "assets/alcoholic/blockstates/access_hatch.json",
                """
                        {
                          "variants": {
                            "facing=north": { "model": "alcoholic:block/access_hatch" },
                            "facing=south": { "model": "alcoholic:block/access_hatch", "y": 180 },
                            "facing=west": { "model": "alcoholic:block/access_hatch", "y": 270 },
                            "facing=east": { "model": "alcoholic:block/access_hatch", "y": 90 }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/item/access_hatch.json",
                """
                        {
                          "parent": "alcoholic:block/access_hatch"
                        }
                        """
        );
    }
}
