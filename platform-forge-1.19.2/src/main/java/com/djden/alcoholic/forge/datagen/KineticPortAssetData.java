package com.djden.alcoholic.forge.datagen;

final class KineticPortAssetData {
    private KineticPortAssetData() {
    }

    static void add(AlcoholicJsonProvider.JsonSink sink) {
        sink.add(
                "assets/alcoholic/blockstates/kinetic_port.json",
                """
                        {
                          "variants": {
                            "facing=north": { "model": "alcoholic:block/kinetic_port" },
                            "facing=south": { "model": "alcoholic:block/kinetic_port", "y": 180 },
                            "facing=west": { "model": "alcoholic:block/kinetic_port", "y": 270 },
                            "facing=east": { "model": "alcoholic:block/kinetic_port", "y": 90 }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/item/kinetic_port.json",
                """
                        {
                          "parent": "alcoholic:block/kinetic_port"
                        }
                        """
        );
    }
}
