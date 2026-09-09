# Audit fonctionnel et jouabilité — Alcoholic

- **Remédiation C1–C15** : 2026-09-09. Recettes AGE/CONDITION distinctes, mash 2 tanks, FTB `form_aging`, T° `EnvironmentSampler`, malt humidité, `speedModifier` unique, fluid port sans reset, press 5000 mB/cellule, moteur FE 128, chaleur sous l’intérieur du hull, faucille + docs, harvest tags Vinery/Brewery, Create mill/press `process_completed`, JEI craft + `bottle.json`. **Hors scope** : C16–C26.

- **HEAD** : `23908fb` (2026-09-08, *Enhance ExecutorModifiers and add new block models for bottle storage*)
- **Branche** : `main`
- **Profils** : Forge 1.19.2 autonome et pack CurseForge *Create 2 Mekanism*
- **Cutoff** : tout le HEAD, bouteillage et vieillissement industriel **inclus**
- **Règle de ce passage** : cartographier, mesurer, reproduire. Aucune règle de gameplay n’a été changée.

Sources d’analyse : MCP projet `.cursor/mcp.json` (`CONTEXT_MODE_PROJECT_DIR=C:\Users\djden\source\repos\Alcoholic`), GrepAI (scan du 2026-09-09, 401 fichiers / 2651 symboles), QMD `alcoholic-wiki` (180 md inchangés — **wiki en retard sur le HEAD**), Serena/LSP sur le HEAD.

---

## Verdict

**C1–C15 corrigés (2026-09-09).** Les DAG vin et bière jusqu’à la bouteille, le mash à deux tanks, les recettes AGE/CONDITION distinctes, l’ambiance fermentateur, JEI `bottle.json` et les catalyseurs craft sont alignés.

**Hors scope de cette remédiation : C16–C26** (casiers sans recette, graphe beer `finished`/`young`, parent vanilla AGE, purity `beer`, crash `BottleStandNetwork` / GameTest casiers).

---

## 1. Cartographie fonctionnelle

### 1.1 Processus

| ID | Runtime | JSON généré | DAG officiel |
|---|---|---|---|
| `press` | oui | `press_red/white_grapes` (200 t, 8 raisins → 1000 mB) | vin |
| `ferment` | oui | must rouge/blanc, hopped wort (24000 t, 18–24 °C, levure) | vin + bière |
| `age` | oui | young red/white (72000 t, 10–16 °C) | vin |
| `blend` | oui | red/white, **hors DAG**, hidden vanilla | — |
| `bottle` | oui | `processes/bottle.json` (250 mB) | nœud progression |
| `malt` | oui | pale/amber/dark (12000 t, humidité ≥ 0,4) | pale seulement |
| `mill` | oui | 200 t, tag `malted_grain` → grist | bière |
| `mash` | oui | 1200 t, 1 grist + 1000 eau → 1000 wort, 62–68 °C | bière |
| `boil` | oui | 1600 t, 98–105 °C, houblon | bière |
| `condition` | oui, **industriel seulement** | 12000 t, maturité 0,85, 2–12 °C | hors DAG |
| `distill`, `infuse` | stub `ProcessResult.unsupported` | aucun | aucun |

Fixtures data-only (non jouables) : cidre, whisky, rhum, liqueur, wheat-beer testpack.

Graphe `beer.json` : le nœud ferment publie le port `finished` alors que `ferment_hopped_wort.json` publie `young`. Les machines n’utilisent pas ce graphe pour exécuter ; JEI/qualité peuvent diverger.

### 1.2 DAG vin

```
raisins (#alcoholic:grapes/red|white)
  → PRESS (8 → 1000 mB must, 200 t)
  → FERMENT (levure, 24000 t) → young_*_wine
      ↳ BOTTLE 250 mB (autorisé)
      ↳ AGE (72000 t) → red/white_wine → BOTTLE
      ↳ BLEND (crock 2×4000, optionnel)
```

Exécuteurs : pressoir 2000 mB, fermentateur 2000 mB, fût 4000 mB, pressoir / vat / aging industriels. **Pas** de press/age/blend craft.

### 1.3 DAG bière

```
orge → MALT pale (12000 t)
  → MILL (200 t) → grist
  → MASH (1200 t) → wort
  → BOIL + houblon (1600 t) → hopped_wort
  → FERMENT (24000 t) → beer → BOTTLE
      ↳ CONDITION industriel optionnel (12000 t)
```

Amber/dark : cycle sneak sur l’aire de maltage, **hors graphe officiel**. CONDITION n’a pas d’exécuteur artisanal.

### 1.4 Machines

**Artisanal (1 bloc)**

| Machine | Process | Tank | Slots | Layout |
|---|---|---|---|---|
| Pressoir | PRESS | 2000 | 1 in / 1 out | TWO_SLOTS_ONE_TANK |
| Fermentateur | FERMENT | 2000 | 1 | ONE_SLOT_ONE_TANK |
| Fût chêne | AGE | 4000 | 0 | — |
| Terrine | BLEND | 2×4000 | 0 | TWO_TANKS |
| Aire de maltage | MALT | — | 1, batch 64 | — |
| Moulin | MILL | — | 2 | TWO_SLOTS |
| Cuve d’empâtage | MASH | **2×2000** | 2 | TWO_SLOTS_TWO_TANKS |
| Chaudron | BOIL | 2000 | 2 | TWO_SLOTS_ONE_TANK |
| Casier / étagère | stockage bouteilles | — | 9 cellules | **aucune recette** |

**Craft (bière, hors `BuiltinMachines` / hors test FORMED)** — hull 3³–5³, `craft_casing`

| Machine | mB min–max | speed | fidélité | batch | tanks GUI |
|---|---|---|---|---|---|
| Malt house | 1000–27000 | 1,25 | 0,94 | 384 | 0 (12 slots) |
| Mill | 1000–27000 | 2,0 | 0,94 | 8 | 0 |
| Mash tun | 2000–54000 | 1,25 | 0,94 | 8 | **1** |
| Kettle | 2000–54000 | 1,25 | 0,94 | 1 | 1 |
| Vat | 2000–54000 | 1,25 | 0,94 | 1 | 1 |

**Industriel** — hull min 3×4×3 (intérieur 2), casings dédiés

| Machine | max | mB min–max | speed | fidélité | floor | batch | cinétique |
|---|---|---|---|---|---|---|---|
| Press | 7×8×7 | 8000–600000 | 2,0 | 0,70 | 0,15 | MAX | load 1, 16–256 |
| Vat | 9×16×9 | 16000–5 488 000 | 1,0 | 0,70 | 0,12 | 1 | non |
| Tank | 9×16×9 | 32000–10 976 000 | — | — | — | — | non |
| Malt house | 7×8×7 | 4000–300000 | 2,0 | 0,70 | 0,15 | MAX | non |
| Roller mill | 5×6×5 | 2000–36000 | 4,0 | 0,70 | 0,15 | MAX | **load 4**, 16–256 |
| Mash tun | 9×12×9 | 16000–3 920 000 | 1,5 | 0,70 | 0,12 | MAX | non |
| Kettle | 7×8×7 | 12000–900000 | 1,5 | 0,70 | 0,12 | 1 | non |
| Conditioning | 7×10×7 | 16000–1 600 000 | 1,0 | 0,70 | 0,15 | 1 | non |
| Aging | 7×10×7 | 16000–1 600 000 | 1,0 | 0,70 | 0,15 | 1 | non |

`MachineLayout.forMultiblock(MASH)` = `TWO_SLOTS_ONE_TANK` aux deux échelles. L’artisanal est le seul mash à deux tanks.

Chaleur : **uniquement le bloc sous le contrôleur** (`HeatSources`). Magma 65 °C (mash OK, boil stall). Campfire/lave ~100 °C (boil OK). Four allumé 80 °C (mash OK, boil stall). Kiln malt industriel ≥ 40 °C.

### 1.5 Progression, JEI, docs

Artisanal : `root` → vendange vin **ou** orge/houblon → press / malt→mill→mash→boil → `ferment_beverage` (OR) → `age_wine` | `blend` hidden | `bottle`.

Industriel : `industrial_root` (tous les contrôleurs en inventaire) → press | tank | malt→mill→mash→kettle → vat (OR) → conditioning | aging (OR FTB, parent vanilla = `industrial_root`).

JEI catalyseurs : artisanal + industriel. **Aucun** contrôleur craft. `BOTTLE` catalyseur = bouteille vide. Sans JSON process, la catégorie Bottling est vide.

FTB image `form_aging` est dans le catalogue, **pas** dans `INDUSTRIAL_FLIPBOOKS`, **pas** de PNG.

Guides `docs/guides/vigne-artisanale.md` / `brasserie-artisanale.md` : durées 20/40/80 t (runtime 200/1200/24000), « clic n’importe quelle main » (faucille obligatoire), « cave industrielle AGE absente » (vaisseau AGE shippé).

Worldgen Alcoholic vignes/orge/houblon **coupé** si Vinery/Brewery. Advancements harvest ciblent les IDs Alcoholic.

Create millstone/crushing/compacting : recettes XOR, **pas** de `alcoholic:process_completed`.

Loot des 14 contrôleurs craft/industriels : `"pools": []`. `onRemove` fait `popResource` du contrôleur item — casser le bloc le drop via le code, pas via la loot table. Si un autre outil casse sans passer par `onRemove` custom, le contrôleur est perdu. À classer P2 (code path actuel droppe l’item).

---

## 2. Mesures d’équilibrage

### 2.1 Temps de cycle (20 tps, sans stall)

| Étape | JSON (t) | Artisanal réel | Craft/indus (speed appliqué 2× sur mill/mash/boil/press) |
|---|---|---|---|
| PRESS | 200 | ~10 s (GameTest t=210) | indus speed 2 → job 100 t puis clock×2 ≈ **~30 t** (test mill analogue t=30) |
| MALT | 12000 | 10 min (rate T° seulement) | speed 2 **une fois** |
| MILL | 200 | ~10 s | indus 4×4 → GameTest **t=30** |
| MASH | 1200 | GameTest t=1210 | craft 1,25² ≈ 768 t (test 970) ; indus 1,5² ≈ 533 t (test **610**) |
| BOIL | 1600 | ~80 s | speed 1,5 deux fois ; test chaudron t=810 |
| FERMENT | 24000 | 20 min | speed 1,0 vat / 1,25 craft |
| AGE | 72000 | 60 min | speed 1,0 |
| CONDITION | 12000 | n/a | speed 1,0 |
| BOTTLE | instantané | 250 mB | idem |

`speedModifier` est **déjà** divisé dans `duration` **et** réappliqué dans `advanceElapsed` pour mill, mash, boil, press. Malt / ferment / age / condition : une seule fois. JEI affiche le JSON, donc **menteur** sur mill/mash/boil/press craft et industriel.

### 2.2 Volume et ressources

- 1 L de vin jeune : 8 raisins + 1 levure + 20 min. 4 bouteilles.
- 1 L de vin élevé : + 60 min fût (4000 mB = 16 bouteilles / cycle fût).
- 1 L de bière : 1 orge + 1 houblon + 1 levure + 1000 mB d’eau. Maltage 10 min domine avant fermentation.
- Seau vanilla 1000 mB ; bouteille 250 mB. Min pressoir industriel 8000 mB = 8 seaux. Min mash industriel 16000 mB.
- Pressoir min 8000 mB vs 64 raisins × yield machine 1,05 = **8400 mB** → hold / overflow sur hull minimale.
- Qualité : artisanal 1,00 / 1,00 / 0 ; craft 0,94 / 0,82 / 0,04 ; industriel 0,70 / 0,55 / 0,12–0,15. L’industriel est **dominé en qualité**, gagne en débit et volume. Pas d’upgrade sans effet : CONDITION sur bière déjà à sucre 0 n’ajoute pas de carbonatation utile (formule `0,35 × residual_sugar`).

### 2.3 Énergie

`ElectricMotorSettings.DEFAULT` : buffer 8000, `maxReceive=80`, `feForLoad(4)=100`. Un roller mill (load 4) **draine 20 FE/t** si l’entrée câble est 80. Le GameTest précharge 40×80 FE puis finit en 30 t (3000 FE) — burst OK, régime continu non soutenable. Le moteur artisanal (load 1 → 25 FE) est soutenable. Le moteur primitif **entraîne** le roller mill (GameTest vert).

---

## 3. Constats (P0–P3)

Légende preuve : **S** statique · **A** automatisée · **J** jeu · **H** hypothèse.

| ID | Sévérité | Preuve | Impact joueur |
|---|---|---|---|
| C1 | **P0** | **corrigé** | CONDITION utilise `brewing_kettle` ; AGE garde `oak_barrel` (vanilla + Create). |
| C2 | **P0** | **corrigé** | Mash craft/indus : 2 tanks (eau / wort). Leftover 1500 mB produit du wort. |
| C3 | **P0** | **corrigé** | `form_aging.png` + `.mcmeta` ; contrat FTB inclut l’ID. |
| C4 | **P0** | **corrigé** | Fermentateur et ambiance multiblock passent par `EnvironmentSampler`. |
| C5 | **P0** | **corrigé** | Steeping industriel utilise `max(sampled, requirement)` ; voisin eau +0,4 sur l’échantillonneur. |
| C6 | **P1** | **corrigé** | `speedModifier` appliqué une fois (durée déjà divisée). |
| C7 | **P1** | **corrigé** | Fluid port → `onProcessTankChanged()` (plus de reset à chaque fill). |
| C8 | **P1** | **corrigé** | Pressoir `capacity_per_internal_block` 5000 (min hull 10000 mB). |
| C9 | **P1** | **corrigé** | `maxReceivePerTick` 128 ≥ `feForLoad(4)` 100. |
| C10 | **P1** | **corrigé** | Chaleur = max sous le contrôleur et sous le plancher intérieur. Magma reste 65 °C. |
| C11 | **P1** | **corrigé** | Faucille obligatoire + message `need_sickle`. GameTests empty-hand conservés. |
| C12 | **P1** | **corrigé** | Guides, grimoire ch8, hover FTB aging alignés (durées JSON, cave AGE, faucille). |
| C13 | **P1** | **corrigé** | Pickup d’items `#alcoholic:grapes/*` / `#alcoholic:hops` déclenche `crop_harvested`. |
| C14 | **P1** | **corrigé** | Create millstone/crushing/basin compacting fire `process_completed`. |
| C15 | **P1** | **corrigé** | JEI catalyseurs craft + moteur ; `processes/bottle.json` 250 mB. |
| C16 | **P2** | S | Casier / étagère à bouteilles : blocs + GameTests, **aucune recette**. |
| C17 | **P2** | S | `beer.json` port `finished` vs process `young`. |
| C18 | **P2** | S | Vanilla `form_industrial_aging` parent = `industrial_root` ; FTB parents vat **ou** conditioning. |
| C19 | **P2** | S | Craft hors `ProgressionCoverageTest` FORMED. CONDITION couvert seulement par FORMED, pas par process_completed. |
| C20 | **P2** | S | DISTILL/INFUSE stubs ; `spirit.json` qualité orpheline ; marc/drêche = compost. |
| C21 | **P2** | S | Blend young+young → vin « fini » sans fût (capacité blend, hors DAG). |
| C22 | **P2** | A | `check` HEAD **rouge** : `checkBeverageFrameworkPurity` refuse le mot `beer` dans le commentaire de `BuiltinCraftMachines.java:18`. |
| C23 | **P3** | S | Datagen `runDataCommon` réordonne seulement les clés lang + newline. Pas de dérive sémantique. |
| C24 | **P3** | S | GUI mill/malt : tanks fantômes possibles selon layout TWO_SLOTS vs process sans liquide. Slots morts ferment/age/boil selon layout 2 slots. |
| C25 | **P1** | S | Malt industriel : steeping exige l’humidité réelle ; kiln ≥ 40 °C sous le contrôleur. Un hull « formé » ne malt pas en biome sec sans eau/chaleur. |
| C26 | **P0** | A | `BottleStandNetwork.find` fait `List.of(origin).sort(...)` si le rectangle dépasse 3×2. Quatre casiers alignés **crashent** (`UnsupportedOperationException`). GameTest `differentStandStyleAndOversizedGridDoNotJoin`. |

Tests de caractérisation inversés avec C1–C15 :

- `PlayabilityAuditCharacterizationTest` — recettes distinctes, `form_aging` présent, `bottle.json` présent
- `CraftGameTests.leftoverWaterStillProducesWort` — C2
- `ElectricMotorSettingsTest.rollerMillLoadFitsDefaultMotorIntake` — C9
- `MachineLayoutTest` — mash craft = 2 tanks

`LiquidBatchMergeTest.leftoverMashWaterCannotAcceptWort` reste : le merge refuse toujours eau≠wort ; le mash n’essaie plus de merger.

---

## 4. Vérification automatique

| Commande | Résultat |
|---|---|
| `:platform-forge-1.19.2:runDataCommon` | **SUCCÈS** (2026-09-09). Recettes CONDITION `brewing_kettle`, `processes/bottle.json`, lang `need_sickle`, press 5000 mB/cellule. |
| `test` (modules touchés) | **SUCCÈS** — `PlayabilityAuditCharacterizationTest` inversé (C1/C3/C15), `MachineLayoutTest` mash 2 tanks, moteur FE 128. |
| `runGameTestServer` | **1 échec autorisé C26** : `differentstandstyleandoversizedgriddonotjoin`. Leftover mash, mill/press moteur FE, mash 2 tanks : **OK**. |
| `runGameTestServer -PwithCreate=true` | **Même 1 échec C26**. Pas d’échec supplémentaire Create. |

Couverture GameTest déjà shippée (chemin heureux) :

- Vin artisanal : press, ferment progressif, fût → bouteille, faucille, palissage.
- Bière artisanal : malt, mill cinétique, mash 2 tanks @ 1000 mB, boil campfire, ferment.
- Industriel : form, press, vat → bouteille young, aging imprint, mash @ 1000 mB, kettle, mill, condition, ports, save/reload.
- Craft : form 3³/5³, mash @ 1000 mB, transfert wort → kettle indus.
- Create : skip si mod absent ; fill tank Create quand présent.
- Trous restants hors C1–C15 : casiers (C16/C26) ; graphe beer `finished`/`young` (C17).

---

## 5. Parcours joueur

Un parcours n’est « validé » que si craftable, découvrable et exécutable **sans** `/alcoholic debug kit`. Les kits ne servent qu’à isoler.

### 5.1 Forge autonome (preuve GameTest = partie automatisable)

| Parcours | Acquisition | Craft | Exécution | Découverte | Verdict |
|---|---|---|---|---|---|
| Vin artisanal → young bottle | worldgen vignes (sans Vinery) | pressoir, faucille, levure, bouteille | press 8, seau, levure, 20 min, bottle | JEI press + bottle.json ; message serpe | **jouable** |
| Vin artisanal → aged bottle | idem + fût | fût | 60 min cave | guide / durées JSON alignés | **jouable** (T° sampler) |
| Vin industriel → young bottle | contrôleurs craftables | hull 3×4×3 + cinétique press | GameTest `industrialPressMustFermentsInVatThenBottlesYoungWine` | JEI form | **jouable** |
| Vin industriel → AGE | contrôleur AGE (fût) | hull fermenter_casing | imprint GameTest | FTB `form_aging` ; guide cave AGE | **jouable** |
| Bière artisanal → bottle | orge/houblon worldgen (sans Brewery) | ligne complète | mash 2 tanks OK | durées guide JSON | **jouable** (humidité eau voisine) |
| Bière craft mash | contrôleur mash craftable | 3³ casing, 2 tanks | leftover 1500 mB → wort | catalyseurs JEI craft | **jouable** |
| Bière industriel CONDITION | contrôleur chaudron (distinct d’AGE) | hull | GameTest optionnel @ sucre résiduel | — | **jouable** (craft) |
| Create mill/press | recettes XOR | millstone | items + `process_completed` | JEI Create | **jouable** |

Checklist UX client (non pilotable ici ; à rejouer à la main après deploy) :

1. Grille 3×3 : fût + casing + fer → quel contrôleur sort (C1).
2. JEI Bottling vide ; catalyseurs craft absents.
3. FTB chapitre industriel : icône aging manquante.
4. Plaines : fermentateur 25 °C, mash magma, boil magma (stall).
5. Create pipe → mash 1500 mB → deadlock.
6. Casier à bouteilles introuvable en survie ; **quatre casiers alignés crashent** (C26).
7. Save/reload fût et vat (déjà GameTest).

### 5.2 Create 2 Mekanism

Jar remappé déployé :

`C:\Users\djden\curseforge\minecraft\Instances\Create 2 Mekanism\mods\alcoholic-forge-1.19.2-0.1.0-SNAPSHOT.jar` (2 707 797 octets).

Relancer le client (F3+T ne recharge pas le jar). Pack 128 non retouché (pas de changement de textures joueur dans cet audit).

Le chemin Create réutilise les mêmes DAG ; les tuyaux rendent C2 **plus probable** (transferts partiels). Les recettes Create des contrôleurs **héritent de C1**. GameTests `-PwithCreate=true` : transferts tank Create non régressés ; seul C26 échoue.

Checklist UX client restante (à rejouer à la main après relance) : C1 grille 3×3, JEI Bottling, FTB `form_aging`, mash 1500 mB via pipe, quatre casiers alignés (C26 crash).

---

## 6. Backlog recommandé (ordre)

Ne pas mixer avec cet audit. Plan de remédiation séparé.

1. **P0 C1** — patterns craft AGE ≠ CONDITION (vanilla + Create).
2. **P0 C2** — mash craft/indus : 2 tanks **ou** vidange/remplacement atomique du tank (eau → wort) **ou** refus d’entrée si volume % 1000 ≠ 0 avec message.
3. **P0 C26** — ne plus appeler `sort` sur `List.of` ; isoler le casier hors rectangle.
4. **P0 C3** — PNG `form_aging` + l’ajouter à `INDUSTRIAL_FLIPBOOKS`.
5. **P0 C4/C5** — fermentateur / malt : même `EnvironmentSampler` que le fût ; feedback GUI si stall T°/humidité.
5. **P1 C6** — une seule application de `speedModifier` ; aligner JEI.
6. **P1 C7** — ne pas `resetProcess` sur fill port (ou reset seulement si le liquide change d’identité).
7. **P1 C11/C12** — docs + grimoire : faucille, durées JSON, cave AGE.
8. **P1 C13/C14** — harvest Vinery/Brewery et `process_completed` Create, ou quêtes OR sur tags.
9. **P1 C9/C8/C10/C15** — moteur FE, press min hold, chaleur, JEI craft.
10. **P2** — recettes casier, graph beer port, parent vanilla aging, purity comment, stubs.

---

## 7. Ce que cet audit n’a pas changé

Aucune recette, durée, modificateur, layout ou loot n’a été corrigé. Seuls des tests de caractérisation et ce rapport / le canvas ont été ajoutés.
