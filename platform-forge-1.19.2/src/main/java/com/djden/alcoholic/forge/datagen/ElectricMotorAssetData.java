package com.djden.alcoholic.forge.datagen;

final class ElectricMotorAssetData {
    private ElectricMotorAssetData() {
    }

    static void add(AlcoholicJsonProvider.JsonSink sink) {
        sink.add(
                "assets/alcoholic/blockstates/electric_motor.json",
                """
                        {
                          "variants": {
                            "facing=north,lit=false": { "model": "alcoholic:block/electric_motor" },
                            "facing=south,lit=false": { "model": "alcoholic:block/electric_motor", "y": 180 },
                            "facing=west,lit=false": { "model": "alcoholic:block/electric_motor", "y": 270 },
                            "facing=east,lit=false": { "model": "alcoholic:block/electric_motor", "y": 90 },
                            "facing=north,lit=true": { "model": "alcoholic:block/electric_motor_on" },
                            "facing=south,lit=true": { "model": "alcoholic:block/electric_motor_on", "y": 180 },
                            "facing=west,lit=true": { "model": "alcoholic:block/electric_motor_on", "y": 270 },
                            "facing=east,lit=true": { "model": "alcoholic:block/electric_motor_on", "y": 90 }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/item/electric_motor.json",
                """
                        {
                          "parent": "alcoholic:block/electric_motor"
                        }
                        """
        );
    }
}
