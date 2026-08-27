package com.djden.alcoholic.forge.datagen;

import java.nio.file.Path;

final class GrapeAssetDataProvider extends AlcoholicJsonProvider {
    private static final String[] STAGES = {
            "planted",
            "establishing",
            "vegetative",
            "flowering",
            "green_fruit",
            "ripening",
            "harvest_ready",
            "dormant"
    };

    GrapeAssetDataProvider(Path outputRoot) {
        super(outputRoot);
    }

    @Override
    protected void collectJson(JsonSink sink) {
        addVineAssets(sink, "red");
        addVineAssets(sink, "white");

        addGeneratedItem(sink, "red_grapes");
        addGeneratedItem(sink, "white_grapes");
        addGeneratedItem(sink, "red_grape_cutting");
        addGeneratedItem(sink, "white_grape_cutting");
        addInfrastructureAssets(sink);
        addProcessingAssets(sink);
        addIndustrialAssets(sink);
        addGrainAssets(sink);

        sink.add(
                "assets/alcoholic/lang/en_us.json",
                """
                        {
                          "block.alcoholic.red_grapevine": "Red Grapevine",
                          "block.alcoholic.white_grapevine": "White Grapevine",
                          "block.alcoholic.red_grapevine_stem": "Red Grapevine Stem",
                          "block.alcoholic.white_grapevine_stem": "White Grapevine Stem",
                          "block.alcoholic.red_grapevine_canopy": "Red Grapevine Canopy",
                          "block.alcoholic.white_grapevine_canopy": "White Grapevine Canopy",
                          "block.alcoholic.vineyard_post": "Vineyard Post",
                          "block.alcoholic.end_post": "Vineyard End Post",
                          "block.alcoholic.trellis_wire": "Trellis Wire",
                          "item.alcoholic.red_grapes": "Red Grapes",
                          "item.alcoholic.white_grapes": "White Grapes",
                          "item.alcoholic.red_grape_cutting": "Red Grapevine Cutting",
                          "item.alcoholic.white_grape_cutting": "White Grapevine Cutting",
                          "item.alcoholic.trellis_spool": "Trellis Wire Spool",
                          "item.alcoholic.pruning_shears": "Pruning Shears",
                          "message.alcoholic.vine.pruned": "Vine pruned: %s",
                          "message.alcoholic.vine.no_harvest_item": "No grape item is available for this variety",
                          "message.alcoholic.vine.inspect": "Climate: %1$s · Health: %2$s · Maturity: %3$s %4$s%% · Pruning: %5$s",
                          "message.alcoholic.vine.climate.poor": "poor",
                          "message.alcoholic.vine.climate.average": "average",
                          "message.alcoholic.vine.climate.good": "good",
                          "message.alcoholic.vine.climate.ideal": "ideal",
                          "message.alcoholic.vine.health.thriving": "thriving",
                          "message.alcoholic.vine.health.healthy": "healthy",
                          "message.alcoholic.vine.health.stressed": "stressed",
                          "message.alcoholic.vine.health.poor": "poor",
                          "message.alcoholic.vine.stage.planted": "planted",
                          "message.alcoholic.vine.stage.establishing": "establishing",
                          "message.alcoholic.vine.stage.vegetative": "vegetative",
                          "message.alcoholic.vine.stage.flowering": "flowering",
                          "message.alcoholic.vine.stage.green_fruit": "green fruit",
                          "message.alcoholic.vine.stage.ripening": "ripening",
                          "message.alcoholic.vine.stage.harvest_ready": "harvest ready",
                          "message.alcoholic.vine.stage.dormant": "dormant",
                          "message.alcoholic.vine.pruning.light": "light",
                          "message.alcoholic.vine.pruning.balanced": "balanced",
                          "message.alcoholic.vine.pruning.severe": "severe",
                          "message.alcoholic.pruning_shears.selected": "Selected pruning: %s",
                          "tooltip.alcoholic.pruning_shears.level": "Pruning: %s",
                          "tooltip.alcoholic.pruning_shears.change": "Sneak + right-click in the air to change",
                          "message.alcoholic.trellis_spool.first_post": "First post selected",
                          "message.alcoholic.trellis_spool.wrong_dimension": "Selection cleared: different dimension",
                          "message.alcoholic.trellis_spool.missing_post": "Selection cleared: the first post is gone",
                          "message.alcoholic.trellis_spool.invalid_alignment": "Posts must be aligned at the same height and within range",
                          "message.alcoholic.trellis_spool.obstructed": "The wire path is obstructed",
                          "message.alcoholic.trellis_spool.insufficient_durability": "The spool lacks enough durability",
                          "message.alcoholic.trellis_spool.placed": "%s wire segment(s) placed",
                          "tooltip.alcoholic.harvest_lot.quality": "Quality: %s",
                          "tooltip.alcoholic.harvest_lot.sugar": "Sugar: %s",
                          "tooltip.alcoholic.harvest_lot.acidity": "Acidity: %s",
                          "tooltip.alcoholic.harvest_lot.quality.poor": "poor",
                          "tooltip.alcoholic.harvest_lot.quality.average": "average",
                          "tooltip.alcoholic.harvest_lot.quality.good": "good",
                          "tooltip.alcoholic.harvest_lot.quality.exceptional": "exceptional",
                          "tooltip.alcoholic.harvest_lot.level.low": "low",
                          "tooltip.alcoholic.harvest_lot.level.balanced": "balanced",
                          "tooltip.alcoholic.harvest_lot.level.high": "high",
                          "block.alcoholic.artisanal_press": "Artisanal Press",
                          "block.alcoholic.artisanal_fermenter": "Artisanal Fermenter",
                          "item.alcoholic.yeast": "Yeast",
                          "item.alcoholic.grape_pomace": "Grape Pomace",
                          "item.alcoholic.red_grape_must_bucket": "Red Grape Must Bucket",
                          "item.alcoholic.white_grape_must_bucket": "White Grape Must Bucket",
                          "item.alcoholic.young_red_wine_bucket": "Young Red Wine Bucket",
                          "item.alcoholic.young_white_wine_bucket": "Young White Wine Bucket",
                          "fluid_type.alcoholic.red_grape_must": "Red Grape Must",
                          "fluid_type.alcoholic.white_grape_must": "White Grape Must",
                          "fluid_type.alcoholic.young_red_wine": "Young Red Wine",
                          "fluid_type.alcoholic.young_white_wine": "Young White Wine",
                          "message.alcoholic.press.status": "Pressing %1$s/%2$s · %3$s mB",
                          "message.alcoholic.fermenter.empty": "Empty fermenter",
                          "message.alcoholic.fermenter.status": "Temp %1$s°C · sugar %2$s · ethanol %3$s · yeast %4$s",
                          "block.alcoholic.oak_barrel": "Oak Barrel",
                          "block.alcoholic.artisanal_blending_crock": "Artisanal Blending Crock",
                          "item.alcoholic.empty_bottle": "Empty Bottle",
                          "item.alcoholic.beverage_bottle": "Beverage Bottle",
                          "item.alcoholic.red_wine_bucket": "Red Wine Bucket",
                          "item.alcoholic.white_wine_bucket": "White Wine Bucket",
                          "fluid_type.alcoholic.red_wine": "Red Wine",
                          "fluid_type.alcoholic.white_wine": "White Wine",
                          "message.alcoholic.barrel.empty": "Empty barrel · used %1$s · last %2$s",
                          "message.alcoholic.barrel.status": "Temp %1$s°C · maturity %2$s · %3$s",
                          "message.alcoholic.crock.status": "Crock %1$s + %2$s mB",
                          "message.alcoholic.crock.need_two": "The crock needs two liquids to blend",
                          "message.alcoholic.crock.no_recipe": "No blend definition matches these liquids",
                          "message.alcoholic.crock.rejected": "Blend rejected: %s",
                          "message.alcoholic.crock.blended": "Liquids blended",
                          "tooltip.alcoholic.bottle.ethanol": "Ethanol: %s",
                          "tooltip.alcoholic.bottle.maturity": "Maturity: %s",
                          "tooltip.alcoholic.bottle.debug": "Sugar %1$s · acidity %2$s · quality %3$s",
                          "tooltip.alcoholic.metadata.lost": "Batch metadata was normalized by foreign storage",
                          "command.alcoholic.inspect.nothing": "Look at a vessel or hold a bottled beverage",
                          "command.alcoholic.inspect.no_player": "Inspect requires a player",
                          "command.alcoholic.debug.kit.no_player": "Debug kits require a player",
                          "command.alcoholic.debug.kit.given": "Prepared %1$s items for %2$s (%3$s optional entries unavailable)",
                          "command.alcoholic.debug.kit.wine_agriculture": "wine agriculture",
                          "command.alcoholic.debug.kit.beer_agriculture": "beer agriculture",
                          "command.alcoholic.debug.kit.wine_artisanal": "artisanal winemaking",
                          "command.alcoholic.debug.kit.beer_artisanal": "artisanal brewing",
                          "command.alcoholic.debug.kit.wine_industrial": "industrial winemaking",
                          "command.alcoholic.debug.kit.beer_industrial": "industrial brewing",
                          "command.alcoholic.debug.place.line": "Placed %1$s beer line at %2$s (%3$s machines, %4$s formed)",
                          "command.alcoholic.debug.place.machine": "Placed %1$s at %2$s · formed=%3$s · %4$s",
                          "command.alcoholic.debug.place.unknown": "Unknown machine '%s'",
                          "block.alcoholic.industrial_casing": "Industrial Casing",
                          "block.alcoholic.machine_window": "Machine Window",
                          "block.alcoholic.access_hatch": "Access Hatch",
                          "block.alcoholic.fluid_port": "Fluid Port",
                          "block.alcoholic.item_port": "Item Port",
                          "block.alcoholic.kinetic_port": "Kinetic Port",
                          "block.alcoholic.industrial_press_controller": "Industrial Press Controller",
                          "block.alcoholic.industrial_vat_controller": "Industrial Fermentation Vat Controller",
                          "block.alcoholic.industrial_tank_controller": "Industrial Storage Tank Controller",
                          "block.alcoholic.industrial_malt_house_controller": "Industrial Malt House Controller",
                          "block.alcoholic.industrial_roller_mill_controller": "Industrial Roller Mill Controller",
                          "block.alcoholic.industrial_mash_tun_controller": "Industrial Mash Tun Controller",
                          "block.alcoholic.industrial_brewing_kettle_controller": "Industrial Brewing Kettle Controller",
                          "block.alcoholic.industrial_conditioning_vessel_controller": "Industrial Conditioning Vessel Controller",
                          "message.alcoholic.port.mode": "Port mode: %s",
                          "death.attack.alcoholic.industrial_press": "%1$s was crushed in an industrial press",
                          "block.alcoholic.barley_crop": "Barley",
                          "block.alcoholic.hop_bine": "Hop Bine",
                          "block.alcoholic.malting_floor": "Malting Floor",
                          "block.alcoholic.mash_tun": "Mash Tun",
                          "block.alcoholic.brewing_kettle": "Brewing Kettle",
                          "block.alcoholic.malt_mill": "Malt Mill",
                          "block.alcoholic.primitive_combustion_engine": "Primitive Combustion Engine",
                          "block.alcoholic.electric_motor": "Electric Motor",
                          "item.alcoholic.barley": "Barley",
                          "item.alcoholic.barley_seeds": "Barley Seeds",
                          "item.alcoholic.malted_barley": "Malted Barley",
                          "item.alcoholic.grist": "Grist",
                          "item.alcoholic.hops": "Hops",
                          "item.alcoholic.hop_rhizome": "Hop Rhizome",
                          "item.alcoholic.spent_grain": "Spent Grain",
                          "item.alcoholic.wort_bucket": "Wort Bucket",
                          "item.alcoholic.hopped_wort_bucket": "Hopped Wort Bucket",
                          "item.alcoholic.beer_bucket": "Beer Bucket",
                          "fluid_type.alcoholic.wort": "Wort",
                          "fluid_type.alcoholic.hopped_wort": "Hopped Wort",
                          "fluid_type.alcoholic.beer": "Beer",
                          "message.alcoholic.malting.status": "Malting %1$s/%2$s (%3$s)",
                          "message.alcoholic.mash.status": "Mash %1$s°C · %2$s/%3$s",
                          "message.alcoholic.boil.status": "Boil %1$s°C · %2$s/%3$s",
                          "message.alcoholic.mill.status": "Milling %1$s/%2$s · drive %3$s",
                          "message.alcoholic.engine.status": "Engine speed %1$s · burn %2$s/%3$s",
                          "message.alcoholic.electric_motor.status": "Motor speed %1$s · FE %2$s/%3$s",
                          "container.alcoholic.machine": "Machine",
                          "container.alcoholic.two_slots": "Processor",
                          "container.alcoholic.two_slots_one_tank": "Press",
                          "container.alcoholic.two_slots_two_tanks": "Mash Tun",
                          "container.alcoholic.one_slot_one_tank": "Kettle",
                          "container.alcoholic.one_tank": "Vessel",
                          "container.alcoholic.two_tanks": "Blending Crock",
                          "container.alcoholic.fuel": "Combustion Engine",
                          "container.alcoholic.energy": "Electric Motor",
                          "gui.alcoholic.temperature": "%s°C",
                          "gui.alcoholic.drive": "Drive %s",
                          "jei.alcoholic.category.mill": "Milling",
                          "jei.alcoholic.category.mash": "Mashing",
                          "jei.alcoholic.category.boil": "Boiling",
                          "jei.alcoholic.category.malt": "Malting",
                          "jei.alcoholic.category.press": "Pressing",
                          "jei.alcoholic.category.ferment": "Fermentation",
                          "jei.alcoholic.category.age": "Aging",
                          "jei.alcoholic.category.blend": "Blending",
                          "jei.alcoholic.category.condition": "Conditioning",
                          "jei.alcoholic.category.bottle": "Bottling",
                          "jei.alcoholic.category.addon": "%s",
                          "jei.alcoholic.duration": "%ss",
                          "jei.alcoholic.volume.unspecified": "Volume not specified",
                          "tooltip.alcoholic.gauge.empty": "Empty",
                          "tooltip.alcoholic.gauge.fluid": "%1$s · %2$s / %3$s mB",
                          "tooltip.alcoholic.gauge.energy": "%1$s / %2$s FE"
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/lang/fr_fr.json",
                """
                        {
                          "block.alcoholic.red_grapevine": "Vigne rouge",
                          "block.alcoholic.white_grapevine": "Vigne blanche",
                          "block.alcoholic.red_grapevine_stem": "Tige de vigne rouge",
                          "block.alcoholic.white_grapevine_stem": "Tige de vigne blanche",
                          "block.alcoholic.red_grapevine_canopy": "Canopée de vigne rouge",
                          "block.alcoholic.white_grapevine_canopy": "Canopée de vigne blanche",
                          "block.alcoholic.vineyard_post": "Poteau de vigne",
                          "block.alcoholic.end_post": "Poteau d'extrémité de vigne",
                          "block.alcoholic.trellis_wire": "Fil de palissage",
                          "item.alcoholic.red_grapes": "Raisins rouges",
                          "item.alcoholic.white_grapes": "Raisins blancs",
                          "item.alcoholic.red_grape_cutting": "Bouture de vigne rouge",
                          "item.alcoholic.white_grape_cutting": "Bouture de vigne blanche",
                          "item.alcoholic.trellis_spool": "Bobine de fil de palissage",
                          "item.alcoholic.pruning_shears": "Cisailles de taille",
                          "message.alcoholic.vine.pruned": "Vigne taillée : %s",
                          "message.alcoholic.vine.no_harvest_item": "Aucun raisin disponible pour cette variété",
                          "message.alcoholic.vine.inspect": "Climat : %1$s · Santé : %2$s · Maturité : %3$s %4$s%% · Taille : %5$s",
                          "message.alcoholic.vine.climate.poor": "mauvais",
                          "message.alcoholic.vine.climate.average": "moyen",
                          "message.alcoholic.vine.climate.good": "bon",
                          "message.alcoholic.vine.climate.ideal": "idéal",
                          "message.alcoholic.vine.health.thriving": "excellente",
                          "message.alcoholic.vine.health.healthy": "saine",
                          "message.alcoholic.vine.health.stressed": "stressée",
                          "message.alcoholic.vine.health.poor": "faible",
                          "message.alcoholic.vine.stage.planted": "plantée",
                          "message.alcoholic.vine.stage.establishing": "enracinement",
                          "message.alcoholic.vine.stage.vegetative": "végétative",
                          "message.alcoholic.vine.stage.flowering": "floraison",
                          "message.alcoholic.vine.stage.green_fruit": "fruits verts",
                          "message.alcoholic.vine.stage.ripening": "véraison",
                          "message.alcoholic.vine.stage.harvest_ready": "récolte prête",
                          "message.alcoholic.vine.stage.dormant": "dormante",
                          "message.alcoholic.vine.pruning.light": "légère",
                          "message.alcoholic.vine.pruning.balanced": "équilibrée",
                          "message.alcoholic.vine.pruning.severe": "sévère",
                          "message.alcoholic.pruning_shears.selected": "Taille sélectionnée : %s",
                          "tooltip.alcoholic.pruning_shears.level": "Taille : %s",
                          "tooltip.alcoholic.pruning_shears.change": "Maj + clic droit en l'air pour changer",
                          "message.alcoholic.trellis_spool.first_post": "Premier poteau sélectionné",
                          "message.alcoholic.trellis_spool.wrong_dimension": "Sélection effacée : dimension différente",
                          "message.alcoholic.trellis_spool.missing_post": "Sélection effacée : le premier poteau a disparu",
                          "message.alcoholic.trellis_spool.invalid_alignment": "Les poteaux doivent être alignés, au même niveau et à distance valide",
                          "message.alcoholic.trellis_spool.obstructed": "Le passage du fil est obstrué",
                          "message.alcoholic.trellis_spool.insufficient_durability": "La bobine n'a pas assez de durabilité",
                          "message.alcoholic.trellis_spool.placed": "%s segment(s) de fil posé(s)",
                          "tooltip.alcoholic.harvest_lot.quality": "Qualité : %s",
                          "tooltip.alcoholic.harvest_lot.sugar": "Sucre : %s",
                          "tooltip.alcoholic.harvest_lot.acidity": "Acidité : %s",
                          "tooltip.alcoholic.harvest_lot.quality.poor": "médiocre",
                          "tooltip.alcoholic.harvest_lot.quality.average": "moyenne",
                          "tooltip.alcoholic.harvest_lot.quality.good": "bonne",
                          "tooltip.alcoholic.harvest_lot.quality.exceptional": "exceptionnelle",
                          "tooltip.alcoholic.harvest_lot.level.low": "faible",
                          "tooltip.alcoholic.harvest_lot.level.balanced": "équilibré",
                          "tooltip.alcoholic.harvest_lot.level.high": "élevé",
                          "block.alcoholic.artisanal_press": "Pressoir artisanal",
                          "block.alcoholic.artisanal_fermenter": "Cuve de fermentation artisanale",
                          "item.alcoholic.yeast": "Levure",
                          "item.alcoholic.grape_pomace": "Marc de raisin",
                          "item.alcoholic.red_grape_must_bucket": "Seau de moût rouge",
                          "item.alcoholic.white_grape_must_bucket": "Seau de moût blanc",
                          "item.alcoholic.young_red_wine_bucket": "Seau de vin rouge jeune",
                          "item.alcoholic.young_white_wine_bucket": "Seau de vin blanc jeune",
                          "fluid_type.alcoholic.red_grape_must": "Moût de raisin rouge",
                          "fluid_type.alcoholic.white_grape_must": "Moût de raisin blanc",
                          "fluid_type.alcoholic.young_red_wine": "Vin rouge jeune",
                          "fluid_type.alcoholic.young_white_wine": "Vin blanc jeune",
                          "message.alcoholic.press.status": "Pressage %1$s/%2$s · %3$s mB",
                          "message.alcoholic.fermenter.empty": "Cuve vide",
                          "message.alcoholic.fermenter.status": "Temp. %1$s°C · sucre %2$s · éthanol %3$s · levure %4$s",
                          "block.alcoholic.oak_barrel": "Fût de chêne",
                          "block.alcoholic.artisanal_blending_crock": "Terrine d'assemblage artisanale",
                          "item.alcoholic.empty_bottle": "Bouteille vide",
                          "item.alcoholic.beverage_bottle": "Bouteille de boisson",
                          "item.alcoholic.red_wine_bucket": "Seau de vin rouge",
                          "item.alcoholic.white_wine_bucket": "Seau de vin blanc",
                          "fluid_type.alcoholic.red_wine": "Vin rouge",
                          "fluid_type.alcoholic.white_wine": "Vin blanc",
                          "message.alcoholic.barrel.empty": "Fût vide · usages %1$s · dernier %2$s",
                          "message.alcoholic.barrel.status": "Temp. %1$s°C · maturité %2$s · %3$s",
                          "message.alcoholic.crock.status": "Terrine %1$s + %2$s mB",
                          "message.alcoholic.crock.need_two": "Deux liquides sont requis pour assembler",
                          "message.alcoholic.crock.no_recipe": "Aucune définition d'assemblage pour ces liquides",
                          "message.alcoholic.crock.rejected": "Assemblage refusé : %s",
                          "message.alcoholic.crock.blended": "Liquides assemblés",
                          "tooltip.alcoholic.bottle.ethanol": "Éthanol : %s",
                          "tooltip.alcoholic.bottle.maturity": "Maturité : %s",
                          "tooltip.alcoholic.bottle.debug": "Sucre %1$s · acidité %2$s · qualité %3$s",
                          "tooltip.alcoholic.metadata.lost": "Les métadonnées du lot ont été normalisées par un stockage externe",
                          "command.alcoholic.inspect.nothing": "Visez un vaisseau ou tenez une bouteille",
                          "command.alcoholic.inspect.no_player": "Inspecter nécessite un joueur",
                          "command.alcoholic.debug.kit.no_player": "Les kits de débogage nécessitent un joueur",
                          "command.alcoholic.debug.kit.given": "%1$s objets préparés pour %2$s (%3$s entrées optionnelles indisponibles)",
                          "command.alcoholic.debug.kit.wine_agriculture": "l'agriculture viticole",
                          "command.alcoholic.debug.kit.beer_agriculture": "l'agriculture brassicole",
                          "command.alcoholic.debug.kit.wine_artisanal": "la production artisanale de vin",
                          "command.alcoholic.debug.kit.beer_artisanal": "la production artisanale de bière",
                          "command.alcoholic.debug.kit.wine_industrial": "la production industrielle de vin",
                          "command.alcoholic.debug.kit.beer_industrial": "la production industrielle de bière",
                          "command.alcoholic.debug.place.line": "Ligne bière %1$s placée en %2$s (%3$s machines, %4$s formées)",
                          "command.alcoholic.debug.place.machine": "Placé %1$s en %2$s · formed=%3$s · %4$s",
                          "command.alcoholic.debug.place.unknown": "Machine inconnue « %s »",
                          "block.alcoholic.industrial_casing": "Revêtement industriel",
                          "block.alcoholic.machine_window": "Hublot de machine",
                          "block.alcoholic.access_hatch": "Trappe d'accès",
                          "block.alcoholic.fluid_port": "Port fluide",
                          "block.alcoholic.item_port": "Port d'objets",
                          "block.alcoholic.kinetic_port": "Port cinétique",
                          "block.alcoholic.industrial_press_controller": "Contrôleur de pressoir industriel",
                          "block.alcoholic.industrial_vat_controller": "Contrôleur de cuve de fermentation industrielle",
                          "block.alcoholic.industrial_tank_controller": "Contrôleur de réservoir de stockage industriel",
                          "block.alcoholic.industrial_malt_house_controller": "Contrôleur de touraille industrielle",
                          "block.alcoholic.industrial_roller_mill_controller": "Contrôleur de broyeur à cylindres industriel",
                          "block.alcoholic.industrial_mash_tun_controller": "Contrôleur de cuve d'empâtage industrielle",
                          "block.alcoholic.industrial_brewing_kettle_controller": "Contrôleur de chaudière de brassage industrielle",
                          "block.alcoholic.industrial_conditioning_vessel_controller": "Contrôleur de cuve de conditionnement industrielle",
                          "message.alcoholic.port.mode": "Mode du port : %s",
                          "death.attack.alcoholic.industrial_press": "%1$s a été écrasé dans un pressoir industriel",
                          "block.alcoholic.barley_crop": "Orge",
                          "block.alcoholic.hop_bine": "Bine de houblon",
                          "block.alcoholic.malting_floor": "Aire de maltage",
                          "block.alcoholic.mash_tun": "Cuve de brassage",
                          "block.alcoholic.brewing_kettle": "Chaudron de houblonnage",
                          "block.alcoholic.malt_mill": "Broyeur à malt",
                          "block.alcoholic.primitive_combustion_engine": "Moteur à combustion primitif",
                          "block.alcoholic.electric_motor": "Moteur électrique",
                          "item.alcoholic.barley": "Orge",
                          "item.alcoholic.barley_seeds": "Graines d'orge",
                          "item.alcoholic.malted_barley": "Orge maltée",
                          "item.alcoholic.grist": "Mouture",
                          "item.alcoholic.hops": "Houblon",
                          "item.alcoholic.hop_rhizome": "Rhizome de houblon",
                          "item.alcoholic.spent_grain": "Drêche",
                          "item.alcoholic.wort_bucket": "Seau de moût de grain",
                          "item.alcoholic.hopped_wort_bucket": "Seau de moût houblonné",
                          "item.alcoholic.beer_bucket": "Seau de bière",
                          "fluid_type.alcoholic.wort": "Moût de grain",
                          "fluid_type.alcoholic.hopped_wort": "Moût houblonné",
                          "fluid_type.alcoholic.beer": "Bière",
                          "message.alcoholic.malting.status": "Maltage %1$s/%2$s (%3$s)",
                          "message.alcoholic.mash.status": "Empâtage %1$s°C · %2$s/%3$s",
                          "message.alcoholic.boil.status": "Ébullition %1$s°C · %2$s/%3$s",
                          "message.alcoholic.mill.status": "Mouture %1$s/%2$s · entraînement %3$s",
                          "message.alcoholic.engine.status": "Moteur vitesse %1$s · combustion %2$s/%3$s",
                          "message.alcoholic.electric_motor.status": "Moteur vitesse %1$s · FE %2$s/%3$s",
                          "container.alcoholic.machine": "Machine",
                          "container.alcoholic.two_slots": "Processeur",
                          "container.alcoholic.two_slots_one_tank": "Pressoir",
                          "container.alcoholic.two_slots_two_tanks": "Cuve de brassage",
                          "container.alcoholic.one_slot_one_tank": "Chaudron",
                          "container.alcoholic.one_tank": "Cuve",
                          "container.alcoholic.two_tanks": "Terrine",
                          "container.alcoholic.fuel": "Moteur à combustion",
                          "container.alcoholic.energy": "Moteur électrique",
                          "gui.alcoholic.temperature": "%s°C",
                          "gui.alcoholic.drive": "Entraînement %s",
                          "jei.alcoholic.category.mill": "Mouture",
                          "jei.alcoholic.category.mash": "Empâtage",
                          "jei.alcoholic.category.boil": "Ébullition",
                          "jei.alcoholic.category.malt": "Maltage",
                          "jei.alcoholic.category.press": "Pressurage",
                          "jei.alcoholic.category.ferment": "Fermentation",
                          "jei.alcoholic.category.age": "Élevage",
                          "jei.alcoholic.category.blend": "Assemblage",
                          "jei.alcoholic.category.condition": "Conditionnement",
                          "jei.alcoholic.category.bottle": "Mise en bouteille",
                          "jei.alcoholic.category.addon": "%s",
                          "jei.alcoholic.duration": "%ss",
                          "jei.alcoholic.volume.unspecified": "Volume non spécifié",
                          "tooltip.alcoholic.gauge.empty": "Vide",
                          "tooltip.alcoholic.gauge.fluid": "%1$s · %2$s / %3$s mB",
                          "tooltip.alcoholic.gauge.energy": "%1$s / %2$s FE"
                        }
                        """
        );
    }

    private static void addVineAssets(JsonSink sink, String color) {
        String blockName = color + "_grapevine";
        String stemName = blockName + "_stem";
        String canopyName = blockName + "_canopy";
        StringBuilder variants = new StringBuilder("{\n  \"variants\": {\n");
        boolean first = true;
        for (String stage : STAGES) {
            for (boolean trained : new boolean[]{false, true}) {
                for (boolean extended : new boolean[]{false, true}) {
                    if (!first) {
                        variants.append(",\n");
                    }
                    String model = vineModelName(blockName, stage, trained, extended);
                    variants.append("    \"extended=")
                            .append(extended)
                            .append(",stage=")
                            .append(stage)
                            .append(",trained=")
                            .append(trained)
                            .append("\": { \"model\": \"alcoholic:block/")
                            .append(model)
                            .append("\" }");
                    first = false;
                }
            }
        }
        variants.append("\n  }\n}\n");
        sink.add(
                "assets/alcoholic/blockstates/" + blockName + ".json",
                variants.toString()
        );

        StringBuilder stemVariants = new StringBuilder("{\n  \"variants\": {\n");
        first = true;
        for (String stage : STAGES) {
            for (boolean trained : new boolean[]{false, true}) {
                if (!first) {
                    stemVariants.append(",\n");
                }
                String training = trained ? "trained" : "untrained";
                stemVariants.append("    \"stage=")
                        .append(stage)
                        .append(",trained=")
                        .append(trained)
                        .append("\": { \"model\": \"alcoholic:block/")
                        .append(stemName)
                        .append("_")
                        .append(stage)
                        .append("_")
                        .append(training)
                        .append("\" }");
                first = false;
            }
        }
        stemVariants.append("\n  }\n}\n");
        sink.add(
                "assets/alcoholic/blockstates/" + stemName + ".json",
                stemVariants.toString()
        );

        StringBuilder canopyVariants = new StringBuilder("{\n  \"variants\": {\n");
        first = true;
        for (String stage : STAGES) {
            for (String axis : new String[]{"x", "z"}) {
                if (!first) {
                    canopyVariants.append(",\n");
                }
                canopyVariants.append("    \"axis=")
                        .append(axis)
                        .append(",stage=")
                        .append(stage)
                        .append("\": { \"model\": \"alcoholic:block/")
                        .append(canopyName)
                        .append("_")
                        .append(stage)
                        .append("\"");
                if ("z".equals(axis)) {
                    canopyVariants.append(", \"y\": 90");
                }
                canopyVariants.append(" }");
                first = false;
            }
        }
        canopyVariants.append("\n  }\n}\n");
        sink.add(
                "assets/alcoholic/blockstates/" + canopyName + ".json",
                canopyVariants.toString()
        );

        for (String stage : STAGES) {
            String texture = blockName + "_" + stage;
            sink.add(
                    "assets/alcoholic/models/block/" + blockName + "_"
                            + stage + "_untrained.json",
                    vineModel(texture, stage, VineModelKind.UNTRAINED)
            );
            sink.add(
                    "assets/alcoholic/models/block/" + blockName + "_"
                            + stage + "_trained.json",
                    vineModel(texture, stage, VineModelKind.TRAINED_SHORT)
            );
            sink.add(
                    "assets/alcoholic/models/block/" + blockName + "_"
                            + stage + "_base.json",
                    vineModel(texture, stage, VineModelKind.TRAINED_BASE)
            );
            sink.add(
                    "assets/alcoholic/models/block/" + stemName + "_"
                            + stage + "_trained.json",
                    vineModel(texture, stage, VineModelKind.STEM_TRAINED)
            );
            sink.add(
                    "assets/alcoholic/models/block/" + stemName + "_"
                            + stage + "_untrained.json",
                    vineModel(texture, stage, VineModelKind.STEM_UNTRAINED)
            );
            sink.add(
                    "assets/alcoholic/models/block/" + canopyName + "_"
                            + stage + ".json",
                    canopyModel(texture)
            );
        }
    }

    private static String vineModelName(
            String blockName,
            String stage,
            boolean trained,
            boolean extended
    ) {
        if (!trained) {
            return blockName + "_" + stage + "_untrained";
        }
        if (extended) {
            return blockName + "_" + stage + "_base";
        }
        return blockName + "_" + stage + "_trained";
    }

    private static String vineModel(String texture, String stage, VineModelKind kind) {
        int height = kind == VineModelKind.UNTRAINED
                ? switch (stage) {
                    case "planted" -> 8;
                    case "establishing" -> 12;
                    default -> 16;
                }
                : 16;
        return """
                {
                  "ambientocclusion": false,
                  "textures": {
                    "particle": "alcoholic:block/%s",
                    "cross": "alcoholic:block/%s"
                  },
                  "elements": [
                %s
                  ]
                }
                """.formatted(texture, texture, threeCrossPlanes(height));
    }

    private static String threeCrossPlanes(int height) {
        return """
                    {
                      "from": [0.8, 0, 8],
                      "to": [15.2, %1$d, 8],
                      "shade": false,
                      "faces": {
                        "north": { "uv": [0, 0, 16, 16], "texture": "#cross" },
                        "south": { "uv": [0, 0, 16, 16], "texture": "#cross" }
                      }
                    },
                    {
                      "from": [8, 0, 0.8],
                      "to": [8, %1$d, 15.2],
                      "shade": false,
                      "faces": {
                        "west": { "uv": [0, 0, 16, 16], "texture": "#cross" },
                        "east": { "uv": [0, 0, 16, 16], "texture": "#cross" }
                      }
                    },
                    {
                      "from": [0.8, 0, 8],
                      "to": [15.2, %1$d, 8],
                      "rotation": { "origin": [8, 8, 8], "axis": "y", "angle": 45, "rescale": true },
                      "shade": false,
                      "faces": {
                        "north": { "uv": [0, 0, 16, 16], "texture": "#cross" },
                        "south": { "uv": [0, 0, 16, 16], "texture": "#cross" }
                      }
                    }""".formatted(height);
    }

    private static String canopyModel(String texture) {
        return """
                {
                  "ambientocclusion": false,
                  "textures": {
                    "particle": "alcoholic:block/%s",
                    "cross": "alcoholic:block/%s",
                    "wire": "alcoholic:block/trellis_wire"
                  },
                  "elements": [
                %s,
                    {
                      "from": [0, 7, 7],
                      "to": [16, 9, 9],
                      "faces": {
                        "down": { "texture": "#wire" },
                        "up": { "texture": "#wire" },
                        "north": { "texture": "#wire" },
                        "south": { "texture": "#wire" },
                        "west": { "texture": "#wire" },
                        "east": { "texture": "#wire" }
                      }
                    }
                  ]
                }
                """.formatted(texture, texture, threeCrossPlanes(16));
    }

    private enum VineModelKind {
        UNTRAINED,
        TRAINED_SHORT,
        TRAINED_BASE,
        STEM_TRAINED,
        STEM_UNTRAINED
    }

    private static void addInfrastructureAssets(JsonSink sink) {
        addPostBlock(sink, "vineyard_post", false);
        addPostBlock(sink, "end_post", true);
        sink.add(
                "assets/alcoholic/blockstates/trellis_wire.json",
                """
                        {
                          "variants": {
                            "axis=x": { "model": "alcoholic:block/trellis_wire" },
                            "axis=z": {
                              "model": "alcoholic:block/trellis_wire",
                              "y": 90
                            }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/block/trellis_wire.json",
                """
                        {
                          "ambientocclusion": false,
                          "textures": {
                            "particle": "alcoholic:block/trellis_wire",
                            "wire": "alcoholic:block/trellis_wire"
                          },
                          "elements": [
                            {
                              "from": [0, 7, 7],
                              "to": [16, 9, 9],
                              "faces": {
                                "down": { "texture": "#wire" },
                                "up": { "texture": "#wire" },
                                "north": { "texture": "#wire" },
                                "south": { "texture": "#wire" },
                                "west": { "texture": "#wire" },
                                "east": { "texture": "#wire" }
                              }
                            }
                          ]
                        }
                        """
        );
        addBlockItem(sink, "vineyard_post");
        addBlockItem(sink, "end_post");
        addBlockItem(sink, "trellis_wire");
        addGeneratedItem(sink, "trellis_spool");
        addGeneratedItem(sink, "pruning_shears");
    }

    private static void addProcessingAssets(JsonSink sink) {
        sink.add(
                "assets/alcoholic/blockstates/artisanal_press.json",
                """
                        {
                          "variants": {
                            "facing=north": { "model": "alcoholic:block/artisanal_press" },
                            "facing=south": { "model": "alcoholic:block/artisanal_press", "y": 180 },
                            "facing=west": { "model": "alcoholic:block/artisanal_press", "y": 270 },
                            "facing=east": { "model": "alcoholic:block/artisanal_press", "y": 90 }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/block/artisanal_press.json",
                """
                        {
                          "parent": "minecraft:block/block",
                          "textures": {
                            "particle": "alcoholic:block/artisanal_press",
                            "wood": "alcoholic:block/artisanal_press",
                            "iron": "alcoholic:block/trellis_wire"
                          },
                          "elements": [
                            {
                              "from": [1, 0, 1],
                              "to": [15, 6, 15],
                              "faces": {
                                "down": { "texture": "#wood" },
                                "up": { "texture": "#wood" },
                                "north": { "texture": "#wood" },
                                "south": { "texture": "#wood" },
                                "west": { "texture": "#wood" },
                                "east": { "texture": "#wood" }
                              }
                            },
                            {
                              "from": [3, 6, 3],
                              "to": [13, 12, 13],
                              "faces": {
                                "down": { "texture": "#wood" },
                                "up": { "texture": "#wood" },
                                "north": { "texture": "#wood" },
                                "south": { "texture": "#wood" },
                                "west": { "texture": "#wood" },
                                "east": { "texture": "#wood" }
                              }
                            },
                            {
                              "from": [7, 12, 7],
                              "to": [9, 16, 9],
                              "faces": {
                                "down": { "texture": "#iron" },
                                "up": { "texture": "#iron" },
                                "north": { "texture": "#iron" },
                                "south": { "texture": "#iron" },
                                "west": { "texture": "#iron" },
                                "east": { "texture": "#iron" }
                              }
                            },
                            {
                              "from": [5, 14, 5],
                              "to": [11, 16, 11],
                              "faces": {
                                "down": { "texture": "#iron" },
                                "up": { "texture": "#iron" },
                                "north": { "texture": "#iron" },
                                "south": { "texture": "#iron" },
                                "west": { "texture": "#iron" },
                                "east": { "texture": "#iron" }
                              }
                            }
                          ]
                        }
                        """
        );
        addBlockItem(sink, "artisanal_press");
        ArtisanalFermenterAssetData.add(sink);
        addSimpleBlock(sink, "oak_barrel");
        addSimpleBlock(sink, "artisanal_blending_crock");
        addBlockItem(sink, "oak_barrel");
        addBlockItem(sink, "artisanal_blending_crock");
        addGeneratedItem(sink, "yeast");
        addGeneratedItem(sink, "grape_pomace");
        addGeneratedItem(sink, "empty_bottle");
        addGeneratedItem(sink, "beverage_bottle");
        addGeneratedItem(sink, "red_grape_must_bucket");
        addGeneratedItem(sink, "white_grape_must_bucket");
        addGeneratedItem(sink, "young_red_wine_bucket");
        addGeneratedItem(sink, "young_white_wine_bucket");
        addGeneratedItem(sink, "red_wine_bucket");
        addGeneratedItem(sink, "white_wine_bucket");
    }

    private static void addIndustrialAssets(JsonSink sink) {
        for (String name : new String[]{
                "industrial_casing",
                "machine_window",
                "access_hatch",
                "fluid_port",
                "item_port",
                "kinetic_port",
                "industrial_vat_controller",
                "industrial_tank_controller",
                "industrial_malt_house_controller",
                "industrial_mash_tun_controller",
                "industrial_brewing_kettle_controller",
                "industrial_conditioning_vessel_controller"
        }) {
            addSimpleBlock(sink, name);
            addBlockItem(sink, name);
        }
        sink.add(
                "assets/alcoholic/blockstates/industrial_press_controller.json",
                """
                        {
                          "variants": {
                            "formed=false": { "model": "alcoholic:block/industrial_press_controller" },
                            "formed=true": { "model": "alcoholic:block/industrial_press_controller_formed" }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/block/industrial_press_controller.json",
                """
                        {
                          "parent": "minecraft:block/cube_all",
                          "textures": {
                            "all": "alcoholic:block/industrial_press_controller"
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/block/industrial_press_controller_formed.json",
                """
                        {
                          "parent": "minecraft:block/cube_all",
                          "textures": {
                            "all": "alcoholic:block/industrial_press_controller_formed"
                          }
                        }
                        """
        );
        addBlockItem(sink, "industrial_press_controller");
        sink.add(
                "assets/alcoholic/blockstates/industrial_roller_mill_controller.json",
                """
                        {
                          "variants": {
                            "formed=false": { "model": "alcoholic:block/industrial_roller_mill_controller" },
                            "formed=true": { "model": "alcoholic:block/industrial_roller_mill_controller_formed" }
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/block/industrial_roller_mill_controller.json",
                """
                        {
                          "parent": "minecraft:block/cube_all",
                          "textures": {
                            "all": "alcoholic:block/industrial_roller_mill_controller"
                          }
                        }
                        """
        );
        sink.add(
                "assets/alcoholic/models/block/industrial_roller_mill_controller_formed.json",
                """
                        {
                          "parent": "minecraft:block/cube_all",
                          "textures": {
                            "all": "alcoholic:block/industrial_roller_mill_controller_formed"
                          }
                        }
                        """
        );
        addBlockItem(sink, "industrial_roller_mill_controller");
    }

    private static void addSimpleBlock(JsonSink sink, String name) {
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
                "assets/alcoholic/models/block/" + name + ".json",
                """
                        {
                          "parent": "minecraft:block/cube_all",
                          "textures": {
                            "all": "alcoholic:block/%s"
                          }
                        }
                        """.formatted(name)
        );
    }

    private static void addPostBlock(JsonSink sink, String name, boolean endPost) {
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
        String elements = endPost
                ? postElement(5, 0, 5, 11, 16, 11)
                        + ",\n"
                        + postElement(3, 14, 3, 13, 16, 13)
                : postElement(6, 0, 6, 10, 16, 10);
        sink.add(
                "assets/alcoholic/models/block/" + name + ".json",
                """
                        {
                          "parent": "minecraft:block/block",
                          "ambientocclusion": false,
                          "textures": {
                            "particle": "alcoholic:block/%1$s",
                            "post": "alcoholic:block/%1$s"
                          },
                          "elements": [
                        %2$s
                          ]
                        }
                        """.formatted(name, elements)
        );
    }

    private static String postElement(
            int fromX,
            int fromY,
            int fromZ,
            int toX,
            int toY,
            int toZ
    ) {
        return """
                    {
                      "from": [%d, %d, %d],
                      "to": [%d, %d, %d],
                      "faces": {
                        "down": { "texture": "#post" },
                        "up": { "texture": "#post" },
                        "north": { "texture": "#post" },
                        "south": { "texture": "#post" },
                        "west": { "texture": "#post" },
                        "east": { "texture": "#post" }
                      }
                    }""".formatted(fromX, fromY, fromZ, toX, toY, toZ);
    }

    private static void addBlockItem(JsonSink sink, String name) {
        sink.add(
                "assets/alcoholic/models/item/" + name + ".json",
                """
                        {
                          "parent": "alcoholic:block/%s"
                        }
                        """.formatted(name)
        );
    }

    private static void addGeneratedItem(JsonSink sink, String name) {
        sink.add(
                "assets/alcoholic/models/item/" + name + ".json",
                """
                        {
                          "parent": "minecraft:item/generated",
                          "textures": {
                            "layer0": "alcoholic:item/%s"
                          }
                        }
                        """.formatted(name)
        );
    }

    private static void addGrainAssets(JsonSink sink) {
        StringBuilder barleyVariants = new StringBuilder("{\n  \"variants\": {\n");
        for (int age = 0; age <= 2; age++) {
            if (age > 0) {
                barleyVariants.append(",\n");
            }
            barleyVariants.append("    \"age=")
                    .append(age)
                    .append("\": { \"model\": \"alcoholic:block/barley_crop_")
                    .append(age)
                    .append("\" }");
            sink.add(
                    "assets/alcoholic/models/block/barley_crop_" + age + ".json",
                    """
                            {
                              "parent": "minecraft:block/crop",
                              "textures": { "crop": "alcoholic:block/barley_crop_%s" }
                            }
                            """.formatted(age)
            );
        }
        barleyVariants.append("\n  }\n}\n");
        sink.add("assets/alcoholic/blockstates/barley_crop.json", barleyVariants.toString());

        StringBuilder hopVariants = new StringBuilder("{\n  \"variants\": {\n");
        for (int age = 0; age <= 2; age++) {
            if (age > 0) {
                hopVariants.append(",\n");
            }
            hopVariants.append("    \"age=")
                    .append(age)
                    .append("\": { \"model\": \"alcoholic:block/hop_bine_")
                    .append(age)
                    .append("\" }");
            sink.add(
                    "assets/alcoholic/models/block/hop_bine_" + age + ".json",
                    """
                            {
                              "parent": "minecraft:block/cross",
                              "textures": { "cross": "alcoholic:block/hop_bine_%s" }
                            }
                            """.formatted(age)
            );
        }
        hopVariants.append("\n  }\n}\n");
        sink.add("assets/alcoholic/blockstates/hop_bine.json", hopVariants.toString());

        MaltingFloorAssetData.add(sink);
        MashTunAssetData.add(sink);
        BrewingKettleAssetData.add(sink);
        MaltMillAssetData.add(sink);
        addLitMachine(sink, "electric_motor");
        addGeneratedItem(sink, "barley");
        addGeneratedItem(sink, "barley_seeds");
        addGeneratedItem(sink, "malted_barley");
        addGeneratedItem(sink, "grist");
        addGeneratedItem(sink, "hops");
        addGeneratedItem(sink, "hop_rhizome");
        addGeneratedItem(sink, "spent_grain");
        addGeneratedItem(sink, "wort_bucket");
        addGeneratedItem(sink, "hopped_wort_bucket");
        addGeneratedItem(sink, "beer_bucket");
    }

    private static void addSimpleMachine(JsonSink sink, String name) {
        sink.add(
                "assets/alcoholic/blockstates/" + name + ".json",
                name.contains("floor")
                        ? """
                        {
                          "variants": {
                            "": { "model": "alcoholic:block/%s" }
                          }
                        }
                        """.formatted(name)
                        : """
                        {
                          "variants": {
                            "facing=north": { "model": "alcoholic:block/%s" },
                            "facing=south": { "model": "alcoholic:block/%s", "y": 180 },
                            "facing=west": { "model": "alcoholic:block/%s", "y": 270 },
                            "facing=east": { "model": "alcoholic:block/%s", "y": 90 }
                          }
                        }
                        """.formatted(name, name, name, name)
        );
        sink.add(
                "assets/alcoholic/models/block/" + name + ".json",
                """
                        {
                          "parent": "minecraft:block/cube_all",
                          "textures": { "all": "alcoholic:block/%s" }
                        }
                        """.formatted(name)
        );
        addBlockItem(sink, name);
    }

    private static void addLitMachine(JsonSink sink, String name) {
        sink.add(
                "assets/alcoholic/blockstates/" + name + ".json",
                """
                        {
                          "variants": {
                            "facing=north,lit=false": { "model": "alcoholic:block/%1$s" },
                            "facing=south,lit=false": { "model": "alcoholic:block/%1$s", "y": 180 },
                            "facing=west,lit=false": { "model": "alcoholic:block/%1$s", "y": 270 },
                            "facing=east,lit=false": { "model": "alcoholic:block/%1$s", "y": 90 },
                            "facing=north,lit=true": { "model": "alcoholic:block/%1$s_on" },
                            "facing=south,lit=true": { "model": "alcoholic:block/%1$s_on", "y": 180 },
                            "facing=west,lit=true": { "model": "alcoholic:block/%1$s_on", "y": 270 },
                            "facing=east,lit=true": { "model": "alcoholic:block/%1$s_on", "y": 90 }
                          }
                        }
                        """.formatted(name)
        );
        sink.add(
                "assets/alcoholic/models/block/" + name + ".json",
                """
                        {
                          "parent": "minecraft:block/cube_all",
                          "textures": { "all": "alcoholic:block/%s" }
                        }
                        """.formatted(name)
        );
        sink.add(
                "assets/alcoholic/models/block/" + name + "_on.json",
                """
                        {
                          "parent": "minecraft:block/cube_all",
                          "textures": { "all": "alcoholic:block/%s_on" }
                        }
                        """.formatted(name)
        );
        addBlockItem(sink, name);
    }

    @Override
    public String getName() {
        return "Alcoholic grape assets";
    }
}
