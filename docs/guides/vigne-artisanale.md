# Guide : vigne artisanale (vin)

Guide joueur pour le DAG officiel du vin. Create, Crossroads et les multiblocks industriels sont **optionnels**. Le graphe livré est `PRESS` → `FERMENT` → `AGE`. `BLEND` est une capacité optionnelle de la terrine, **pas** un nœud du graphe. `BOTTLE` est une action de vaisseau, **pas** un nœud DAG.

Bière : voir [brasserie artisanale](brasserie-artisanale.md).

```text
raisin → pressage → moût
moût + levure → fermentation → vin jeune
vin jeune → bouteille
        ↘ seau → fût → AGE → vin → bouteille
assemblage (terrine) = optionnel, hors DAG
```

Sources wiki (collection `alcoholic-wiki`) : perennial-viticulture, trellis-training, harvest-lot-metadata, artisanal-processing, fermentation-physics, aging-process, blend-versus-tank-merge, bottled-beverage-snapshot, create-press-adapter, industrial-processing, README.

---

## 1. Ce qu’il faut poser

| Machine | Id | Rôle |
|---|---|---|
| Pressoir artisanal | `alcoholic:artisanal_press` | `PRESS` uniquement |
| Cuve de fermentation artisanale | `alcoholic:artisanal_fermenter` | `FERMENT` (la même que pour la bière) |
| Fût de chêne | `alcoholic:oak_barrel` | `AGE` — 8000 mB |
| Terrine d’assemblage | `alcoholic:artisanal_blending_crock` | `BLEND` optionnel, **hors DAG** — deux tanks 4000 mB |

Optionnel : sécateur, poteaux, bobine, pressoir / cuve industriels (autres exécuteurs, pas d’autres recettes), tuyaux Create.

Alcoholic **n’ajoute pas** d’arbres, d’engrenages ni de tuyaux. Les seaux suffisent. Create n’est **pas** requis.

Main vide (sans sneak) sur une machine **ouvre son écran**. Un item en main s’insère encore au clic ; les seaux se versent toujours sur le bloc.

### Disposition type

```text
  [vigne palissée]  →  [pressoir]  →  seau  →  [fermenteur]
                                                 │
                                    bouteille ←──┤
                                                 ↓ seau
                                              [fût]  →  AGE  →  bouteille

  [terrine]  =  assemblage optionnel, hors graphe
```

- Fermenteur à température ambiante (bande préférée 18–24 °C).
- Le fût n’élève que tant que le chunk est chargé.
- La terrine n’est pas sur le chemin officiel.

---

## 2. Recettes de craft

### Pressoir artisanal

```
  I
P B P
P P P
```

`I` = lingot de fer, `P` = planches, `B` = bol.

### Cuve de fermentation artisanale

```
P   P
P   P
P P P
```

`P` = planches.

### Fût de chêne

```
P   P
P I P
P P P
```

`P` = planches de chêne, `I` = lingot de fer.

### Terrine d’assemblage (optionnelle)

```
P   P
P B P
  P
```

`P` = planches, `B` = bol.

### Sécateur

```
  I
S I
```

`I` = lingot de fer, `S` = bâton.

### Levure (sans forme, ×2)

Champignon brun + sucre.

### Bouteilles vides (×4)

```
  G
G   G
  G
```

`G` = verre.

### Palissage

- **Poteau de vigne** ×2 : stick au-dessus de deux planches.
- **Poteau d’extrémité** ×2 : pépites de fer + deux bûches.
- **Bobine de fil** : pépites de fer + ficelle.

---

## 3. Cultures

Vigne pérenne (rouge ou blanche). On plante **une fois** ; la récolte ne casse pas le plant.

### Boutures

1. Trouver une **vigne sauvage** (`alcoholic:red_grapevine` / `alcoholic:white_grapevine` générée `harvest_ready`) dans les plaines, plaines de tournesols, forêts, forêts fleuries ou forêts de bouleaux.
2. Casser le bloc : 1 **bouture** (`alcoholic:red_grape_cutting` / `alcoholic:white_grape_cutting`).
3. Planter la bouture sur de la terre (`#minecraft:dirt`) ou de la terre labourée.
4. La poudre d’os avance **un stade** (souche, tige ou canopée). Marc et drêche au composteur vanilla en produisent. À `HARVEST_READY`, le clic vendange : la poudre d’os ne fait plus rien.

Si Vinery est chargé, le worldgen Alcoholic des vignes sauvages est coupé. Ses graines ne plantent une vigne Alcoholic que dans un rang **palissé**.

### Palissage

Une vigne peut pousser sans fil, mais moins bien (rendement ~70 %, qualité ~85 % versus 100 % palissée).

1. Placer deux poteaux (vigne ou extrémité) au même niveau, alignés.
2. Clic sur le premier poteau avec la bobine, puis sur le second → fil(s) de palissage.
3. Planter sous / le long du fil.

### Huit stades (premier cycle)

`PLANTED` → `ESTABLISHING` → `VEGETATIVE` → `FLOWERING` → `GREEN_FRUIT` → `RIPENING` → `HARVEST_READY` → `DORMANT`

Cycles suivants : `DORMANT` → `FLOWERING` → … → `HARVEST_READY` → `DORMANT`.

### Récolte (comportement cible)

À `HARVEST_READY`, un **clic droit n’importe quelle main** (vide ou non) récolte. La vigne passe en `DORMANT` ; elle n’est **pas** détruite. Le tas porte le NBT de lot (qualité, sucre, acidité, cépage).

**Shift + main vide** : inspecte le plant (stade, santé, taille, lot).

### Taille

Le **sécateur** ne taille qu’en `DORMANT`. C’est **optionnel** : ça ajuste la qualité, ce n’est **pas** requis pour relancer le cycle.

---

## 4. Chaîne de production

Un clic à main vide ouvre l’écran de la machine (le statut reste visible en sneak, ou dans l’écran). Les durées sont en ticks (20 ticks = 1 s).

### Étape A — Pressage (20 ticks)

1. Clic droit sur le pressoir avec des **raisins** (`#alcoholic:grapes/red` ou `#alcoholic:grapes/white`).
2. Attendre. Résultat : **moût** (`alcoholic:red_grape_must` / `alcoholic:white_grape_must`) + **marc**.
3. Le NBT de lot (sucre, acidité, qualité) est copié sur le moût par le process `PRESS` générique.
4. Shift + main vide : récupère le marc.
5. **Seau vide** pour extraire le moût. Il **faut** passer par le seau pour aller au fermenteur.

Create n’est pas requis. Un Mechanical Press Create produit du moût aux propriétés **par défaut** : le NBT de lot de récolte est perdu.

### Étape B — Fermentation (80 ticks de cinétique)

1. Verser le moût au seau dans la **cuve de fermentation**.
2. Clic avec de la **levure**.
3. Chaque tick : le sucre baisse, l’éthanol monte. Le CO₂ est éventé, pas stocké.
4. Hors 18–24 °C : plus lent. Hors 10–30 °C : à l’arrêt. L’ambiance (~20 °C) suffit.
5. Quand le sucre passe sous le seuil, l’identité du batch devient **vin jeune** (`alcoholic:young_red_wine` / `alcoholic:young_white_wine`).

La cuve n’est pas « une machine à vin » : elle exécute `alcoholic:ferment` sur n’importe quel liquide qui a une définition.

### Étape C — Fourche vin jeune / vin élevé

Deux sorties officielles, **pas** un second DAG :

1. **Bouteiller le vin jeune** depuis le fermenteur (bouteille vide, 250 mB).
2. **Élever** : seau de vin jeune → **fût de chêne** → `AGE` jusqu’à maturité ≥ 1,0 → le batch devient `alcoholic:red_wine` / `alcoholic:white_wine` → bouteiller depuis le fût.

Le fût saisonné (déjà utilisé) applique un multiplicateur 1,15. Vider le fût enregistre le liquide précédent.

### Étape D — Assemblage (optionnel, hors DAG)

La terrine exécute `alcoholic:blend` : deux tanks, **shift + main vide**. Ce n’est **pas** un nœud de `red_wine` / `white_wine`. Le graphe officiel s’arrête à `AGE`.

### Étape E — Mise en bouteille

Clic droit avec une **bouteille vide** sur un **fermenteur**, un **fût**, une **terrine**, ou un **contrôleur industriel**. Volume par défaut : **250 mB**. Snapshot (définition, éthanol, sucre, acidité, maturité, origine, qualité) — pas une mini-cuve.

On **ne** bouteille **pas** depuis le pressoir, la cuve d’empâtage ni le chaudron. Le moût et le moût de grain **non fermentés** ne se mettent pas en bouteille.

`/alcoholic inspect` : vise le vaisseau ou tiens la bouteille / le seau.

---

## 5. Industriels et Create

Les contrôleurs industriels (pressoir, cuve de fermentation, tank passif) sont des **exécuteurs supplémentaires** des mêmes process. Ils ne changent pas le DAG.

Create reste optionnel (Mechanical Press, tuyaux). Le pressoir Alcoholic conserve le lot de récolte ; le compactage Create ne le conserve pas.

---

## 6. Ce qu’il ne faut pas faire

- **Ne pas sauter le seau** : le pressoir ne nourrit pas le fermenteur tout seul sur le chemin artisanal.
- **Ne pas traiter `BLEND` comme une étape obligatoire** : hors graphe.
- **Ne pas bouteiller le moût** depuis le pressoir.
- **Ne pas exiger Create** pour progresser.
- **Ne pas tailler hors `DORMANT`** : le sécateur refuse. La taille n’est pas requise pour le cycle.
- **Ne pas confondre avec la bière** : pas de `MALT` / `MILL` / `MASH` / `BOIL` ici. La bière n’a pas de `AGE` officiel : [brasserie artisanale](brasserie-artisanale.md).

---

## 7. Propriétés du batch

Le vin officiel porte : sucre, éthanol, acidité, maturité, qualité, provenance. Elles survivent au pressage → fermentation → élevage. Deux lots distincts ne fusionnent pas tout seuls dans un tank.

Il n’y a pas encore de gameplay boisson / ivresse / villageois.

---

## Hors scope

- Pas de boisson, d’ivresse, de tavernes ni de villageois.
- `DISTILL` et `INFUSE` sont des types enregistrés **sans** machine de jeu.
- Cidre, whisky, rhum et liqueur de fruit restent des **fixtures** de validation, pas du gameplay.
- Pas de cidre jouable (pommes → `PRESS` → `FERMENT` existe seulement en `testpack:`).
- La bière officielle n’a **pas** de nœud `AGE` ; `CONDITION` est industriel uniquement et hors DAG.

---

## 8. Kit debug (créatif / tests)

```
/alcoholic debug kit wine_agriculture
/alcoholic debug kit wine_artisanal
```

Le kit agriculture donne boutures, poteaux, bobine, sécateur, terre et poudre d’os. Le kit artisanal donne pressoir, fermenteurs, fûts, terrine, raisins, levure, bouteilles et seaux de moût / vin jeune.
