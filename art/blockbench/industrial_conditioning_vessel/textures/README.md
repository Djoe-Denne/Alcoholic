# Industrial conditioning vessel textures

`master-512` is the approved formed-overview hull atlas (copy of
`industrial_casing`). `SHA256SUMS.txt` sits next to it. This id has **no
game block** and **no** resource-pack / 64×64 downsample.
Do not invent a mega-mesh `models/block/industrial_conditioning_vessel.json`.

Formed multiblock (~3×6×3 hollow cuboid). Industrial painted steel with
a cooling jacket and cold-blue controller accent. No locked oak.

Reference image (concept only, not the atlas): `../reference.png`

## Brief modelage (agent Blockbench)

- **Id / type** : `industrial_conditioning_vessel` — **vue d’ensemble formée**. Ce n’est **pas** le modèle jeu. Le jeu assemble des cubes + `industrial_conditioning_vessel_controller`. **Interdit** : mega-mesh unique.
- **Refs** : `art/blockbench/industrial_conditioning_vessel/reference.png`.
- **Kit hull** : cuboïde creux 3×6×3, gros bloc `industrial_casing` (`fermenter_casing`). Face −Z : `item_port` bas centre ; `industrial_conditioning_vessel_controller` au-dessus ; `fluid_port` mi-hauteur à droite (x 0–16) ; `access_hatch` centre ; 3× `machine_window` (1 centre + 2 haut). Chemise = 2 bandes bleues sur +X (peinture, pas un bloc). 3 dômes toit = art only. **Pas** de `kinetic_port` (`required_ports: []`).
- **Contraintes raid** : cuboïde creux plus haut. Typique art ~3×6×3 ; code min 3×4×3 max 7×10×7. Casing `fermenter_casing`. Pas de bulles de ferment, pas de tank nu.
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`. Grey → UV → paint 512 → **stop user**.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Bloc formé | **aucun** |
| Contrôleur | `MultiblockControllerBlock` / `MultiblockControllerBlockEntity` ; id `alcoholic:industrial_conditioning_vessel_controller` |
| Properties | `FORMED` (blockstate ignore) |
| BER formé | **aucune** |
| Définition | `BuiltinMachines.INDUSTRIAL_CONDITIONING_VESSEL` ; JSON `…/machines/industrial_conditioning_vessel.json` |
| Former | `revalidate()` + `HollowCuboidValidator` + `WorldStructureSampler` |

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FORMED` contrôleur.
- **Art sans Java** : chemise froide / toit bombé — peinture hull, pas de BER.
- **Si givre / niveau animés** : BER sur `conditioningVesselControllerEntity` (pattern press). Pas de mega-mesh.

## Mini-prompt copiable

```
NE sculpte PAS un mega-mesh industrial_conditioning_vessel comme bloc jeu.
Vue d’ensemble : industrial_conditioning_vessel/reference.png.
Modèle jeu = hull + industrial_conditioning_vessel_controller. Cuboïde creux ~3×6×3.
Chemise froide peinte sur le casing. Hublots ronds, 1 contrôleur bleu, face −Z.
MCP Blockbench seul. Skill alcoholic-java-machine-model.
Grey → UV → paint 512 → stop user.
Java : MultiblockControllerBlockEntity + HollowCuboidValidator. Pas de BER.
Givre animé = BER pattern press. Interdit process / 4e pipeline.
```
