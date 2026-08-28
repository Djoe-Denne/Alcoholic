# Industrial tank controller textures

`master-512` will contain the approved full-resolution atlas.
It is the only source used to generate lower-resolution runtime textures.

Do not upscale a reduced atlas or overwrite the master files when preparing a
new pack. The editable source remains in the adjacent Blockbench project.

This block is painted blue-grey industrial steel (unformed controller).
It does not use the locked oak tile.

The current deliverable in this folder is `../reference.png` only
(single cube, unformed desk + cistern/gauge pictogram). Paint the 512 master
after the Blockbench silhouette is approved.

## Brief modelage (agent Blockbench)

- **Id / type** : `industrial_tank_controller` — contrôleur unitaire 1×1 (réservoir passif, non formé).
- **Refs** : `art/blockbench/industrial_tank_controller/reference.png`. Vue formée = `art/blockbench/industrial_storage_tank/`.
- **Kit hull** : acier `industrial_casing` ; pupitre −Z ; pictogramme citerne / jauge ; accent bleu-gris ; pas de serpentins.
- **Contraintes raid** : cuboïde fermé, 1 contrôleur, pas de process painted (vapeur, presse, cylindres).
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Grey silhouette → UV pack → paint 512 → **stop user**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Block | `MultiblockControllerBlock` (`BuiltinMachines.INDUSTRIAL_TANK`) |
| Fichiers | `MultiblockControllerBlock.java` + `MultiblockControllerBlockEntity.java` |
| BE type | `AlcoholicIds.INDUSTRIAL_TANK_ENTITY` = `alcoholic:industrial_tank_controller` |
| Properties | `FORMED` — pas de `FACING` |
| Renderer / BER | **aucune** |
| Modèle / blockstate | handmade `minecraft:block/block` + variante `""` — **ignore** `FORMED` |
| Datagen | `GrapeAssetDataProvider.addHandmadeSimpleBlock("industrial_tank_controller")` |
| Définition | `BuiltinMachines.industrialTank()` (`MachineKind.STORAGE`, pas de process) ; JSON `…/machines/industrial_storage_tank.json` |
| Formation | `revalidate()` → `HollowCuboidValidator` + `WorldStructureSampler` |

Pas de bloc `industrial_storage_tank`. Casing tag `alcoholic:industrial_tank_casing`.

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FORMED` (blockstate l’ignore).
- **Art sans Java visuel** : unformed only.
- **Si look formé** : split `_formed` comme press/roller mill dans `GrapeAssetDataProvider`.

## Mini-prompt copiable

```
Sculpte / texture industrial_tank_controller (Java 1.19.2, pas Bedrock/GeckoLib).
Contrôleur 1×1 NON FORMÉ, pas une machine de process. Ref : industrial_tank_controller/reference.png.
Acier casing, pupitre −Z, citerne/jauge, pas de vapeur ni presse.
MCP Blockbench seul. Skill alcoholic-java-machine-model.
Grey silhouette → UV → paint 512 → stop user.
Classes : MultiblockControllerBlock / BlockEntity (FORMED ; blockstate l’ignore).
Look formé = pattern press _formed. Interdit process / mega-mesh / 4e pipeline.
```
