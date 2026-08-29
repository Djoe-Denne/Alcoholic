# Prompts ChatGPT — machines craft (ligne bière)

Vague **craft** : intermédiaire entre l’artisanal 1×1 (chêne / cuivre) et l’usine acier. Cuboïde creux **3×3×3 à 5×5×5**. Pas de mega-mesh en jeu pour l’instant : formé = coque 9-slice `craft_casing` + fittings 1×1.

Coller **un prompt à la fois**. Joindre si possible : `brewing_kettle` (cuivre Alcoholic), `mash_tun` (chêne), `industrial_casing` / un contrôleur industriel (acier), pour caler l’échelle « atelier chaudronnier ».

Références déjà générées (à peindre ensuite, un id à la fois) :

| Id | Fichier |
|---|---|
| `craft_casing` | `art/blockbench/craft_casing/reference.png` |
| `craft_malt_house_controller` | `art/blockbench/craft_malt_house_controller/reference.png` |
| `craft_mill_controller` | `art/blockbench/craft_mill_controller/reference.png` |
| `craft_mash_tun_controller` | `art/blockbench/craft_mash_tun_controller/reference.png` |
| `craft_brewing_kettle_controller` | `art/blockbench/craft_brewing_kettle_controller/reference.png` |
| `craft_vat_controller` | `art/blockbench/craft_vat_controller/reference.png` |
| `craft_malt_house` (formé) | `art/blockbench/craft_malt_house/reference.png` |
| `craft_mill` (formé) | `art/blockbench/craft_mill/reference.png` |
| `craft_mash_tun` (formé) | `art/blockbench/craft_mash_tun/reference.png` |
| `craft_brewing_kettle` (formé) | `art/blockbench/craft_brewing_kettle/reference.png` |
| `craft_vat` (formé) | `art/blockbench/craft_vat/reference.png` |

Chaque prompt demande une image **carrée**, sans texte.

---

## Comment coller

1. « Génère une image à partir de ce prompt, carré 1024 ou 1536, une seule image. »
2. Trop photo : `more painted, less photo, BDcraft Minecraft`.
3. Cuivre trop laiton / steampunk : `workshop copper like Alcoholic brewing_kettle, warm reddish copper, not brass, not chrome`.
4. Chêne trop foncé (si un détail bois apparaît par erreur) : `warm medium oak RGB 108 69 35, oval knots, not chocolate` — **mais le craft n’est pas en chêne**.

---

## Bible de style

- Minecraft Java 1.19, BDcraft peint / comic voxel. PAS vanilla, PAS photo, PAS anime, PAS Create, PAS Immersive Engineering.
- Dégradés doux, rivets lisibles, pas de grille UV 1 px, pas de watermark, pas de logo, pas de texte.
- **Cuivre craft (coque)** : cuivre d’atelier / chaudronnerie, même famille que `brewing_kettle`. Roux-chaud, plaques rivetées, patine peinte légère. PAS laiton steampunk, PAS or, PAS `copper_block` vanilla brillant, PAS chrome.
- **Fer** (pupitre, brides, cercles, ports réutilisés) : bleu-gris foncé riveté, langage mash tun / moteur.
- **Acier industriel** : **interdit sur le casing craft**. Les fittings partagés (`machine_window`, `access_hatch`, `fluid_port`, `item_port`, `kinetic_port`) restent acier : c’est volontaire (même plomberie que l’usine).
- **Chêne** : interdit sur les blocs craft. Le chêne reste aux machines artisanales 1×1.
- Face utile vers la caméra (**−Z**).
- Unformed = cube 1×1. Formed = cuboïde creux, intérieur vide (capacité = cellules intérieures).

## Place dans la gamme

| Échelle | Coque | Taille | Sensation |
|---|---|---|---|
| Artisanal | chêne (+ cuivre sur le chaudron) | 1×1×1 | ferme, fût, plancher |
| **Craft** | **cuivre d’atelier** | **3³–5³** | brasserie de village, chaudronnier |
| Industriel | acier gris-bleu | 3×4×3 et plus | usine |

---

## Fittings déjà existants — NE PAS régénérer

Ces blocs sont **partagés** avec l’industriel. Prompts déjà dans `chatgpt-machines-restantes.md` § B2–B6.

| Id | Rôle sur un craft |
|---|---|
| `machine_window` | Hublot acier + verre bleuté. Sur coque cuivre : on voit l’intérieur sombre / l’équipement peint. |
| `access_hatch` | Trappe acier, loquet laiton-ambre. |
| `fluid_port` | Bride + vanne, accent bleu. |
| `item_port` | Gueule carrée / hoppe, accent ambre. |
| `kinetic_port` | Arbre, accent vert. **Obligatoire seulement sur `craft_mill`.** |

Sur une vue formée : **coque cuivre + ces fittings acier**. Ne pas recuire les ports en cuivre.

---

# A. Bloc de coque (nouveau)

## A1. `craft_casing` — revêtement craft

Bloc unique partagé par les cinq familles. Doit être **tileable**.

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube plein, style BDcraft peint, revêtement CRAFT Alcoholic (pas industriel).

Plaques de CUIVRE d’atelier chaudronnier, roux-chaud, rivets ronds peints aux quatre coins, léger dégradé haut/bas, joint de plaque en croix très soft. Patine peinte discrète (pas vert-de-gris photo, pas rouille). AUCUNE fenêtre, aucun tuyau, aucun logo, aucun texte.

Ce n’est PAS l’acier gris-bleu industrial_casing. PAS laiton steampunk. PAS le copper_block vanilla brillant. PAS de chêne.

Vue 3/4 d’un cube isolé, fond atelier sombre uni, éclairage atelier. PAS photo, PAS grille pixel sale 1 px. Le bloc doit rester tileable si on en aligne plusieurs (pas de flèche, pas de numéro, pas de face unique trop criarde).
```

---

# B. Contrôleurs unitaires (machine NON formée)

Cube 1×1 posé tout seul. Corps **cuivre craft** + petit pupitre fer + pictogramme peint (pas de lettrage). Voyants éteints. Air « machine éteinte, pas encore assemblée ».

## B1. `craft_malt_house_controller`

Sœur du `malting_floor` (chêne, 1×1) et de `industrial_malt_house_controller` (acier). Ici : touraille d’atelier.

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube, style BDcraft peint : contrôleur de MALTERIE CRAFT Alcoholic, état NON FORMÉ.

Corps CUIVRE d’atelier (plaques rivetées, même famille que brewing_kettle). Face avant (−Z) = petit pupitre FER bleu-gris : levier, deux voyants éteints, pictogramme peint d’un PLANCHER PERFORÉ / grain + petite flamme de touraille très sobre. Accent orge-ambre discret. Pas de champs, pas d’épis photo, PAS de chêne malting_floor, PAS d’acier usine.

Vue 3/4 légèrement dessus, fond sombre uni, aucun texte, aucun logo.
```

## B2. `craft_mill_controller`

Sœur du `malt_mill` (1×1) et de `industrial_roller_mill_controller`. Port cinétique obligatoire une fois formé — le cube seul le suggère.

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube, style BDcraft peint : contrôleur de MOULIN CRAFT Alcoholic, état NON FORMÉ.

Corps CUIVRE d’atelier riveté. Face avant pupitre FER, pictogramme peint de DEUX PETITS CYLINDRES horizontaux (meules à malt). Accent gris-jaune / meule discret. Voyants éteints. Petit bout d’arbre ou palier peint sur le côté droit (rappel kinetic), PAS de cogwheel Create, PAS de moulin à vent, pas de grain en tas.

Vue 3/4, fond sombre, aucun texte.
```

## B3. `craft_mash_tun_controller`

Sœur du `mash_tun` chêne et de `industrial_mash_tun_controller` acier.

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube, style BDcraft peint : contrôleur de CUVE D’EMPÂTAGE CRAFT Alcoholic, état NON FORMÉ.

Corps CUIVRE d’atelier, un peu plus « cuve isolée » que les autres (bande / cerclage fer). Face avant pupitre FER, pictogramme peint d’une CUVE + pale / râteau. Accent cuivre déjà partout : le pictogramme reste sobre, un peu de fer pour le pupitre. PAS le mash_tun à douves de chêne. PAS l’acier usine.

Vue 3/4, fond sombre, aucun texte, pas de grain en explosion.
```

## B4. `craft_brewing_kettle_controller`

Sœur du `brewing_kettle` cuivre 1×1 (déjà fait) et de `industrial_brewing_kettle_controller` acier+cuivre. Le craft est le chaudron d’atelier agrandi, pas l’usine.

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube, style BDcraft peint : contrôleur de CHAUDIÈRE DE HOUBLONNAGE CRAFT Alcoholic, état NON FORMÉ.

Corps CUIVRE chaudronnier (plus « dôme / chaudron » que plaque plane : bande supérieure un peu bombée peinte). Face avant pupitre FER, pictogramme peint d’un CHAUDRON / dôme + filet de vapeur. Accent fer-bleu pour le pupitre seulement. Relier visuellement au brewing_kettle artisanal, PAS à une chaudière usine acier. PAS de houblon photo, PAS de chêne, PAS de laiton steampunk.

Vue 3/4, fond sombre, aucun texte.
```

## B5. `craft_vat_controller`

Sœur de `artisanal_fermenter` (douves chêne) et de `industrial_vat_controller` (silo acier). Ici : cuve d’atelier cuivre, calme.

```
Génère une image d’un SEUL bloc Minecraft 1×1×1, cube, style BDcraft peint : contrôleur de CUVE DE FERMENTATION CRAFT Alcoholic, état NON FORMÉ.

Corps CUIVRE d’atelier, cerclages FER. Face avant pupitre FER, pictogramme peint d’une CUVE ronde + airlock / bulles très simples. Accent VERT discret. Voyants éteints. PAS de douves de chêne (artisanal_fermenter). PAS de silo usine haut. Pas de levure réaliste.

Vue 3/4, fond sombre, aucun texte.
```

---

# C. Multiblocs formés (vue d’ensemble)

Une machine = cuboïde **CREUX** en blocs 1 m. Taille d’art typique = **minimum légal 3×3×3** (showcase debug). Max code = 5×5×5. On doit lire : casing cuivre + hublots acier + trappe + ports + 1 contrôleur. Intérieur surtout vide.

Kit face −Z (idéal 3×3, compressé) :
- bas centre : `item_port`
- au-dessus : contrôleur craft
- mi-hauteur à droite : `fluid_port`
- centre : `access_hatch`
- 1 ou 2 `machine_window`
- **mill seulement** : `kinetic_port` sur +X (arbre)

## C1. `craft_malt_house` — malterie craft formée

Kind MALT / `alcoholic:malt`. Pas de port cinétique. Sœur du `malting_floor` et de `industrial_malt_house` (5×4×5 acier).

```
Génère une image de référence 3D voxel Minecraft Java 1.19 : MULTIBLOC malterie CRAFT Alcoholic FORMÉE. Style BDcraft peint / comic. PAS photo, PAS le malting_floor en chêne, PAS la malterie industrielle acier.

Cuboïde CREUX d’environ 3 blocs de large, 3 de haut, 3 de profondeur, assemblé en blocs 1×1 : coque CUIVRE d’atelier rivetée (craft_casing), 1 ou 2 hublots ACIER (machine_window), une trappe acier, un port ITEM, un port FLUIDE, un CONTRÔLEUR cuivre avec pictogramme plancher + flamme sur la face avant (−Z). Toit : 1 petite cheminée / évent de touraille très voxel (art only).

À travers un hublot : plancher perforé + couche de grain PEINT (ambre orge sobre), pas de photo de malt. Intérieur sinon vide. PAS de chêne. PAS d’acier en coque.

Vue 3/4 légèrement dessus, fond atelier sombre, aucun texte, aucun ouvrier, aucun logo. Échelle Minecraft claire (on compte les 3×3×3 blocs).
```

## C2. `craft_mill` — moulin craft formé

Kind MILL / `alcoholic:mill`. **Port cinétique obligatoire.** Sœur du `malt_mill` et de `industrial_roller_mill` (3×4×3 acier).

```
Génère une image de référence 3D voxel Minecraft Java 1.19 : MULTIBLOC moulin / petits cylindres CRAFT Alcoholic FORMÉ. Style BDcraft peint. PAS Create crushing wheels, PAS moulin à vent, PAS malt_mill 1×1 tout seul.

Petit cuboïde CREUX ~3×3×3 en blocs CUIVRE d’atelier, hublots acier, trappe, ports fluide + item, CONTRÔLEUR cuivre (pictogramme deux cylindres) face −Z, et un PORT CINÉTIQUE acier (arbre vert) sur le côté +X, prêt à recevoir un moteur.

À travers les hublots : DEUX PETITS CYLINDRES métalliques horizontaux (meules à malt), fer + un peu de cuivre, pas de roues Create. Intérieur sinon vide.

Vue 3/4, fond sombre, aucun texte, on doit pouvoir compter les blocs.
```

## C3. `craft_mash_tun` — cuve d’empâtage craft formée

Kind MASH / `alcoholic:mash`. Pas de cinétique. Sœur du `mash_tun` chêne et de `industrial_mash_tun` (5×5×5 acier). Capacité 2000 / cellule (1 cellule à 3×3×3).

```
Génère une image de référence 3D voxel Minecraft Java 1.19 : MULTIBLOC cuve d’empâtage CRAFT Alcoholic FORMÉE. Style BDcraft peint. C’est la sœur d’atelier du mash_tun chêne : CUIVRE isolé, PAS de douves, PAS l’acier usine 5×5×5.

Cuboïde CREUX ~3×3×3, parois cuivre épaisses / cerclages fer, hublots acier, trappe, ports item + fluide, contrôleur cuivre + pale peinte. Toit : couvercle bas / dôme très plat + évent. À travers un hublot : fond de cuve + pale / râteau très simple. Pas de grain en explosion. PAS de chêne.

Vue 3/4, fond sombre, aucun texte, échelle blocs 3×3×3.
```

## C4. `craft_brewing_kettle` — chaudière craft formée

Kind BOIL / `alcoholic:boil`. Pas de cinétique. Sœur du `brewing_kettle` cuivre 1×1 et de `industrial_brewing_kettle` (acier + dôme cuivre usine).

```
Génère une image de référence 3D voxel Minecraft Java 1.19 : MULTIBLOC chaudière de houblonnage CRAFT Alcoholic FORMÉE. Style BDcraft peint. Relier visuellement au chaudron cuivre artisanal (dôme, cercles, rivets) MAIS en petit cuboïde d’atelier, PAS l’usine acier.

Cuboïde CREUX ~3×3×3, coque CUIVRE rivetée, hublots acier où l’on entrevoit un intérieur cuivre sombre / niveau de moût peint très sobre, petite cheminée de vapeur sur le toit (art only), trappe, ports (dont fluide), contrôleur cuivre + pictogramme dôme. PAS de chêne. PAS de houblon photo. PAS de laiton steampunk. Moins « citerne acier » que l’industriel, plus « gros chaudron habillé de plaques ».

Vue 3/4, fond sombre, aucun texte, on compte les blocs.
```

## C5. `craft_vat` — cuve de fermentation craft formée

Kind FERMENT / `alcoholic:ferment`. Pas de cinétique. Sœur de `artisanal_fermenter` (chêne) et de `industrial_fermentation_vat` (silo acier haut). CONDITION n’existe pas en craft.

```
Génère une image de référence 3D voxel Minecraft Java 1.19 : MULTIBLOC cuve de fermentation CRAFT Alcoholic FORMÉE. Style BDcraft peint. PAS le fermenteur à douves de chêne, PAS le silo usine 3×5×3.

Cuboïde CREUX un peu trapu ~3×3×3, coque CUIVRE + cerclages FER, hublots acier (intérieur sombre / niveau de liquide peint sobre, 1–2 bulles comic max), trappe, ports fluide + item, contrôleur cuivre accent vert, petit airlock / évent sur le toit (art only).

Pas de chêne. Pas de bulles photo. Ambiance calme d’atelier, pas une usine. Vue 3/4, fond sombre, aucun texte, échelle blocs lisible.
```

---

# Hors périmètre

Ne pas régénérer : `malting_floor`, `malt_mill`, `mash_tun`, `brewing_kettle`, `artisanal_fermenter`, tous les `industrial_*`, fittings B2–B6.

Pas de craft pour : press, tank, barrel, crock, `CONDITION`.

Pas de mega-mesh `block/formed/craft_*` pour l’instant : ces vues d’ensemble sont des **références d’image**, pas un quatrième bloc jeu.
