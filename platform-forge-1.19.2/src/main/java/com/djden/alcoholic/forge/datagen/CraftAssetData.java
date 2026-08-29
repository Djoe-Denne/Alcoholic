package com.djden.alcoholic.forge.datagen;

final class CraftAssetData {
    private CraftAssetData() {
    }

    static void add(AlcoholicJsonProvider.JsonSink sink) {
        sink.add(
                "assets/alcoholic/blockstates/craft_casing.json",
                """
                        {
                          "variants": {
                            "formed=false": { "model": "alcoholic:block/craft_casing" },
                            "formed=true": { "model": "alcoholic:block/craft_casing" }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/item/craft_casing.json",
                """
                        {
                          "parent": "alcoholic:block/craft_casing"
                        }
                        """
        );
        for (String name : new String[]{
                "craft_malt_house_controller",
                "craft_mill_controller",
                "craft_mash_tun_controller",
                "craft_brewing_kettle_controller",
                "craft_vat_controller"
        }) {
            addHandmadeController(sink, name);
        }
    }

    private static void addHandmadeController(AlcoholicJsonProvider.JsonSink sink, String name) {
        sink.add(
                "assets/alcoholic/blockstates/" + name + ".json",
                """
                        {
                          "variants": {
                            "": { "model": "alcoholic:block/%s" }
                          }
                        }
                        """.formatted(name)
        );
        sink.add(
                "assets/alcoholic/models/item/" + name + ".json",
                """
                        {
                          "parent": "alcoholic:block/%s"
                        }
                        """.formatted(name)
        );
    }
}
