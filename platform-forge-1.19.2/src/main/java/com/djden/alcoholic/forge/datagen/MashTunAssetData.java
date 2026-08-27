package com.djden.alcoholic.forge.datagen;

final class MashTunAssetData {
    private MashTunAssetData() {
    }

    static void add(AlcoholicJsonProvider.JsonSink sink) {
        sink.add(
                "assets/alcoholic/blockstates/mash_tun.json",
                """
                        {
                          "variants": {
                            "facing=north,open=false": { "model": "alcoholic:block/mash_tun" },
                            "facing=south,open=false": { "model": "alcoholic:block/mash_tun", "y": 180 },
                            "facing=west,open=false": { "model": "alcoholic:block/mash_tun", "y": 270 },
                            "facing=east,open=false": { "model": "alcoholic:block/mash_tun", "y": 90 },
                            "facing=north,open=true": { "model": "alcoholic:block/mash_tun_open" },
                            "facing=south,open=true": { "model": "alcoholic:block/mash_tun_open", "y": 180 },
                            "facing=west,open=true": { "model": "alcoholic:block/mash_tun_open", "y": 270 },
                            "facing=east,open=true": { "model": "alcoholic:block/mash_tun_open", "y": 90 }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/item/mash_tun.json",
                """
                        {
                          "parent": "alcoholic:block/mash_tun"
                        }
                        """
        );
    }
}
