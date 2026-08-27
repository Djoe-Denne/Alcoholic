# Prompts ChatGPT — machines encore sans art custom

Hors `brewing_kettle` et `artisanal_fermenter` (déjà faits).

Coller **un prompt à la fois**. Joindre si possible une capture de `mash_tun`, `primitive_combustion_engine` et `brewing_kettle` comme références de style.

- Machines artisanales et moteur : **un prompt = le modèle voxel complet** (référence Blockbench).
- Multiblocs : **un prompt par bloc unitaire** (texture de cube) **et un prompt par machine formée** (vue d’ensemble).

Chaque prompt demande une image carrée, sans texte.

---

## Comment coller

1. « Génère une image à partir de ce prompt, carré 1024 ou 1536, une seule image. »
2. Si le résultat est trop réaliste : relancer en ajoutant `more painted, less photo, BDcraft Minecraft`.
3. Si le chêne est trop foncé : `warm medium oak RGB 108 69 35, oval knots, not chocolate`.

---

## Bible de style (déjà dans chaque prompt)

- Minecraft Java 1.19, BDcraft peint / comic voxel, pas vanilla, pas photo, pas anime.
- Dégradés doux, rivets lisibles, pas de grille UV 1 px, pas de watermark, pas de logo, pas de texte.
- **Chêne verrouillé** (fût, pressoir, crock bois) : chêne moyen chaud, teinte ≈ 28°, RGB moyen ≈ (108, 69, 35), veinage fluide, nœuds ovales. Pas chocolat, pas miel. Douves = même tuile tournée 90°. Pieds × 0.88.
- **Fer** : plaques bleu-gris foncé, rivets peints, même langage que mash tun / moteur à combustion.
- **Cuivre** : chaudron de houblonnage (pas de laiton steampunk).
- **Acier industriel** : gris-bleu peint, pas rouille, pas chrome Create, pas IE.
- Face utile vers la caméra (−Z).

---

# A. Machines artisanales (1 prompt chacune)

## A1. `oak_barrel` — fût de chêne (vieillissement)

```
Génère une image de référence 3D voxel Minecraft Java 1.19 pour un fût de chêne artisanal de 1×1×1 bloc, style BDcraft peint / comic, comme une machine Faithful HD peinte à la main. PAS photo, PAS vanilla sale, PAS anime, PAS steampunk.

Le fût est COUCHÉ (axe horizontal), tonneau à douves de chêne, deux cerclages de fer épais + un cerclage central, bonde / bung sur la FACE AVANT vers la caméra, petit robinet de dégustation en fer bas-avant. Fonds du fût visibles à gauche et à droite, légèrement bombés. Quatre petits pieds / cales en chêne plus sombre (×0.88) pour qu’il ne flotte pas.

Chêne OBLIGATOIRE : chêne moyen chaud, teinte ~28°, RGB moyen ~(108, 69, 35), veinage peint fluide, nœuds ovales. INTERDIT : chêne chocolat, chêne miel, bouleau, pixels sales.

Fer : plaques bleu-gris foncé, rivets peints ronds, même métal que le mash tun Alcoholic.

Vue 3/4 légèrement dessus, fond atelier sombre uni, une seule machine, aucun texte, aucun UI, aucun watermark. Silhouette lisible, pas de transparence, pas d’<item> Minecraft flottant.
```

## A2. `artisanal_blending_crock` — terrine d’assemblage

```
Génère une image de référence 3D voxel Minecraft Java 1.19 pour une terrine d’assemblage artisanale (blending crock) de 1×1×1 bloc, style BDcraft peint / comic. PAS photo, PAS vanilla, PAS anime.

C’est une grande jarre / crock de grès émaillé pour assembler des vins, PAS un fût, PAS un fermenteur à douves. Corps cylindrique un peu ventru, lèvre épaisse, couvercle en chêne plat avec poignée, cerclage de fer en haut et en bas. Petit bec verseur ou échancrure sur la FACE AVANT. Intérieur à peine visible sous le couvercle : émail crème.

Grès : terre cuite chaude grise-beige, émail satiné peint (pas céramique photo, pas porcelaine blanche hospitalière).
Couvercle et éventuellement un cercle de bois : CHÊNE VERROUILLÉ Alcoholic, chêne moyen chaud RGB ~(108, 69, 35), nœuds ovales, pas chocolat.
Fer : bleu-gris foncé riveté, langage mash tun.

Vue 3/4 légèrement dessus, fond sombre uni, une seule machine, aucun texte, aucun UI. Silhouette ronde et lourde, pieds très bas ou anneau de base.
```

## A3. `artisanal_press` — pressoir artisanal

```
Génère une image de référence 3D voxel Minecraft Java 1.19 pour un pressoir à vis artisanal de 1×1×1 bloc, style BDcraft peint / comic. PAS photo, PAS vanilla, PAS cage à vin réaliste, PAS presse Create.

Cage / panier cylindrique à douves de CHÊNE avec fentes verticales pour le jus, plateau inférieur en chêne, vis et écrou en FER au-dessus, long levier / barre de vis horizontale sur le haut, plateau presseur en bois, petit bec d’écoulement sur la FACE AVANT (−Z). Quatre pieds en chêne plus sombre.

Chêne verrouillé : moyen chaud RGB ~(108, 69, 35), veinage fluide, nœuds ovales, douves verticales (grain à 90°). Pas chocolat, pas miel.
Fer : vis, écrou, cercles, bec — bleu-gris foncé, rivets peints.

La machine doit rentrer dans un cube 16×16×16 (rien qui dépasse trop). Vue 3/4, fond sombre uni, aucun texte, aucun raisin décoratif en tas énorme, pas de flaque photo. Une seule machine lisible.
```

## A4. `electric_motor` — moteur électrique (éteint + allumé)

```
Génère une image de référence 3D voxel Minecraft Java 1.19 pour un petit moteur électrique compact 1×1×1, contenu NATIF du mod Alcoholic (PAS Immersive Engineering, PAS Create, PAS moteur diesel). Style BDcraft peint / comic. PAS photo.

Boîtier acier peint gris-bleu, ailettes de refroidissement, capot boulonné, boîte à bornes FE sur le dessus ou le côté, ARBRE / axe de sortie sur la FACE AVANT (−Z) pour s’accoupler à un port cinétique. Pied plat. Cuivre seulement en petits détails (bagues, bobinage entrevu), fer bleu-gris pour la structure.

DEMANDE : un SPLIT image, même modèle deux fois :
- Gauche : état ÉTEINT, pas de lueur.
- Droite : état ALLUMÉ, fentes / voyants ambre-orangé doux, pas de neon cyberpunk, pas d’éclairs.

Fond sombre uni, aucun texte, aucun logo, aucun câble IE. Vue 3/4 légèrement dessus.
```

Si tu veux deux fichiers séparés (`electric_motor` / `electric_motor_on`), relance le même prompt en demandant seulement l’état éteint, puis seulement l’état allumé.

---

# B. Blocs unitaires industriels (textures de cube)

Ces blocs sont aujourd’hui des `cube_all` générés. Un prompt = **un cube 1×1 peint**, face utile vers la caméra. Ils sont partagés par tous les multiblocs.

## B1. `industrial_casing` — revêtement

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube plein, style BDcraft peint, revêtement industriel Alcoholic.

Plaques d’acier peint gris-bleu (pas rouille, pas chrome, pas andésite Create). Quatre rivets d’angle peints, léger dégradé haut/bas, joint de plaque en croix très soft. Aucune fenêtre, aucun tuyau, aucun logo, aucun texte.

Vue 3/4 d’un cube isolé, fond sombre uni, éclairage atelier. PAS photo, PAS grille pixel sale 1 px. Le bloc doit rester tileable visuellement si on en aligne plusieurs (pas de flèche, pas de numéro, pas de face unique trop criarde).
```

## B2. `machine_window` — hublot

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube, style BDcraft peint : hublot de machine industrielle Alcoholic.

Cadre acier gris-bleu épais, rivets, VITRE centrale bleutée peinte (verre comic, pas photo, pas transparence réelle). Derrière le verre : un peu d’ombre d’intérieur creux, pas de pièce de théâtre. Croisillon optionnel très fin.

Vue 3/4 d’un cube isolé, fond sombre. Aucun texte, aucun logo, pas de reflets HDR. Doit rester lisible en 16×16.
```

## B3. `access_hatch` — trappe d’accès

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube, style BDcraft peint : trappe d’accès industrielle Alcoholic.

Même acier gris-bleu que le casing, mais une PORTE / trappe rivetée sur la face avant, poignée / loquet LAITON-ambre discret (accent or-brun, pas or chrome). Charnières peintes. Pas de fenêtre.

Vue 3/4 d’un cube isolé, fond sombre. Aucun texte. Accent laiton petit, le bloc reste surtout acier.
```

## B4. `fluid_port` — port fluide

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube, style BDcraft peint : port de fluide industriel Alcoholic.

Corps = même acier casing. FACE AVANT : bride ronde + orifice de tuyau, petit volant / vanne. Accent BLEU industriel discret (anneau ou pastille), pas de flaque, pas de seau Minecraft vanilla collé.

Vue 3/4, fond sombre, aucun texte, aucun logo Create. Lisibilité 16×16 : un cercle de bride très clair.
```

## B5. `item_port` — port d’objets

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube, style BDcraft peint : port d’objets industriel Alcoholic.

Corps acier casing. FACE AVANT : trappe / gueule d’entonnoir carrée, un peu comme une hoppe peinte, pas l’item hopper vanilla collé. Accent AMBRE / orange discret.

Vue 3/4, fond sombre, aucun texte, aucun item qui flotte. Lisibilité 16×16 : ouverture carrée évidente.
```

## B6. `kinetic_port` — port cinétique

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube, style BDcraft peint : port cinétique / accouplement d’arbre Alcoholic.

Corps acier casing. FACE AVANT : palier + bout d’ARBRE hexagonal / cylindrique qui dépasse un peu, prêt à recevoir un moteur ou un axe. Accent VERT industriel discret. PAS de cogwheel Create, PAS de moulin à vent.

Vue 3/4, fond sombre, aucun texte. Lisibilité 16×16 : un axe central évident.
```

---

# C. Contrôleurs unitaires (machine NON formée)

Texture du bloc contrôleur posé tout seul, avant que le multibloc soit valide. Même langage acier + une identité de métier petite et peinte (pas de lettrage).

## C1. `industrial_press_controller`

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube, style BDcraft peint : contrôleur de PRESSOIR industriel Alcoholic, état NON FORMÉ.

Acier gris-bleu, face avant = petit pupitre : levier, deux voyants éteints, pictogramme peint d’une PRESSE / plaque qui descend (sans texte). Accent ROUGE sombre discret. Air « machine éteinte, pas encore assemblée ».

Vue 3/4, fond sombre, aucun texte, aucun logo.
```

## C2. `industrial_roller_mill_controller`

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube, style BDcraft peint : contrôleur de LAMINOIR / moulin à cylindres industriel Alcoholic, état NON FORMÉ.

Acier gris-bleu, face avant pupitre, pictogramme peint de DEUX CYLINDRES horizontaux. Accent gris-jaune / meule discret. Voyants éteints. Pas de grain en tas.

Vue 3/4, fond sombre, aucun texte.
```

## C3. `industrial_vat_controller`

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube, style BDcraft peint : contrôleur de CUVE DE FERMENTATION industrielle Alcoholic, état NON FORMÉ.

Acier gris-bleu, pupitre avant, pictogramme peint d’une CUVE ronde + bulles / airlock très simple. Accent VERT discret. Voyants éteints.

Vue 3/4, fond sombre, aucun texte, pas de levure réaliste.
```

## C4. `industrial_tank_controller`

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube, style BDcraft peint : contrôleur de RÉSERVOIR DE STOCKAGE industriel Alcoholic, état NON FORMÉ. Ce n’est PAS une machine de process.

Acier gris-bleu, pupitre simple, pictogramme peint d’une CITERNE / jauge. Accent BLEU-GRIS. Pas de serpentins, pas de vapeur, pas de presse.

Vue 3/4, fond sombre, aucun texte.
```

## C5. `industrial_malt_house_controller`

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube, style BDcraft peint : contrôleur de MALTERIE industrielle Alcoholic, état NON FORMÉ.

Acier gris-bleu, pupitre, pictogramme peint d’un PLANCHER / grain + petite flamme de touraille très sobre. Accent orge / ambre discret. Pas de champs, pas d’épis photo.

Vue 3/4, fond sombre, aucun texte.
```

## C6. `industrial_mash_tun_controller`

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube, style BDcraft peint : contrôleur de CUVE DE BRASSAGE / mash tun industrielle Alcoholic, état NON FORMÉ.

Acier gris-bleu, pupitre, pictogramme peint d’une CUVE isolée + pale / râteau. Accent cuivre TRÈS petit (rappel du mash tun artisanal), le bloc reste acier.

Vue 3/4, fond sombre, aucun texte.
```

## C7. `industrial_brewing_kettle_controller`

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube, style BDcraft peint : contrôleur de CHAUDIÈRE DE HOUBLONNAGE industrielle Alcoholic, état NON FORMÉ.

Acier gris-bleu (casing pressure-safe), pupitre, pictogramme peint d’un CHAUDRON / dôme + vapeur. Accent CUIVRE plus présent que sur les autres contrôleurs, mais pas un cube tout cuivre.

Vue 3/4, fond sombre, aucun texte, pas de houblon en décor photo.
```

## C8. `industrial_conditioning_vessel_controller`

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube, style BDcraft peint : contrôleur de CUVE DE CONDITIONNEMENT / garde industrielle Alcoholic, état NON FORMÉ.

Acier gris-bleu, pupitre, pictogramme peint d’une cuve CHEMISEE + petit flocon / froid. Accent bleu-froid discret. Ambiance calme, pas de fermentation bouillonnante, pas de presse.

Vue 3/4, fond sombre, aucun texte.
```

---

# D. Modèles formés (vue d’ensemble)

Une machine = cuboïde CREUX en blocs 1 m. Montrer un exemple typique (pas le max). On doit lire : casing + hublots + trappe + ports + 1 contrôleur. Intérieur vide (capacité = volume intérieur).

## D1. Pressoir industriel formé — `industrial_press`

Taille typique : **3×4×3** (X×Y×Z). Casing pressure-safe. **Port cinétique obligatoire.**

```
Génère une image de référence 3D voxel Minecraft Java 1.19 : MULTIBLOC pressoir industriel Alcoholic FORMÉ. Style BDcraft peint / comic. PAS photo, PAS usine réelle, PAS Create Mechanical Press.

Cuboïde CREUX d’environ 3 blocs de large, 4 de haut, 3 de profondeur, assemblé en blocs 1×1 : revêtement acier gris-bleu riveté, 1 ou 2 hublots, une trappe, un port FLUIDE, un port ITEM, un port CINÉTIQUE (arbre) sur un côté, un CONTRÔLEUR rouge-sombre sur la face avant.

À travers les hublots : une PLAQUE / plateau de presse vertical (platen) en fer, comme une presse à raisin / cidre industrielle, pas un piston vanilla. Intérieur surtout vide.

Vue 3/4 légèrement dessus, fond atelier sombre, aucun texte, aucun ouvrier, aucun logo. Échelle Minecraft claire (on compte les blocs).
```

## D2. Laminoir industriel formé — `industrial_roller_mill`

Taille typique : **3×4×3** (max 5×6×5). Port cinétique obligatoire. Plus compact.

```
Génère une image de référence 3D voxel Minecraft Java 1.19 : MULTIBLOC laminoir / moulin à cylindres industriel Alcoholic FORMÉ. Style BDcraft peint. PAS Create crushing wheels, PAS moulin à vent.

Petit cuboïde CREUX ~3×4×3 en blocs acier gris-bleu, hublots, trappe, ports fluide + item + CINÉTIQUE, contrôleur avec pictogramme deux cylindres.

À travers les hublots : DEUX GROS CYLINDRES métalliques horizontaux (meules à malt), pas de roues Create. Intérieur sinon vide.

Vue 3/4, fond sombre, aucun texte, on doit pouvoir compter les blocs.
```

## D3. Cuve de fermentation formée — `industrial_fermentation_vat`

Taille typique : **3×5×3** (peut monter très haut). Casing fermenter.

```
Génère une image de référence 3D voxel Minecraft Java 1.19 : MULTIBLOC cuve de fermentation industrielle Alcoholic FORMÉE. Style BDcraft peint. PAS fermenteur à douves chêne (ça c’est l’artisanal).

Cuboïde CREUX un peu plus HAUT que large, ~3×5×3, coque acier gris-bleu, hublots (on entrevoit un intérieur sombre / niveau de liquide peint très sobre), trappe, ports fluide + item, contrôleur vert. Option : un airlock / évent sur le toit en blocs.

Pas de chêne. Pas de bulles photo. Vue 3/4, fond sombre, aucun texte, échelle blocs lisible.
```

## D4. Réservoir de stockage formé — `industrial_storage_tank`

Taille typique : **3×5×3**. Passif : pas d’équipement de process.

```
Génère une image de référence 3D voxel Minecraft Java 1.19 : MULTIBLOC réservoir de stockage industriel Alcoholic FORMÉ. Style BDcraft peint. Ce n’est PAS une cuve de fermentation, PAS un mash tun, PAS une chaudière.

Cuboïde CREUX ~3×5×3, acier gris-bleu très sobre, hublots de jauge, trappe, ports fluide + item, contrôleur bleu-gris. Toit plat simple. AUCUN serpentin, AUCUNE vapeur, AUCUN plateau de presse, AUCUN cylindre de moulin.

Vue 3/4, fond sombre, aucun texte, silhouette de citerne / silo cubique calme.
```

## D5. Malterie industrielle formée — `industrial_malt_house`

Taille typique : **5×4×5**. Casing fermenter. Process malt (trempage / germination / touraille).

```
Génère une image de référence 3D voxel Minecraft Java 1.19 : MULTIBLOC malterie industrielle Alcoholic FORMÉE. Style BDcraft peint. PAS le malting_floor artisanal en bois.

Cuboïde CREUX ~5×4×5, acier gris-bleu, plusieurs hublots, trappe, ports item + fluide, contrôleur ambre. Toit avec évents / cheminée de séchage très voxel. À travers un hublot : plancher perforé / couche de grain PEINT (jaune orge sobre), pas de photo de malt.

Vue 3/4 légèrement dessus, fond sombre, aucun texte, on compte les blocs.
```

## D6. Mash tun industriel formé — `industrial_mash_tun`

Taille typique : **5×5×5**. Très stable thermiquement.

```
Génère une image de référence 3D voxel Minecraft Java 1.19 : MULTIBLOC cuve de concassage / empâtage industrielle Alcoholic FORMÉE. Style BDcraft peint. C’est la sœur industrielle du mash tun chêne (déjà existant), donc ACIER + un peu de CUIVRE, pas tout en chêne.

Cuboïde CREUX ~5×5×5, parois acier isolées (plaques épaisses), hublots, trappe, ports, contrôleur avec petite note cuivre. Toit : dôme / couvercle bas + évent. À travers un hublot : fond de cuve + pale / râteau très simple. Pas de grain en explosion.

Vue 3/4, fond sombre, aucun texte, échelle blocs.
```

## D7. Chaudière de houblonnage industrielle formée — `industrial_brewing_kettle`

Taille typique : **5×5×5**. Casing pressure-safe. Sœur industrielle du brewing kettle cuivre déjà fait.

```
Génère une image de référence 3D voxel Minecraft Java 1.19 : MULTIBLOC chaudière de houblonnage industrielle Alcoholic FORMÉE. Style BDcraft peint. Relier visuellement au chaudron cuivre artisanal (dôme, cercles, rivets) MAIS à l’échelle usine : coque acier + INTÉRIEUR / dôme CUIVRE.

Cuboïde CREUX ~5×5×5, casing acier riveté, hublots où l’on voit le cuivre intérieur, cheminée / cheminée de vapeur sur le toit, trappe, ports (dont fluide), contrôleur avec accent cuivre. PAS de chêne. PAS de houblon photo.

Vue 3/4, fond sombre, aucun texte, on compte les blocs.
```

## D8. Cuve de conditionnement formée — `industrial_conditioning_vessel`

Taille typique : **3×6×3** (plus haute que large). Garde / maturation, ambiance froide.

```
Génère une image de référence 3D voxel Minecraft Java 1.19 : MULTIBLOC cuve de conditionnement / garde industrielle Alcoholic FORMÉE. Style BDcraft peint. PAS une cuve de fermentation active (pas de bulles), PAS un tank de stockage nu.

Cuboïde CREUX plus HAUT ~3×6×3, acier gris-bleu, CHEMISE / serpentins peints discrets sur les flancs (froid), hublots, trappe, ports, contrôleur bleu-froid. Toit bombé type bright tank voxel. Ambiance calme, un peu plus claire/froide que la cuve de ferment.

Vue 3/4, fond sombre, aucun texte, échelle blocs.
```

---

# Hors périmètre

Déjà custom (ne pas régénérer) : `primitive_combustion_engine`, `malting_floor`, `mash_tun`, `malt_mill`, `brewing_kettle`, `artisanal_fermenter`.

Pas des machines à art custom : vignes, poteaux, cultures, items plats.

Les trois tags de casing (`fermenter_casing`, `pressure_safe_casing`, `industrial_tank_casing`) réutilisent aujourd’hui les **mêmes** blocs (`industrial_casing` + `access_hatch`). Un seul casing, un seul hatch.
