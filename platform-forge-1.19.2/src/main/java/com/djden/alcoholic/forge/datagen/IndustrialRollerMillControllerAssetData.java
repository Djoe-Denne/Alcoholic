package com.djden.alcoholic.forge.datagen;

final class IndustrialRollerMillControllerAssetData {
    private IndustrialRollerMillControllerAssetData() {
    }

    static void add(AlcoholicJsonProvider.JsonSink sink) {
        sink.add(
                "assets/alcoholic/blockstates/industrial_roller_mill_controller.json",
                """
                        {
                          "variants": {
                            "formed=false": { "model": "alcoholic:block/industrial_roller_mill_controller" },
                            "formed=true": { "model": "alcoholic:block/industrial_roller_mill_controller" }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/item/industrial_roller_mill_controller.json",
                """
                        {
                          "parent": "alcoholic:block/industrial_roller_mill_controller"
                        }
                        """
        );
    }
}
