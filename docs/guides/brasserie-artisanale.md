# Guide : brasserie artisanale (bière)

Guide joueur pour le DAG officiel `alcoholic:beer`. Create, Crossroads et les multiblocks industriels sont **optionnels**. La bière se termine après `FERMENT`. Il n’y a **pas** de définition `AGE` officielle pour la bière. `CONDITION` est industriel uniquement et hors DAG. Pas de fût, pas d’assemblage.

Vin : voir [vigne artisanale](vigne-artisanale.md).

```text
orge → maltage → orge maltée → mouture → mouture (grist)
grist + eau → empâtage → moût + drêche
moût + houblon → ébullition → moût houblonné
moût houblonné + levure → fermentation → bière → bouteille
```

Sources wiki (QMD, collection `alcoholic-wiki`) : `#fee238` grain-processing, `#576a7d` artisanal-processing, `#a7f96f` mechanical-drive-port, `#7c6a25` fermentation-physics, `#e4e4a3` bottled-beverage-snapshot, `#7ca151` README.

---

## 1. Ce qu’il faut poser

| Machine | Id | Rôle |
|---|---|---|
| Aire de maltage | `alcoholic:malting_floor` | `MALT` uniquement |
| Broyeur à malt | `alcoholic:malt_mill` | `MILL` — **exige un entraînement adjacent** |
| Moteur à combustion primitif | `alcoholic:primitive_combustion_engine` | Puissance rotative (charbon, bois, etc.) |
| Cuve de brassage | `alcoholic:mash_tun` | `MASH` — chaleur **en dessous** |
| Chaudron de houblonnage | `alcoholic:brewing_kettle` | `BOIL` — chaleur **en dessous** |
| Cuve de fermentation artisanale | `alcoholic:artisanal_fermenter` | `FERMENT` (la même que pour le vin) |

Optionnel : moteur électrique (`alcoholic:electric_motor` + FE), millstone/crushing Create, tuyaux Create pour transférer les fluides.

Alcoholic **n’ajoute pas** d’arbres, d’engrenages ni de tuyaux. Les seaux suffisent.

Main vide (sans sneak) sur une machine **ouvre son écran** : slots visibles et jauges de liquide. Un item en main s’insère encore au clic ; les seaux se versent toujours sur le bloc.

### Disposition type

```text
  [moteur]—[broyeur]
  [aire de maltage]

  [magma]          [feu de camp]
     ↑                  ↑
  [cuve mash]      [chaudron]  →  [fermenteur]
```

- Broyeur et moteur **côte à côte** (adjacent).
- Cuve d’empâtage sur un **bloc de magma** (65 °C, bande préférée 62–68 °C).
- Chaudron sur un **feu de camp allumé** ou de la lave (≈ 100 °C, bande préférée 98–105 °C).
- Fermenteur à température ambiante (bande préférée 18–24 °C).

---

## 2. Recettes de craft

### Aire de maltage

```
S S S
P P P
```

`S` = graines de blé, `P` = planches.

### Broyeur à malt

```
S   S
P S P
P P P
```

`S` = dalle de pierre lisse, `P` = planches.

### Moteur à combustion primitif

```
I I I
I F I
I I I
```

`I` = lingot de fer, `F` = fourneau.

### Cuve de brassage (mash tun)

```
P   P
P B P
P P P
```

`P` = planches, `B` = seau.

### Chaudron de houblonnage

```
I   I
I B I
I I I
```

`I` = lingot de fer, `B` = seau.

### Cuve de fermentation artisanale

```
P   P
P   P
P P P
```

`P` = planches.

### Levure (sans forme, ×2)

Champignon brun + sucre.

### Bouteilles vides (×4)

```
  G
G   G
  G
```

`G` = verre.

### Palissage du houblon

- **Poteau de vigne** ×2 : stick au-dessus de deux planches.
- **Poteau d’extrémité** ×2 : pépites de fer + deux bûches.
- **Bobine de fil** : pépites de fer + ficelle.

### Moteur électrique (optionnel)

```
I R I
R C R
I R I
```

`I` = fer, `R` = redstone, `C` = cuivre. Alimenter en Forge Energy.

---

## 3. Cultures

### Orge

Céréale annuelle, 3 stades (comme le blé).

1. Trouver de l’**orge sauvage** dans les plaines, plaines de tournesols ou prairies.
2. Planter les **graines d’orge** sur de la terre labourée.
3. À maturité : 1 orge + 1 graine. La poudre d’os accélère.

Si le mod Brewery est chargé, ses items d’orge sont acceptés via `#alcoholic:barley`. Les ids Alcoholic restent enregistrés.

### Houblon

Bine verticale, pas une vigne. Elle **meurt** sans fil de palissage au-dessus (max 4 blocs).

1. Placer deux poteaux (vigne ou extrémité) au même niveau, alignés.
2. Clic sur le premier poteau avec la bobine, puis sur le second → fil(s) de palissage.
3. Planter le **rhizome de houblon** sur terre / herbe / terre labourée, **sous** le fil.
4. La bine grandit vers le haut (âge 0 → 2, puis un étage au-dessus).
5. Clic droit sur une bine mature : 1 houblon, l’étage revient à l’âge 0.
6. Casser une bine : 1 rhizome (+ 1 houblon si mature).

Premier rhizome : casser du **houblon sauvage** (`alcoholic:wild_hops`) en forêt, forêt fleurie, forêt de bouleaux, taïga ou rivière. Drop : 1 rhizome + 1 houblon. Si Brewery est chargé, ce worldgen est coupé.

---

## 4. Chaîne de production

Un clic à main vide ouvre l’écran de la machine (le statut reste visible en sneak, ou dans l’écran). Les durées sont en ticks (20 ticks = 1 s).

### Étape A — Maltage (80 ticks)

1. Clic droit sur l’aire avec de l’**orge**.
2. Shift + main vide : récupère l’**orge maltée**. Si le slot de sortie est vide, le même geste **cycle** le profil (pâle / ambré / foncé).
3. Le DAG officiel `alcoholic:beer` utilise **`alcoholic:malt_pale`**.

| Profil | Couleur | Fermentescible | Torréfaction |
|---|---|---|---|
| Pâle (défaut) | 0,12 | 0,85 | 0,15 |
| Ambré | 0,35 | 0,78 | 0,45 |
| Foncé | 0,62 | 0,70 | 0,80 |

L’aire ne broie plus : c’est uniquement `MALT`.

### Étape B — Mouture (80 ticks)

1. Placer le **moteur** (ou un moteur électrique / arbre Create / essieu Crossroads) **adjacent** au broyeur.
2. Charger le moteur : clic avec du **combustible de fourneau** (charbon, charbon de bois, bois). Shift + main vide retire le combustible.
3. Clic droit sur le broyeur avec l’**orge maltée**.
4. Sans entraînement, le broyeur **cale** (`progress` reste à 0).
5. Shift + main vide : récupère la **mouture** (`alcoholic:grist`). Les propriétés du malt (sucre, couleur) sont copiées.

Avec Create, le même process `alcoholic:mill_malted_grain` peut aussi tourner sur millstone / crushing wheels.

### Étape C — Empâtage (40 ticks)

1. Poser la cuve **sur** un bloc de magma (65 °C). Hors 52–78 °C le rendement chute ; hors bande le process stagne.
2. Clic avec de la **mouture**.
3. Clic avec un **seau d’eau** (1000 mB). La cuve a deux tanks : on remplit l’entrée, on draine le moût en sortie.
4. Attendre. Résultat : **moût** (`alcoholic:wort`, 1000 mB) + **drêche**.
5. Shift + main vide : récupère la drêche.
6. Seau vide (ou tuyau Create) pour extraire le moût.

### Étape D — Ébullition (40 ticks)

1. Poser le chaudron **sur** un feu de camp allumé ou de la lave (≈ 100 °C). Bande préférée 98–105 °C, opérable 90–110 °C.
2. Verser le **moût** au seau.
3. Clic avec du **houblon** (`#alcoholic:hops`). Ajout unique en début de process (rôle *bittering*).
4. Le liquide devient **moût houblonné** : amertume ≈ 0,55, arôme ≈ 0,40. Le sucre déjà présent est conservé.

### Étape E — Fermentation (80 ticks de cinétique)

1. Transférer le moût houblonné dans la **cuve de fermentation** (seau, clic, ou tuyau Create).
2. Clic avec de la **levure**.
3. Chaque tick : le sucre baisse, l’éthanol monte (`sugar_to_ethanol` 0,47). Le CO₂ est éventé, pas stocké.
4. Hors 18–24 °C : plus lent. Hors 10–30 °C : à l’arrêt. L’ambiance (~20 °C) suffit.
5. Quand le sucre passe sous 0,02, l’identité du batch devient `alcoholic:beer`.

La cuve n’est pas « une machine à bière » : elle exécute `alcoholic:ferment` sur n’importe quel liquide qui a une définition (ici `alcoholic:ferment_hopped_wort`).

### Étape F — Mise en bouteille

Clic droit sur le **fermenteur** (ou un contrôleur industriel de fermentation) avec une **bouteille vide**. Volume par défaut : **250 mB**. La bouteille est un *snapshot* (définition, éthanol, sucre, acidité, maturité, origine, qualité) — pas une mini-cuve. On ne peut pas relancer l’horloge de process en reverser.

On ne met en bouteille **que** depuis un fermenteur, un fût, une terrine ou un contrôleur industriel — pas depuis le pressoir, la cuve d’empâtage ni le chaudron. Le moût (grain ou raisin) et le moût houblonné **non fermentés** ne se mettent pas en bouteille.

`/alcoholic inspect` : vise la cuve ou tiens la bouteille.

---

## 5. Chaleur (bloc du dessous)

| Source | °C | Mash (62–68 / 52–78) | Boil (98–105 / 90–110) |
|---|---|---|---|
| Bloc de magma | 65 | Idéal | Trop froid |
| Fourneau / fumoir allumé | 80 | Hors bande opérable | Trop froid |
| Feu / feu des âmes | 95 | Trop chaud | Opérable |
| Feu de camp allumé | 100 | Trop chaud | Idéal |
| Lave / chaudron de lave | 100 | Trop chaud | Idéal |
| Feu de camp des âmes allumé | 95 | Trop chaud | Opérable |
| Haut fourneau allumé | 110 | Trop chaud | Limite haute |
| Rien (ambiance) | ~20 | Froid | Froid |

Un brûleur Create peut s’enregistrer comme sonde de chaleur ; ce n’est pas requis.

---

## 6. Ce qu’il ne faut pas faire

- **Pas de pressoir** : la bière ne passe pas par `PRESS`.
- **Pas de fût ni de terrine** : il n’y a pas de définition `AGE` officielle sur `alcoholic:beer`. `BLEND` n’est pas dans ce DAG. `CONDITION` est industriel uniquement et hors graphe.
- **Pas de multiblock industriel** pour cette chaîne : les contrôleurs 7B (malterie, broyeur à cylindres, cuve, chaudron, conditionnement) sont d’**autres exécuteurs** des mêmes process 7A, pas d’autres recettes. Ils ne sont pas nécessaires.
- **Ne pas coller le broyeur sans moteur** : il cale.
- **Ne pas mettre magma sous le chaudron** : 65 °C est trop bas pour l’ébullition.
- **Ne pas mettre un feu de camp sous la mash tun** : 100 °C sort de la bande 52–78 °C.
- **Ne pas bouteiller le moût** : seul un liquide déjà fermenté (ici `alcoholic:beer`) se met en bouteille, et seulement depuis fermenteur / fût / terrine / contrôleur industriel.

---

## 7. Propriétés du batch

La bière officielle porte : sucre, éthanol, amertume, couleur, arôme. Elles survivent au maltage → mouture → empâtage → ébullition → fermentation. Deux lots distincts ne fusionnent pas tout seuls dans un tank.

Il n’y a pas encore de gameplay boisson / ivresse / villageois.

## Hors scope

- Pas de boisson, d’ivresse, de tavernes ni de villageois.
- `DISTILL` et `INFUSE` sont des types enregistrés **sans** machine de jeu.
- Cidre, whisky, rhum et liqueur de fruit restent des **fixtures** de validation, pas du gameplay.
- La bière officielle n’a **pas** de nœud `AGE`. Le vin, lui, fourche vin jeune / vin élevé : [vigne artisanale](vigne-artisanale.md).
- Le DAG officiel `alcoholic:beer` utilise **`alcoholic:malt_pale`**. Les profils ambré et foncé restent jouables sur l’aire de maltage.

---

## 8. Kit debug (créatif / tests)

```
/alcoholic debug kit beer_agriculture
/alcoholic debug kit beer_artisanal
```

Le kit artisanal donne les machines, orge, malt, grist, houblon, levure, bouteilles, seaux d’eau, charbon, magma et feux de camp.
