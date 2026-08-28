# Industrial roller mill (formed) textures

`master-512` is the approved formed-overview hull atlas (copy of
`industrial_casing`). `SHA256SUMS.txt` sits next to it. This id has **no
game block** and **no** resource-pack / 64×64 downsample.
Do not invent a mega-mesh `models/block/industrial_roller_mill.json`.

This formed multiblock is painted industrial steel. Kinetic is required.

## Brief modelage (agent Blockbench)

- **Id / type** : `industrial_roller_mill` — **vue d’ensemble formée**. Ce n’est **pas** le modèle jeu. Le jeu assemble des cubes 1×1 + `industrial_roller_mill_controller`. **Interdit** : mega-mesh unique comme bloc (id bloc inexistant).
- **Refs** : `art/blockbench/industrial_roller_mill/reference.png`.
- **Kit hull** : cuboïde creux 3×4×3, gros bloc `industrial_casing` (`pressure_safe_casing`). Face −Z : `item_port` bas centre ; `industrial_roller_mill_controller` au-dessus ; `fluid_port` mi-hauteur à droite ; `access_hatch` centre ; 2× `machine_window` (2 cylindres = tile FACE du contrôleur). **`kinetic_port` sur +X** (arbre vert, obligatoire). Pas de BER.
- **Contraintes raid** : cuboïde creux fermé. Typique art 3×4×3 ; code min 3×4×3 max 5×6×5. Casing `pressure_safe`. À travers les hublots : deux cylindres (guide visuel seulement).
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`. Grey silhouette des cubes unitaires → UV → paint 512 → **stop user**.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Bloc formé | **aucun** |
| Contrôleur | `MultiblockControllerBlock` / `MultiblockControllerBlockEntity` ; id `alcoholic:industrial_roller_mill_controller` |
| Properties | `FORMED` + atlas `_formed` sur le contrôleur |
| BER formé | **aucune** — pas d’équivalent à `IndustrialPressRenderer` |
| Définition | `BuiltinMachines.INDUSTRIAL_ROLLER_MILL` ; JSON `…/machines/industrial_roller_mill.json` |
| Former | `revalidate()` + `HollowCuboidValidator` + `WorldStructureSampler` |
| Ports | `PartRole.KINETIC_PORT` obligatoire |

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FORMED` + texture contrôleur `_formed`.
- **Art sans Java** : cylindres / meules dans les hublots — **pas** de BER.
- **Si rollers animés** : BER sur `rollerMillControllerEntity` (copier `IndustrialPressRenderer` / `MaltMillRenderer`). Ne pas créer un bloc `industrial_roller_mill`.

## Mini-prompt copiable

```
NE sculpte PAS un mega-mesh industrial_roller_mill comme bloc jeu.
Vue d’ensemble : art/blockbench/industrial_roller_mill/reference.png.
Modèle jeu = hull + industrial_roller_mill_controller. ~3×4×3, kinetic obligatoire.
Hublots ronds, 1 contrôleur, face −Z. MCP Blockbench seul.
Skill alcoholic-java-machine-model. Grey → UV → paint 512 → stop user.
Java : MultiblockControllerBlockEntity + HollowCuboidValidator. Pas de BER aujourd’hui.
Cylindres animés = BER pattern press/mill. Interdit process / 4e pipeline.
```
