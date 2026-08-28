# Industrial malt house textures

`master-512` is the approved formed-overview hull atlas (copy of
`industrial_casing`). `SHA256SUMS.txt` sits next to it. This id has **no
game block** and **no** resource-pack / 64×64 downsample.
Do not invent a mega-mesh `models/block/industrial_malt_house.json`.

This formed multiblock is painted industrial steel (grey-blue casing, windows,
hatch, ports). It does not use the locked oak tile — that belongs to
`malting_floor`.

## Brief modelage (agent Blockbench)

- **Id / type** : `industrial_malt_house` — **vue d’ensemble formée**. Ce n’est **pas** le modèle jeu. Le jeu assemble des cubes + `industrial_malt_house_controller`. **Interdit** : mega-mesh unique. Ce n’est **pas** `malting_floor`.
- **Refs** : `art/blockbench/industrial_malt_house/reference.png`.
- **Kit hull** : cuboïde creux 5×4×5, gros bloc `industrial_casing` (`fermenter_casing`). Face −Z : `item_port` bas centre ; `industrial_malt_house_controller` au-dessus ; `fluid_port` mi-hauteur à droite (x 0–16) ; `access_hatch` centre ; 2× `machine_window` en haut (grain = tile FACE du contrôleur, pas un nouvel ambre). Cheminées toit = art only. **Pas** de `kinetic_port` (`required_ports: []`).
- **Contraintes raid** : cuboïde creux. Typique art ~5×4×5 ; code min 3×4×3 max 7×8×7. Casing `fermenter_casing`. Grain peint sobre derrière un hublot.
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`. Grey → UV → paint 512 → **stop user**.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Bloc formé | **aucun** |
| Contrôleur | `MultiblockControllerBlock` / `MultiblockControllerBlockEntity` ; id `alcoholic:industrial_malt_house_controller` |
| Properties | `FORMED` (blockstate ignore) |
| BER formé | **aucune** |
| Définition | `BuiltinMachines.INDUSTRIAL_MALT_HOUSE` ; JSON `…/machines/industrial_malt_house.json` |
| Former | `revalidate()` + `HollowCuboidValidator` + `WorldStructureSampler` |

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FORMED` contrôleur ; process malt côté stratégie (`IndustrialProcessStrategy`), pas un look.
- **Art sans Java** : plancher perforé / couche de grain / cheminée — pas de BER.
- **Si grain / touraille animés** : BER sur `maltHouseControllerEntity` (pattern press) **ou** `LIT` sur le contrôleur (pattern moteur). Pas de mega-mesh.

## Mini-prompt copiable

```
NE sculpte PAS un mega-mesh industrial_malt_house comme bloc jeu.
Vue d’ensemble : industrial_malt_house/reference.png.
Modèle jeu = hull + industrial_malt_house_controller. Cuboïde creux ~5×4×5.
Pas le malting_floor bois. Hublots ronds, 1 contrôleur ambre, face −Z.
MCP Blockbench seul. Skill alcoholic-java-machine-model.
Grey → UV → paint 512 → stop user.
Java : MultiblockControllerBlockEntity + HollowCuboidValidator. Pas de BER.
Grain/touraille animés = BER ou LIT. Interdit process / 4e pipeline.
```
