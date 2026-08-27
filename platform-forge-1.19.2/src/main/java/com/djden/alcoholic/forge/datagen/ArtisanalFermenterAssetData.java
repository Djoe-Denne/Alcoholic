package com.djden.alcoholic.forge.datagen;

final class ArtisanalFermenterAssetData {
    private ArtisanalFermenterAssetData() {
    }

    static void add(AlcoholicJsonProvider.JsonSink sink) {
        sink.add(
                "assets/alcoholic/blockstates/artisanal_fermenter.json",
                """
                        {
                          "variants": {
                            "facing=north,open=false": { "model": "alcoholic:block/artisanal_fermenter" },
                            "facing=south,open=false": { "model": "alcoholic:block/artisanal_fermenter", "y": 180 },
                            "facing=west,open=false": { "model": "alcoholic:block/artisanal_fermenter", "y": 270 },
                            "facing=east,open=false": { "model": "alcoholic:block/artisanal_fermenter", "y": 90 },
                            "facing=north,open=true": { "model": "alcoholic:block/artisanal_fermenter_open" },
                            "facing=south,open=true": { "model": "alcoholic:block/artisanal_fermenter_open", "y": 180 },
                            "facing=west,open=true": { "model": "alcoholic:block/artisanal_fermenter_open", "y": 270 },
                            "facing=east,open=true": { "model": "alcoholic:block/artisanal_fermenter_open", "y": 90 }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/item/artisanal_fermenter.json",
                """
                        {
                          "parent": "alcoholic:block/artisanal_fermenter"
                        }
                        """
        );
    }
}
