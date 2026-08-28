# Industrial mash tun textures

`master-512` is the approved formed-overview hull atlas (copy of
`industrial_casing`). `SHA256SUMS.txt` sits next to it. This id has **no
game block** and **no** resource-pack / 64×64 downsample.
Do not invent a mega-mesh `models/block/industrial_mash_tun.json`.

This formed multiblock is painted industrial steel plus a copper note copied
from `industrial_mash_tun_controller`. It is not the oak `mash_tun`.

## Brief modelage (agent Blockbench)

- **Id / type** : `industrial_mash_tun` — **vue d’ensemble formée**. Ce n’est **pas** le modèle jeu et **pas** `mash_tun` (chêne, `OPEN`). Le jeu assemble des cubes + `industrial_mash_tun_controller`. **Interdit** : mega-mesh unique.
- **Refs** : `art/blockbench/industrial_mash_tun/reference.png`.
- **Kit hull** : cuboïde creux 5×5×5, gros bloc `industrial_casing` (`fermenter_casing`). Face −Z : `item_port` bas centre (grist) ; `industrial_mash_tun_controller` au-dessus ; `fluid_port` mi-hauteur à droite (eau / moût) ; `access_hatch` centre ; 2× `machine_window` (pale = tiles du contrôleur, pas un mesh plein). Dôme + bande cuivre = art only. **Pas** de `kinetic_port`. **Pas** de `OPEN`.
- **Contraintes raid** : cuboïde creux. Typique art ~5×5×5 ; code min 3×4×3 max 9×12×9. Casing `fermenter_casing`. Pale / râteau = détail hublot, pas un mesh plein.
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`. Grey → UV → paint 512 → **stop user**.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Bloc formé | **aucun** |
| Contrôleur | `MultiblockControllerBlock` / `MultiblockControllerBlockEntity` ; id `alcoholic:industrial_mash_tun_controller` |
| Artisanal (autre id) | `MashTunBlock` / `MashTunBlockEntity` — **ne pas** les modifier pour cette vue |
| Properties | `FORMED` sur le contrôleur (blockstate ignore) |
| BER formé | **aucune** |
| Définition | `BuiltinMachines.INDUSTRIAL_MASH_TUN` ; JSON `…/machines/industrial_mash_tun.json` |
| Former | `revalidate()` + `HollowCuboidValidator` + `WorldStructureSampler` |

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FORMED` contrôleur.
- **Art sans Java** : dôme / pale intérieure — pas de BER, pas de `OPEN`.
- **Si pale animée** : BER sur `mashTunControllerEntity` (pattern press). **Ne pas** coller `MashTunBlock.OPEN` sur le contrôleur industriel.

## Mini-prompt copiable

```
NE sculpte PAS un mega-mesh industrial_mash_tun comme bloc jeu.
Vue d’ensemble : industrial_mash_tun/reference.png.
Modèle jeu = hull + industrial_mash_tun_controller. Cuboïde creux ~5×5×5, acier + peu de cuivre.
PAS le mash_tun chêne. Hublots ronds, 1 contrôleur, face −Z.
MCP Blockbench seul. Skill alcoholic-java-machine-model.
Grey → UV → paint 512 → stop user.
Java : MultiblockControllerBlockEntity + HollowCuboidValidator. Pas MashTunBlock.
Pale animée = BER pattern press. Interdit process / 4e pipeline.
```
