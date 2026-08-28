package com.djden.alcoholic.forge.datagen;

final class IndustrialPressControllerAssetData {
    private IndustrialPressControllerAssetData() {
    }

    static void add(AlcoholicJsonProvider.JsonSink sink) {
        sink.add(
                "assets/alcoholic/blockstates/industrial_press_controller.json",
                """
                        {
                          "variants": {
                            "formed=false": { "model": "alcoholic:block/industrial_press_controller" },
                            "formed=true": { "model": "alcoholic:block/industrial_press_controller" }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/item/industrial_press_controller.json",
                """
                        {
                          "parent": "alcoholic:block/industrial_press_controller"
                        }
                        """
        );
    }
}
