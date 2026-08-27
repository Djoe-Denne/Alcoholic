package com.djden.alcoholic.forge.datagen;

final class ArtisanalPressAssetData {
    private ArtisanalPressAssetData() {
    }

    static void add(AlcoholicJsonProvider.JsonSink sink) {
        sink.add(
                "assets/alcoholic/blockstates/artisanal_press.json",
                """
                        {
                          "variants": {
                            "facing=north": { "model": "alcoholic:block/artisanal_press" },
                            "facing=south": { "model": "alcoholic:block/artisanal_press", "y": 180 },
                            "facing=west": { "model": "alcoholic:block/artisanal_press", "y": 270 },
                            "facing=east": { "model": "alcoholic:block/artisanal_press", "y": 90 }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/item/artisanal_press.json",
                """
                        {
                          "parent": "alcoholic:block/artisanal_press"
                        }
                        """
        );
    }
}
