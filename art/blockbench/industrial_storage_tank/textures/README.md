# Industrial storage tank textures

`master-512` is the approved formed-overview hull atlas (copy of
`industrial_casing`). `SHA256SUMS.txt` sits next to it. This id has **no
game block** and **no** resource-pack / 64×64 downsample.
Mega-mesh = overlay BER `models/block/formed/industrial_storage_tank.json` at art size 3×5×3 only.
It is not a fourth block. Any formed size uses the 9-slice hull; fittings stay 1×1.

This formed multiblock is painted blue-grey industrial steel.
It does not use the locked oak tile. No process coils, steam, or press gear.

## Brief modelage (agent Blockbench)

- **Id / type** : `industrial_storage_tank` — **vue d’ensemble formée**. Ce n’est **pas** un bloc posable. Overlay BER à 3×5×3 ; sinon hull 9-slice + `industrial_tank_controller`.
- **Refs** : `art/blockbench/industrial_storage_tank/reference.png`.
- **Kit hull** : cuboïde creux 3×5×3, gros bloc `industrial_casing` (`industrial_tank_casing`). Face −Z : `item_port` bas centre ; `industrial_tank_controller` au-dessus ; `fluid_port` mi-hauteur à droite (x 0–16) ; `access_hatch` centre ; 2× `machine_window` en haut. Toit plat + regard = art only. **Pas** de `kinetic_port` (`required_ports: []`). **Pas** de serpentin / vapeur / dôme.
- **Contraintes raid** : cuboïde creux calme, toit plat. Typique art ~3×5×3 ; code min 3×4×3 max 9×16×9. Casing `industrial_tank_casing`. **Aucun** serpentin, vapeur, platen, cylindre.
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`. Grey → UV → paint 512 → **stop user**.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Bloc formé | **aucun** |
| Contrôleur | `MultiblockControllerBlock` / `MultiblockControllerBlockEntity` ; id `alcoholic:industrial_tank_controller` |
| Properties | `FORMED` (blockstate ignore) |
| BER formé | `FormedMultiblockRenderer` — hull 9-slice si `formed` ; mega-mesh seulement à 3×5×3 |
| Définition | `BuiltinMachines.INDUSTRIAL_TANK` (`MachineKind.STORAGE`, pas de process) ; JSON `…/machines/industrial_storage_tank.json` |
| Former | `revalidate()` + `HollowCuboidValidator` + `WorldStructureSampler` |

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FORMED` contrôleur ; tank passif (capacité seulement).
- **Art sans Java** : jauge / niveau dans les hublots — pas de BER.
- **Si jauge animée** : BER sur `tankControllerEntity` (pattern press). Pas de mega-mesh.

## Mini-prompt copiable

```
NE sculpte PAS un mega-mesh industrial_storage_tank comme bloc jeu.
Vue d’ensemble : industrial_storage_tank/reference.png.
Modèle jeu = hull + industrial_tank_controller. Cuboïde creux ~3×5×3, passif.
Pas de serpentin / vapeur / presse. Hublots ronds, 1 contrôleur, face −Z.
MCP Blockbench seul. Skill alcoholic-java-machine-model.
Grey → UV → paint 512 → stop user.
Java : MultiblockControllerBlockEntity + HollowCuboidValidator. Pas de BER.
Jauge animée = BER pattern press. Interdit process / 4e pipeline.
```
