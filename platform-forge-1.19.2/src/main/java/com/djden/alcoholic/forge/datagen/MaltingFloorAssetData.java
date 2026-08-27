package com.djden.alcoholic.forge.datagen;

final class MaltingFloorAssetData {
    private MaltingFloorAssetData() {
    }

    static void add(AlcoholicJsonProvider.JsonSink sink) {
        sink.add(
                "assets/alcoholic/blockstates/malting_floor.json",
                """
                        {
                          "variants": {
                            "": { "model": "alcoholic:block/malting_floor" }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/item/malting_floor.json",
                """
                        {
                          "parent": "alcoholic:block/malting_floor"
                        }
                        """
        );
    }
}
