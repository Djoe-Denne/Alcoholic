# Industrial vat controller textures

`master-512` will contain the approved full-resolution atlas.
It is the only source used to generate lower-resolution runtime textures.

Do not upscale a reduced atlas or overwrite the master files when preparing a
new pack. The editable source remains in the adjacent Blockbench project.

This block is painted industrial steel with a small green vat pictogram.
It does not use the locked oak tile.

## Brief modelage (agent Blockbench)

- **Id / type** : `industrial_vat_controller` — contrôleur unitaire 1×1 (cuve de fermentation, non formé).
- **Refs** : `art/blockbench/industrial_vat_controller/reference.png`. Vue formée = `art/blockbench/industrial_fermentation_vat/`.
- **Kit hull** : acier `industrial_casing` ; pupitre −Z ; pictogramme cuve + airlock ; accent vert ; voyants éteints.
- **Contraintes raid** : cuboïde fermé, 1 contrôleur, pas de chêne, pas de levure photo.
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Grey silhouette → UV pack → paint 512 → **stop user**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Block | `MultiblockControllerBlock` (`BuiltinMachines.INDUSTRIAL_VAT`) |
| Fichiers | `MultiblockControllerBlock.java` + `MultiblockControllerBlockEntity.java` |
| BE type | `AlcoholicIds.INDUSTRIAL_VAT_ENTITY` = `alcoholic:industrial_vat_controller` |
| Properties | `FORMED` — pas de `FACING` |
| Renderer / BER | **aucune** |
| Modèle / blockstate | handmade `minecraft:block/block` + variante `""` — **ignore** `FORMED` |
| Datagen | `GrapeAssetDataProvider.addHandmadeSimpleBlock("industrial_vat_controller")` |
| Définition | `BuiltinMachines.industrialVat()` ; JSON `…/machines/industrial_fermentation_vat.json` |
| Formation | `revalidate()` → `HollowCuboidValidator` + `WorldStructureSampler` |

Pas de bloc `industrial_fermentation_vat`. Casing tag `alcoholic:fermenter_casing`.

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FORMED` sur le Block / la BE, mais le blockstate generated ne commute pas d’atlas.
- **Art sans Java visuel** : unformed only. Intérieur de la formée = pas de BER.
- **Si look formé** : copier le split press/roller (`formed=true` + `*_formed.json`) dans `GrapeAssetDataProvider`. Voyants allumés = `LIT` pattern moteur **ou** l’atlas `_formed`, pas les deux pipelines.

## Mini-prompt copiable

```
Sculpte / texture industrial_vat_controller (Java 1.19.2, pas Bedrock/GeckoLib).
Contrôleur 1×1 NON FORMÉ. Ref : industrial_vat_controller/reference.png.
Acier casing, pupitre −Z, cuve + airlock, accent vert, voyants off.
MCP Blockbench seul. Skill alcoholic-java-machine-model.
Grey silhouette → UV → paint 512 → stop user.
Classes : MultiblockControllerBlock / BlockEntity (FORMED ; blockstate l’ignore encore).
Look formé = pattern press _formed. Interdit process / mega-mesh / 4e pipeline.
```
