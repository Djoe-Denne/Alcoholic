package com.djden.alcoholic.forge.datagen;

final class FluidPortAssetData {
    private FluidPortAssetData() {
    }

    static void add(AlcoholicJsonProvider.JsonSink sink) {
        sink.add(
                "assets/alcoholic/blockstates/fluid_port.json",
                """
                        {
                          "variants": {
                            "facing=north,mode=input": { "model": "alcoholic:block/fluid_port" },
                            "facing=north,mode=output": { "model": "alcoholic:block/fluid_port" },
                            "facing=north,mode=both": { "model": "alcoholic:block/fluid_port" },
                            "facing=south,mode=input": { "model": "alcoholic:block/fluid_port", "y": 180 },
                            "facing=south,mode=output": { "model": "alcoholic:block/fluid_port", "y": 180 },
                            "facing=south,mode=both": { "model": "alcoholic:block/fluid_port", "y": 180 },
                            "facing=west,mode=input": { "model": "alcoholic:block/fluid_port", "y": 270 },
                            "facing=west,mode=output": { "model": "alcoholic:block/fluid_port", "y": 270 },
                            "facing=west,mode=both": { "model": "alcoholic:block/fluid_port", "y": 270 },
                            "facing=east,mode=input": { "model": "alcoholic:block/fluid_port", "y": 90 },
                            "facing=east,mode=output": { "model": "alcoholic:block/fluid_port", "y": 90 },
                            "facing=east,mode=both": { "model": "alcoholic:block/fluid_port", "y": 90 }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/item/fluid_port.json",
                """
                        {
                          "parent": "alcoholic:block/fluid_port"
                        }
                        """
        );
    }
}
