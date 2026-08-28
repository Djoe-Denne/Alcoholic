# Industrial press (formed) textures

`master-512` is the approved formed-overview hull atlas (copy of
`industrial_casing`). `SHA256SUMS.txt` sits next to it. This id has **no
game block** and **no** resource-pack / 64×64 downsample.
Mega-mesh = overlay BER `models/block/formed/industrial_press.json` at art size 3×4×3 only.
It is not a fourth block. Any formed size uses the 9-slice hull; fittings stay 1×1. The iron platen stays a BER overlay.

Typical formed size: 3×4×3, pressure-safe casing, kinetic port required.
A vertical iron platen is visible through the windows (art only; the game BER
still uses `Blocks.IRON_BLOCK`).

## Brief modelage (agent Blockbench)

- **Id / type** : `industrial_press` — **vue d’ensemble formée**. Ce n’est **pas** un bloc posable. Overlay BER à 3×4×3 ; sinon hull 9-slice + contrôleur. Le platen fer reste un overlay.
- **Refs** : `art/blockbench/industrial_press/reference.png` et `industrial_press_formed.png`.
- **Kit hull** : cuboïde creux 3×4×3, gros bloc `industrial_casing` (`pressure_safe_casing`). Face −Z : `item_port` bas centre (raisin) ; `industrial_press_controller` au-dessus ; `fluid_port` mi-hauteur à droite (moût) ; `access_hatch` centre ; 2× `machine_window` (platen fer = tile IRON du casing, art only). **`kinetic_port` sur +X** (obligatoire). BER jeu inchangée.
- **Contraintes raid** : cuboïde **creux** fermé, hublots ronds, 1 contrôleur acier, port cinétique obligatoire. Typique art 3×4×3 ; code min 3×4×3 max 7×8×7 (`BuiltinMachines.industrialPress()`).
- **Face utile** : −Z (contrôleur). Java Block/Item. MCP Blockbench **seul**. Grey silhouette des **cubes unitaires**, pas d’un titan. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`.
- **Validation** : on peut poser les cubes en scène Blockbench pour coller à la planche ; on n’exporte pas cette scène comme modèle runtime.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Bloc formé `industrial_press` | **aucun** — id datapack / définition seulement |
| Contrôleur Block | `MultiblockControllerBlock` + `AlcoholicIds.INDUSTRIAL_PRESS_CONTROLLER` |
| Contrôleur BE | `MultiblockControllerBlockEntity` ; type `alcoholic:industrial_press_controller` |
| Properties | `FORMED` sur le contrôleur |
| BER formé | `FormedMultiblockRenderer` — hull 9-slice ; mega-mesh à 3×4×3 ; platen = `Blocks.IRON_BLOCK` via `strokeCycle()` |
| Enregistrement BER | `FormedMultiblockClient` → toutes les BE contrôleur |
| Définition | `application/…/BuiltinMachines.INDUSTRIAL_PRESS` ; JSON `data/alcoholic/alcoholic/machines/industrial_press.json` |
| Former | `MultiblockControllerBlockEntity.revalidate()` + `domain/…/HollowCuboidValidator.java` + `WorldStructureSampler` |
| Pose debug | `HollowCuboidPlacer` / `BeerLinePlacer` |
| Casing | tag `alcoholic:pressure_safe_casing` |

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FORMED` + atlas contrôleur `_formed` ; BER platen fer (placeholder, pas le mesh art).
- **Art sans Java fidèle** : platen custom de la planche ≠ `IRON_BLOCK`.
- **Si platen art** : éditer `IndustrialPressRenderer` (modèle JSON / cube custom), **pas** un bloc mega-mesh. Flywheel ailleurs = autre machine.

## Mini-prompt copiable

```
NE sculpte PAS un mega-mesh industrial_press comme bloc jeu (id inexistant).
Vue d’ensemble seulement : reference.png + industrial_press_formed.png.
Modèle jeu = cubes hull + industrial_press_controller. Cuboïde creux ~3×4×3, kinetic obligatoire.
Hublots ronds, 1 contrôleur, face −Z. MCP Blockbench seul, une machine/cube à la fois.
Skill alcoholic-java-machine-model. Grey → UV → paint 512 → stop user.
Java : MultiblockControllerBlockEntity + HollowCuboidValidator + IndustrialPressRenderer (platen).
Interdit process/recipes / 4e pipeline. Platen custom = BER, pas un titan.
```
