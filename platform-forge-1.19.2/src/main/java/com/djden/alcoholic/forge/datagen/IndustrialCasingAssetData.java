package com.djden.alcoholic.forge.datagen;

final class IndustrialCasingAssetData {
    private IndustrialCasingAssetData() {
    }

    static void add(AlcoholicJsonProvider.JsonSink sink) {
        sink.add(
                "assets/alcoholic/blockstates/industrial_casing.json",
                """
                        {
                          "variants": {
                            "": { "model": "alcoholic:block/industrial_casing" }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/item/industrial_casing.json",
                """
                        {
                          "parent": "alcoholic:block/industrial_casing"
                        }
                        """
        );
    }
}
