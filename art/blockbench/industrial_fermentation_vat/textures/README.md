# Industrial fermentation vat textures

`master-512` is the approved formed-overview hull atlas (copy of
`industrial_casing`). `SHA256SUMS.txt` sits next to it. This id has **no
game block** and **no** resource-pack / 64×64 downsample.

Mega-mesh = overlay BER `models/block/formed/industrial_fermentation_vat.json` at art size 3×5×3 only.
It is not a fourth block. Any formed size uses the 9-slice hull; fittings stay 1×1.

This formed multiblock is painted industrial steel (grey-blue casing, windows,
hatch, ports). It does not use the locked oak tile.

## Brief modelage (agent Blockbench)

- **Id / type** : `industrial_fermentation_vat` — **vue d’ensemble formée**. Ce n’est **pas** un bloc posable. Overlay BER à 3×5×3 ; sinon hull 9-slice + `industrial_vat_controller`.
- **Refs** : `art/blockbench/industrial_fermentation_vat/reference.png` et `industrial_fermentation_vat_formed.png`.
- **Kit hull** : cuboïde creux 3×5×3, gros bloc `industrial_casing` (`fermenter_casing`). Face −Z : `item_port` bas centre ; `industrial_vat_controller` au-dessus ; `fluid_port` mi-hauteur à droite ; `access_hatch` centre ; 2× `machine_window` en haut. **Pas** de `kinetic_port` (`required_ports: []`). Couvercle = art only.
- **Contraintes raid** : cuboïde creux plus haut que large. Typique art ~3×5×3 ; code min 3×4×3 max 9×16×9. Casing `fermenter_casing`. Pas de chêne (l’artisanal c’est `artisanal_fermenter`).
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`. Grey → UV → paint 512 → **stop user**.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Bloc formé | **aucun** |
| Contrôleur | `MultiblockControllerBlock` / `MultiblockControllerBlockEntity` ; id `alcoholic:industrial_vat_controller` |
| Properties | `FORMED` (blockstate contrôleur = `cube_all`, ignore `formed`) |
| BER formé | `FormedMultiblockRenderer` — hull 9-slice si `formed` ; mega-mesh seulement à 3×5×3 |
| Définition | `BuiltinMachines.INDUSTRIAL_VAT` ; JSON `…/machines/industrial_fermentation_vat.json` |
| Former | `revalidate()` + `HollowCuboidValidator` + `WorldStructureSampler` |

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FORMED` sur le contrôleur seulement.
- **Art sans Java** : niveau de liquide / intérieur sombre dans les hublots — pas de BER ni overlay fluide.
- **Si overlay liquide** : BER sur `vatControllerEntity` (pattern `IndustrialPressRenderer`), pas un mega-mesh.

## Mini-prompt copiable

```
NE sculpte PAS un mega-mesh industrial_fermentation_vat comme bloc jeu.
Vue d’ensemble : reference.png + industrial_fermentation_vat_formed.png.
Modèle jeu = hull + industrial_vat_controller. Cuboïde creux ~3×5×3, casing fermenter.
Hublots ronds, 1 contrôleur vert, face −Z, pas de chêne. MCP Blockbench seul.
Skill alcoholic-java-machine-model. Grey → UV → paint 512 → stop user.
Java : MultiblockControllerBlockEntity + HollowCuboidValidator. Pas de BER.
Liquide vu = BER pattern press. Interdit process / 4e pipeline.
```
