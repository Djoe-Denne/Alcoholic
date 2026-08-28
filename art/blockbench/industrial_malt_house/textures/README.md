# Industrial malt house textures

`master-512` is the approved formed-overview hull atlas (copy of
`industrial_casing`). `SHA256SUMS.txt` sits next to it. This id has **no
game block** and **no** resource-pack / 64×64 downsample.
Mega-mesh = overlay BER `models/block/formed/industrial_malt_house.json` at art size 5×4×5 only.
It is not a fourth block. Any formed size uses the 9-slice hull; fittings stay 1×1.

This formed multiblock is painted industrial steel (grey-blue casing, windows,
hatch, ports). It does not use the locked oak tile — that belongs to
`malting_floor`.

## Brief modelage (agent Blockbench)

- **Id / type** : `industrial_malt_house` — **vue d’ensemble formée**. Ce n’est **pas** un bloc posable. Overlay BER à 5×4×5 ; sinon hull 9-slice + `industrial_malt_house_controller`. Ce n’est **pas** `malting_floor`.
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
| BER formé | `FormedMultiblockRenderer` — hull 9-slice si `formed` ; mega-mesh seulement à 5×4×5 |
| Définition | `BuiltinMachines.INDUSTRIAL_MALT_HOUSE` ; JSON `…/machines/industrial_malt_house.json` |
| Former | `revalidate()` + `HollowCuboidValidator` + `WorldStructureSampler` |

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FORMED` contrôleur ; process malt côté stratégie (`IndustrialProcessStrategy`), pas un look.
- **Art + Java visuel** : casing `INVISIBLE` dès `formed` ; hull 9-slice toute taille ; mega-mesh overlay à 5×4×5.
- **Si grain / touraille animés** : pièces supplémentaires sur le même BER, pas un 4ᵉ bloc.

## Mini-prompt copiable

```
NE sculpte PAS un mega-mesh industrial_malt_house comme bloc jeu.
Vue d’ensemble : industrial_malt_house/reference.png.
Look formé = 9-slice + fittings 1×1 ; overlay BER à 5×4×5.
Pas le malting_floor bois. Hublots ronds, 1 contrôleur ambre, face −Z.
MCP Blockbench seul. Skill alcoholic-java-machine-model.
Grey → UV → paint 512 → stop user.
Java : FormedMultiblockRenderer + HollowCuboidValidator. Interdit process / 4e pipeline.
```
