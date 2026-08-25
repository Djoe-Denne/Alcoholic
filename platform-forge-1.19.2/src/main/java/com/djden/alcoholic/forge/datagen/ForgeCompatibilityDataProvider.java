package com.djden.alcoholic.forge.datagen;

import java.nio.file.Path;

final class ForgeCompatibilityDataProvider extends AlcoholicJsonProvider {
    ForgeCompatibilityDataProvider(Path outputRoot) {
        super(outputRoot);
    }

    @Override
    protected void collectJson(JsonSink sink) {
        sink.add(
                "data/alcoholic/forge/biome_modifier/wild_grapevines.json",
                """
                        {
                          "forge:conditions": [
                            {
                              "type": "forge:not",
                              "value": {
                                "type": "forge:mod_loaded",
                                "modid": "vinery"
                              }
                            }
                          ],
                          "type": "forge:add_features",
                          "biomes": "#alcoholic:has_wild_grapevines",
                          "features": [
                            "alcoholic:wild_red_grapevines",
                            "alcoholic:wild_white_grapevines"
                          ],
                          "step": "vegetal_decoration"
                        }
                        """
        );
        sink.add(
                "data/alcoholic/forge/biome_modifier/wild_barley.json",
                """
                        {
                          "forge:conditions": [
                            {
                              "type": "forge:not",
                              "value": {
                                "type": "alcoholic:item_present",
                                "item": "brewery:barley"
                              }
                            }
                          ],
                          "type": "forge:add_features",
                          "biomes": "#alcoholic:has_wild_barley",
                          "features": [
                            "alcoholic:wild_barley"
                          ],
                          "step": "vegetal_decoration"
                        }
                        """
        );
    }

    @Override
    public String getName() {
        return "Alcoholic Forge compatibility data";
    }
}
