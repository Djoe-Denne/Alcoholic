package com.djden.alcoholic.forge.datagen;

final class MachineWindowAssetData {
    private MachineWindowAssetData() {
    }

    static void add(AlcoholicJsonProvider.JsonSink sink) {
        sink.add(
                "assets/alcoholic/blockstates/machine_window.json",
                """
                        {
                          "variants": {
                            "facing=north": { "model": "alcoholic:block/machine_window" },
                            "facing=south": { "model": "alcoholic:block/machine_window", "y": 180 },
                            "facing=west": { "model": "alcoholic:block/machine_window", "y": 270 },
                            "facing=east": { "model": "alcoholic:block/machine_window", "y": 90 }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/item/machine_window.json",
                """
                        {
                          "parent": "alcoholic:block/machine_window"
                        }
                        """
        );
    }
}
