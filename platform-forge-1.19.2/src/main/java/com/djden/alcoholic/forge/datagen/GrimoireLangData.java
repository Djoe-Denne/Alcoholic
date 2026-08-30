package com.djden.alcoholic.forge.datagen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.LinkedHashMap;
import java.util.Map;

final class GrimoireLangData {
    private GrimoireLangData() {
    }

    static String mergeEnglish(String baseJson) {
        return merge(baseJson, english());
    }

    static String mergeFrench(String baseJson) {
        return merge(baseJson, french());
    }

    private static String merge(String baseJson, Map<String, String> extra) {
        JsonObject json = JsonParser.parseString(baseJson).getAsJsonObject();
        extra.forEach(json::addProperty);
        return json.toString();
    }

    static Map<String, String> english() {
        Map<String, String> lang = new LinkedHashMap<>();
        lang.put("item.alcoholic.wine_grimoire", "Grimoire of Wine");
        lang.put("item.alcoholic.beer_grimoire", "Grimoire of Beer");
        lang.put("grimoire.alcoholic.toc", "Contents");
        lang.put("grimoire.alcoholic.illustration.pending", "Plate to be engraved");

        lang.put("grimoire.alcoholic.wine.ch0.title", "Frontispiece");
        lang.put(
                "grimoire.alcoholic.wine.ch0.p0",
                "A common book, stained with a cluster of grapes, becomes this volume. It will not plant the vine for you. It only remembers the order of the work."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch0.p1",
                "The official path is press, then ferment, then age in oak. Blending is a side dish. Bottling is a gesture, not a node on the graph."
        );

        lang.put("grimoire.alcoholic.wine.ch1.title", "The Vine");
        lang.put(
                "grimoire.alcoholic.wine.ch1.p0",
                "Seek a wild vine in plains, sunflower plains, forest, flower forest, or birch. Break it for one cutting. Plant once on dirt or farmland. The plant is perennial: harvest does not kill it."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch1.p1",
                "Two posts at the same height, then the spool from one to the other, stretch a wire. Untrellised vines still grow, but yield about 70% and quality about 85% of a trained row."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch1.p2",
                "Eight stages on the first cycle, then dormancy back to flowering. At harvest-ready, right-click any hand. Prune only in dormancy, and only if you wish: the cycle does not require the shears."
        );

        lang.put("grimoire.alcoholic.wine.ch2.title", "The Workshop");
        lang.put(
                "grimoire.alcoholic.wine.ch2.p0",
                "You need a press, a fermenter, and an oak barrel. The crock is optional and sits off the official path. Empty hand, no sneak, opens a machine. A held item still inserts; buckets still pour."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch2.p1",
                "Alcoholic adds no trees, gears, or pipes. Buckets are enough. Create is not required. Keep the fermenter at room temperature: 18-24 C is the preferred band."
        );

        lang.put("grimoire.alcoholic.wine.ch3.title", "The Press");
        lang.put(
                "grimoire.alcoholic.wine.ch3.p0",
                "Right-click the press with grapes. Twenty ticks later you have must and pomace. The harvest lot (sugar, acidity, quality) is copied onto the must. Sneak with an empty hand takes the pomace."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch3.p1",
                "An empty bucket is the only artisan path from press to fermenter. A Create mechanical press will make must, but it forgets the harvest lot and writes default properties."
        );

        lang.put("grimoire.alcoholic.wine.ch4.title", "Fermentation");
        lang.put(
                "grimoire.alcoholic.wine.ch4.p0",
                "Pour must into the fermenter, then pitch yeast. Each tick, sugar falls and ethanol rises. The carbon dioxide is vented, never stored. Eighty ticks of kinetics finish the work."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch4.p1",
                "Outside 18-24 C the work slows. Outside 10-30 C it stops. Ambient air, about 20 C, is enough. When sugar falls below the threshold, the batch becomes young red or white wine."
        );

        lang.put("grimoire.alcoholic.wine.ch5.title", "The Cellar");
        lang.put(
                "grimoire.alcoholic.wine.ch5.p0",
                "There is no cellar block. The oak barrel is the cave: 4000 mB, and it ages only while the chunk stays loaded. Pour young wine. Wait until maturity reaches 1.0. Then the batch becomes wine."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch5.p1",
                "A seasoned barrel (already used) multiplies the next lot by 1.15. Emptying the barrel remembers the last liquid. You may bottle young wine from the fermenter instead, and skip the cave."
        );

        lang.put("grimoire.alcoholic.wine.ch6.title", "The Bottle");
        lang.put(
                "grimoire.alcoholic.wine.ch6.p0",
                "Right-click a fermenter, barrel, crock, or industrial controller with an empty bottle. Default volume: 250 mB. The bottle is a snapshot (definition, ethanol, sugar, acidity, maturity, origin, quality), not a tiny tank."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch6.p1",
                "The crock blends two 4000 mB tanks (sneak, empty hand). That is not a node on the official graph. Never bottle from the press. Never bottle unfermented must."
        );

        lang.put("grimoire.alcoholic.wine.ch7.title", "What Not To Do");
        lang.put(
                "grimoire.alcoholic.wine.ch7.p0",
                "Do not skip the bucket between press and fermenter. Do not treat blending as a required step. Do not prune outside dormancy: the shears refuse. Do not demand Create to progress."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch7.p1",
                "Do not confuse this book with beer. Wine has no malt, mill, mash, or boil. Beer has no official age. Two distinct lots never merge on their own in a tank."
        );

        lang.put("grimoire.alcoholic.wine.ch8.title", "The Factory");
        lang.put(
                "grimoire.alcoholic.wine.ch8.p0",
                "Industrial press, vat, and tank are extra executors of the same processes. They do not change the graph. A factory-only player can reach bottled young wine. Aging still wants the oak barrel."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch8.p1",
                "That missing industrial cave is the next official hole to close, not distill and not cider. Create pipes remain optional. The Alcoholic press keeps the harvest lot; Create compacting does not."
        );

        lang.put("grimoire.alcoholic.beer.ch0.title", "Frontispiece");
        lang.put(
                "grimoire.alcoholic.beer.ch0.p0",
                "A common book, dusted with barley, becomes this volume. Beer ends after ferment. There is no official age, no barrel, no crock on this path."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch0.p1",
                "The order is malt, mill, mash, boil, then ferment. Conditioning is industrial only and off the graph. Create and Crossroads are optional extra hands, not a requirement."
        );

        lang.put("grimoire.alcoholic.beer.ch1.title", "The Fields");
        lang.put(
                "grimoire.alcoholic.beer.ch1.p0",
                "Barley is an annual cereal, three stages like wheat. Find wild barley in plains, sunflower plains, or meadow. Plant seeds on farmland. At maturity: one barley and one seed. Bone meal hurries it."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch1.p1",
                "Hops are a bine, not a vine. They die without a wire above (at most four blocks). Stretch wire between two posts, plant the rhizome under the wire on dirt, grass, or farmland."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch1.p2",
                "The first rhizome comes from wild hops in forest, flower forest, birch, taiga, or river. Right-click a mature bine for one hop; the tier returns to age 0. If Brewery is loaded, its barley counts through the barley tag."
        );

        lang.put("grimoire.alcoholic.beer.ch2.title", "The Brewery");
        lang.put(
                "grimoire.alcoholic.beer.ch2.p0",
                "Lay out a malting floor, a mill beside an engine, a mash tun on magma, a kettle on a lit campfire, and a fermenter in still air. The mill and engine must touch."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch2.p1",
                "Empty hand, no sneak, opens a machine. Alcoholic adds no trees, gears, or pipes. Buckets are enough. An electric motor on Forge Energy is optional."
        );

        lang.put("grimoire.alcoholic.beer.ch3.title", "Malting");
        lang.put(
                "grimoire.alcoholic.beer.ch3.p0",
                "Right-click the floor with barley. Eighty ticks later, sneak with an empty hand for malted barley. If the output is empty, the same gesture cycles pale, amber, and dark."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch3.p1",
                "The official beer graph uses pale malt. Pale: color 0.12, fermentable 0.85, roast 0.15. Amber and dark remain playable on the floor. The floor no longer mills."
        );

        lang.put("grimoire.alcoholic.beer.ch4.title", "The Mill");
        lang.put(
                "grimoire.alcoholic.beer.ch4.p0",
                "Place the engine adjacent to the mill. Feed the engine furnace fuel (coal, charcoal, wood). Sneak with an empty hand to pull fuel back. Right-click the mill with malted barley."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch4.p1",
                "Without drive, the mill stalls: progress stays at 0. Eighty ticks later, sneak for grist. Malt properties (sugar, color) are copied. Create millstone or crushing wheels may run the same mill process."
        );

        lang.put("grimoire.alcoholic.beer.ch5.title", "The Mash");
        lang.put(
                "grimoire.alcoholic.beer.ch5.p0",
                "Set the tun on magma (65 C). Preferred band 62-68 C, operable 52-78 C. Outside that, yield falls or the process stalls. Add grist, then a water bucket (1000 mB)."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch5.p1",
                "Forty ticks later: 1000 mB of wort and spent grain. The tun has two tanks: fill the inlet, drain the wort. Sneak for spent grain. An empty bucket (or an optional Create pipe) takes the wort."
        );

        lang.put("grimoire.alcoholic.beer.ch6.title", "The Boil");
        lang.put(
                "grimoire.alcoholic.beer.ch6.p0",
                "Set the kettle on a lit campfire or lava (about 100 C). Preferred 98-105 C, operable 90-110 C. Pour wort, then add hops. One addition at the start, bittering role."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch6.p1",
                "Forty ticks later the liquid is hopped wort: bitterness about 0.55, aroma about 0.40. Sugar already present is kept. Do not put magma under the kettle: 65 C is too cold for the boil."
        );

        lang.put("grimoire.alcoholic.beer.ch7.title", "Fermentation");
        lang.put(
                "grimoire.alcoholic.beer.ch7.p0",
                "Carry hopped wort to the same fermenter the vineyard uses. Pitch yeast. Sugar falls, ethanol rises (sugar to ethanol 0.47). Carbon dioxide is vented. Eighty ticks of kinetics."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch7.p1",
                "Outside 18-24 C it slows. Outside 10-30 C it stops. Ambient air is enough. When sugar falls below 0.02, the batch becomes beer. The vat is not a beer machine; it runs ferment on any defined liquid."
        );

        lang.put("grimoire.alcoholic.beer.ch8.title", "Heat");
        lang.put(
                "grimoire.alcoholic.beer.ch8.p0",
                "Heat is the block below. Magma 65 C: ideal mash, too cold to boil. Lit furnace or smoker 80 C: outside mash operable, still too cold to boil. Fire or soul fire 95 C: too hot to mash, operable boil."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch8.p1",
                "Lit campfire or lava 100 C: too hot to mash, ideal boil. Soul campfire 95 C: operable boil. Lit blast furnace 110 C: mash too hot, boil at the high edge. Bare air about 20 C: both stay cold."
        );

        lang.put("grimoire.alcoholic.beer.ch9.title", "The Bottle");
        lang.put(
                "grimoire.alcoholic.beer.ch9.p0",
                "Right-click the fermenter (or an industrial ferment controller) with an empty bottle. Default 250 mB. The bottle is a snapshot, not a tiny tank. You cannot restart the process by pouring it back."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch9.p1",
                "Bottle only from fermenter, barrel, crock, or industrial controller. Never from press, mash tun, or kettle. Unfermented wort and hopped wort will not take the bottle."
        );

        lang.put("grimoire.alcoholic.beer.ch10.title", "What Not To Do");
        lang.put(
                "grimoire.alcoholic.beer.ch10.p0",
                "No press: beer does not press. No barrel and no crock on the official beer graph. No official age. Conditioning is industrial only and off-graph. Do not seat the mill without an engine: it stalls."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch10.p1",
                "Do not put magma under the kettle. Do not put a campfire under the mash tun. Do not bottle wort. Industrial 7B hulls are other executors of the same work, not other recipes, and not required."
        );
        return lang;
    }

    static Map<String, String> french() {
        Map<String, String> lang = new LinkedHashMap<>();
        lang.put("item.alcoholic.wine_grimoire", "Grimoire du vin");
        lang.put("item.alcoholic.beer_grimoire", "Grimoire de la bière");
        lang.put("grimoire.alcoholic.toc", "Sommaire");
        lang.put("grimoire.alcoholic.illustration.pending", "Planche à graver");

        lang.put("grimoire.alcoholic.wine.ch0.title", "Frontispice");
        lang.put(
                "grimoire.alcoholic.wine.ch0.p0",
                "Un livre ordinaire, tache par une grappe, devient ce volume. Il ne plantera pas la vigne a votre place. Il se souvient seulement de l'ordre du travail."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch0.p1",
                "Le chemin officiel est presser, puis fermenter, puis elever en chene. L'assemblage est un hors-d'oeuvre. La mise en bouteille est un geste, pas un noeud du graphe."
        );

        lang.put("grimoire.alcoholic.wine.ch1.title", "La vigne");
        lang.put(
                "grimoire.alcoholic.wine.ch1.p0",
                "Cherchez une vigne sauvage dans les plaines, plaines de tournesols, forets, forets fleuries ou de bouleaux. Cassez-la : une bouture. Plantez une fois sur de la terre ou de la terre labouree. La plante est perenne : la recolte ne la tue pas."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch1.p1",
                "Deux poteaux a la meme hauteur, puis la bobine de l'un a l'autre, tendent un fil. Sans palissage la vigne pousse encore, mais le rendement vaut environ 70 % et la qualite 85 % d'un rang dresse."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch1.p2",
                "Huit stades au premier cycle, puis la dormance jusqu'a la floraison. A maturite, clic droit n'importe quelle main. Le secateur ne taille qu'en dormance, et seulement si vous le voulez : le cycle n'exige pas la taille."
        );

        lang.put("grimoire.alcoholic.wine.ch2.title", "L'atelier");
        lang.put(
                "grimoire.alcoholic.wine.ch2.p0",
                "Il vous faut un pressoir, une cuve et un fut de chene. La terrine est optionnelle et hors chemin officiel. Main vide, sans sneak, ouvre une machine. Un item en main s'insere encore ; les seaux se versent toujours."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch2.p1",
                "Alcoholic n'ajoute ni arbres, ni engrenages, ni tuyaux. Les seaux suffisent. Create n'est pas requis. Tenez la cuve a l'ambiance : la bande preferee est 18-24 C."
        );

        lang.put("grimoire.alcoholic.wine.ch3.title", "Le pressurage");
        lang.put(
                "grimoire.alcoholic.wine.ch3.p0",
                "Clic droit sur le pressoir avec des raisins. Vingt ticks plus tard : mout et marc. Le lot de recolte (sucre, acidite, qualite) est copie sur le mout. Sneak et main vide prennent le marc."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch3.p1",
                "Le seau vide est le seul chemin artisanal du pressoir au fermenteur. Un Mechanical Press Create fera du mout, mais il oublie le lot et ecrit des proprietes par defaut."
        );

        lang.put("grimoire.alcoholic.wine.ch4.title", "La fermentation");
        lang.put(
                "grimoire.alcoholic.wine.ch4.p0",
                "Versez le mout dans la cuve, puis la levure. A chaque tick le sucre baisse et l'ethanol monte. Le CO2 est evente, jamais stocke. Quatre-vingts ticks de cinetique achevent l'ouvrage."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch4.p1",
                "Hors 18-24 C le travail ralentit. Hors 10-30 C il s'arrete. L'air ambiant, vers 20 C, suffit. Quand le sucre passe sous le seuil, le lot devient vin rouge ou blanc jeune."
        );

        lang.put("grimoire.alcoholic.wine.ch5.title", "La cave");
        lang.put(
                "grimoire.alcoholic.wine.ch5.p0",
                "Il n'existe pas de bloc cave. Le fut de chene est la cave : 4000 mB, et il n'eleve que tant que le chunk reste charge. Versez le vin jeune. Attendez une maturite de 1,0. Alors le lot devient vin."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch5.p1",
                "Un fut saisonne (deja utilise) multiplie le lot suivant par 1,15. Vider le fut enregistre le dernier liquide. Vous pouvez aussi bouteiller le vin jeune depuis la cuve et epargner la cave."
        );

        lang.put("grimoire.alcoholic.wine.ch6.title", "La bouteille");
        lang.put(
                "grimoire.alcoholic.wine.ch6.p0",
                "Clic droit sur un fermenteur, un fut, une terrine ou un controleur industriel avec une bouteille vide. Volume par defaut : 250 mB. La bouteille est un instantane (definition, ethanol, sucre, acidite, maturite, origine, qualite), pas une mini-cuve."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch6.p1",
                "La terrine assemble deux tanks de 4000 mB (sneak, main vide). Ce n'est pas un noeud du graphe officiel. Ne bouteillez jamais depuis le pressoir. Ne bouteillez jamais le mout non fermente."
        );

        lang.put("grimoire.alcoholic.wine.ch7.title", "Ce qu'il ne faut pas faire");
        lang.put(
                "grimoire.alcoholic.wine.ch7.p0",
                "Ne sautez pas le seau entre pressoir et fermenteur. Ne traitez pas l'assemblage comme une etape obligatoire. Ne taillez pas hors dormance : le secateur refuse. N'exigez pas Create pour avancer."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch7.p1",
                "Ne confondez pas ce livre avec la biere. Le vin n'a ni malt, ni mouture, ni empatage, ni ebullition. La biere n'a pas d'elevage officiel. Deux lots distincts ne fusionnent jamais seuls dans un tank."
        );

        lang.put("grimoire.alcoholic.wine.ch8.title", "L'usine");
        lang.put(
                "grimoire.alcoholic.wine.ch8.p0",
                "Pressoir, cuve et reservoir industriels sont d'autres executeurs des memes process. Ils ne changent pas le graphe. Un joueur uniquement usine peut atteindre le vin jeune en bouteille. L'elevage veut encore le fut de chene."
        );
        lang.put(
                "grimoire.alcoholic.wine.ch8.p1",
                "Cette cave industrielle manquante est le prochain trou officiel a fermer, pas la distillation ni le cidre. Les tuyaux Create restent optionnels. Le pressoir Alcoholic garde le lot ; le compactage Create ne le garde pas."
        );

        lang.put("grimoire.alcoholic.beer.ch0.title", "Frontispice");
        lang.put(
                "grimoire.alcoholic.beer.ch0.p0",
                "Un livre ordinaire, poudre d'orge, devient ce volume. La biere s'arrete apres la fermentation. Pas d'elevage officiel, pas de fut, pas de terrine sur ce chemin."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch0.p1",
                "L'ordre est malter, moudre, empatage, ebullition, puis fermentation. Le conditionnement est industriel seulement et hors graphe. Create et Crossroads sont des mains optionnelles, pas une exigence."
        );

        lang.put("grimoire.alcoholic.beer.ch1.title", "Les champs");
        lang.put(
                "grimoire.alcoholic.beer.ch1.p0",
                "L'orge est une cereale annuelle, trois stades comme le ble. Trouvez de l'orge sauvage dans les plaines, plaines de tournesols ou prairies. Plantez les graines sur terre labouree. A maturite : une orge et une graine. La poudre d'os hate."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch1.p1",
                "Le houblon est une bine, pas une vigne. Elle meurt sans fil au-dessus (quatre blocs au plus). Tendez le fil entre deux poteaux, plantez le rhizome sous le fil, sur terre, herbe ou terre labouree."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch1.p2",
                "Le premier rhizome vient du houblon sauvage en foret, foret fleurie, bouleaux, taiga ou riviere. Clic droit sur une bine mure : un houblon, l'etage revient a l'age 0. Si Brewery est charge, son orge compte via le tag orge."
        );

        lang.put("grimoire.alcoholic.beer.ch2.title", "La brasserie");
        lang.put(
                "grimoire.alcoholic.beer.ch2.p0",
                "Posez une aire de maltage, un broyeur contre un moteur, une cuve d'empatage sur du magma, un chaudron sur un feu de camp allume, et un fermenteur a l'air calme. Broyeur et moteur doivent se toucher."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch2.p1",
                "Main vide, sans sneak, ouvre une machine. Alcoholic n'ajoute ni arbres, ni engrenages, ni tuyaux. Les seaux suffisent. Un moteur electrique sur Forge Energy reste optionnel."
        );

        lang.put("grimoire.alcoholic.beer.ch3.title", "Le maltage");
        lang.put(
                "grimoire.alcoholic.beer.ch3.p0",
                "Clic droit sur l'aire avec de l'orge. Quatre-vingts ticks plus tard, sneak et main vide pour l'orge maltee. Si la sortie est vide, le meme geste cycle pale,ambre et fonce."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch3.p1",
                "Le graphe officiel de la biere utilise le malt pale. Pale : couleur 0,12, fermentescible 0,85, torrefaction 0,15. Ambre et fonce restent jouables sur l'aire. L'aire ne broie plus."
        );

        lang.put("grimoire.alcoholic.beer.ch4.title", "La mouture");
        lang.put(
                "grimoire.alcoholic.beer.ch4.p0",
                "Placez le moteur adjacent au broyeur. Chargez-le avec un combustible de fourneau (charbon, charbon de bois, bois). Sneak et main vide retirent le combustible. Clic droit sur le broyeur avec l'orge maltee."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch4.p1",
                "Sans entrainement, le broyeur cale : la progression reste a 0. Quatre-vingts ticks plus tard, sneak pour la mouture. Les proprietes du malt (sucre, couleur) sont copiees. Millstone ou crushing wheels Create peuvent executer le meme process."
        );

        lang.put("grimoire.alcoholic.beer.ch5.title", "L'empâtage");
        lang.put(
                "grimoire.alcoholic.beer.ch5.p0",
                "Posez la cuve sur du magma (65 C). Bande preferee 62-68 C, operable 52-78 C. Hors de la, le rendement chute ou le process stagne. Ajoutez la mouture, puis un seau d'eau (1000 mB)."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch5.p1",
                "Quarante ticks plus tard : 1000 mB de mout et de la dreche. La cuve a deux tanks : on remplit l'entree, on draine le mout. Sneak pour la dreche. Un seau vide (ou un tuyau Create optionnel) prend le mout."
        );

        lang.put("grimoire.alcoholic.beer.ch6.title", "L'ébullition");
        lang.put(
                "grimoire.alcoholic.beer.ch6.p0",
                "Posez le chaudron sur un feu de camp allume ou de la lave (environ 100 C). Prefere 98-105 C, operable 90-110 C. Versez le mout, puis le houblon. Un seul ajout au depart, role amertume."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch6.p1",
                "Quarante ticks plus tard le liquide est du mout houblonne : amertume vers 0,55, arome vers 0,40. Le sucre deja present est conserve. Ne mettez pas de magma sous le chaudron : 65 C est trop froid pour bouillir."
        );

        lang.put("grimoire.alcoholic.beer.ch7.title", "La fermentation");
        lang.put(
                "grimoire.alcoholic.beer.ch7.p0",
                "Portez le mout houblonne dans la meme cuve que le vignoble. Ajoutez la levure. Le sucre baisse, l'ethanol monte (sucre vers ethanol 0,47). Le CO2 est evente. Quatre-vingts ticks de cinetique."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch7.p1",
                "Hors 18-24 C cela ralentit. Hors 10-30 C cela s'arrete. L'air ambiant suffit. Quand le sucre passe sous 0,02, le lot devient biere. La cuve n'est pas une machine a biere ; elle execute fermenter sur tout liquide defini."
        );

        lang.put("grimoire.alcoholic.beer.ch8.title", "La chaleur");
        lang.put(
                "grimoire.alcoholic.beer.ch8.p0",
                "La chaleur est le bloc du dessous. Magma 65 C : ideal a l'empatage, trop froid pour bouillir. Fourneau ou fumoir allume 80 C : hors bande operable d'empatage, encore trop froid pour bouillir. Feu ou feu des ames 95 C : trop chaud a l'empatage, operable a l'ebullition."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch8.p1",
                "Feu de camp allume ou lave 100 C : trop chaud a l'empatage, ideal a l'ebullition. Feu de camp des ames 95 C : operable a l'ebullition. Haut fourneau allume 110 C : empatage trop chaud, ebullition en limite haute. L'air nu, vers 20 C, laisse les deux froids."
        );

        lang.put("grimoire.alcoholic.beer.ch9.title", "La bouteille");
        lang.put(
                "grimoire.alcoholic.beer.ch9.p0",
                "Clic droit sur le fermenteur (ou un controleur industriel de fermentation) avec une bouteille vide. Defaut 250 mB. La bouteille est un instantane, pas une mini-cuve. On ne relance pas l'horloge en reverser."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch9.p1",
                "On ne bouteille que depuis fermenteur, fut, terrine ou controleur industriel. Jamais depuis pressoir, cuve d'empatage ou chaudron. Le mout et le mout houblonne non fermentes n'acceptent pas la bouteille."
        );

        lang.put("grimoire.alcoholic.beer.ch10.title", "Ce qu'il ne faut pas faire");
        lang.put(
                "grimoire.alcoholic.beer.ch10.p0",
                "Pas de pressoir : la biere ne se presse pas. Pas de fut ni de terrine sur le graphe officiel. Pas d'elevage officiel. Le conditionnement est industriel seulement et hors graphe. Ne collez pas le broyeur sans moteur : il cale."
        );
        lang.put(
                "grimoire.alcoholic.beer.ch10.p1",
                "Ne mettez pas de magma sous le chaudron. Ne mettez pas de feu de camp sous la cuve d'empatage. Ne bouteillez pas le mout. Les coques industrielles 7B sont d'autres executeurs du meme travail, pas d'autres recettes, et pas necessaires."
        );
        return lang;
    }
}
