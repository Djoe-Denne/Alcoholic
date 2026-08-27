package com.djden.alcoholic.forge.datagen;

final class ArtisanalBlendingCrockAssetData {
    private ArtisanalBlendingCrockAssetData() {
    }

    static void add(AlcoholicJsonProvider.JsonSink sink) {
        sink.add(
                "assets/alcoholic/blockstates/artisanal_blending_crock.json",
                """
                        {
                          "variants": {
                            "facing=north,open=false": { "model": "alcoholic:block/artisanal_blending_crock" },
                            "facing=south,open=false": { "model": "alcoholic:block/artisanal_blending_crock", "y": 180 },
                            "facing=west,open=false": { "model": "alcoholic:block/artisanal_blending_crock", "y": 270 },
                            "facing=east,open=false": { "model": "alcoholic:block/artisanal_blending_crock", "y": 90 },
                            "facing=north,open=true": { "model": "alcoholic:block/artisanal_blending_crock_open" },
                            "facing=south,open=true": { "model": "alcoholic:block/artisanal_blending_crock_open", "y": 180 },
                            "facing=west,open=true": { "model": "alcoholic:block/artisanal_blending_crock_open", "y": 270 },
                            "facing=east,open=true": { "model": "alcoholic:block/artisanal_blending_crock_open", "y": 90 }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/item/artisanal_blending_crock.json",
                """
                        {
                          "parent": "alcoholic:block/artisanal_blending_crock"
                        }
                        """
        );
    }
}
