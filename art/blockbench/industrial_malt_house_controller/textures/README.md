# Industrial malt house controller textures

`master-512` will be the only source used to generate lower-resolution runtime
textures once the atlas is painted and approved.

Do not upscale a reduced atlas or overwrite the master files when preparing a
new pack. The editable source remains in the adjacent Blockbench project.

This folder currently holds the Blockbench reference board only. No 512 master
has been painted yet. The reference shows the UNFORMED / idle cube (lamps off,
painted malt-floor + kiln pictogram, discreet barley-amber accent).

## Brief modelage (agent Blockbench)

- **Id / type** : `industrial_malt_house_controller` — contrôleur unitaire 1×1 (malterie, non formé).
- **Refs** : `art/blockbench/industrial_malt_house_controller/reference.png`. Vue formée = `art/blockbench/industrial_malt_house/`.
- **Kit hull** : acier `industrial_casing` ; pupitre −Z ; pictogramme plancher / grain + flamme touraille ; accent orge-ambre.
- **Contraintes raid** : cuboïde fermé, 1 contrôleur, pas d’épis photo, pas de chêne `malting_floor`.
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Grey silhouette → UV pack → paint 512 → **stop user**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Block | `MultiblockControllerBlock` (`BuiltinMachines.INDUSTRIAL_MALT_HOUSE`) |
| Fichiers | `MultiblockControllerBlock.java` + `MultiblockControllerBlockEntity.java` |
| BE type | `AlcoholicIds.INDUSTRIAL_MALT_HOUSE_ENTITY` = `alcoholic:industrial_malt_house_controller` |
| Properties | `FORMED` — pas de `FACING` |
| Renderer / BER | **aucune** |
| Modèle / blockstate | handmade `minecraft:block/block` + variante `""` — **ignore** `FORMED` |
| Datagen | `GrapeAssetDataProvider.addHandmadeSimpleBlock("industrial_malt_house_controller")` |
| Définition | `BuiltinMachines.industrialMaltHouse()` ; JSON `…/machines/industrial_malt_house.json` |
| Formation | `revalidate()` → `HollowCuboidValidator` + `WorldStructureSampler` |

Pas de bloc `industrial_malt_house`. Casing `alcoholic:fermenter_casing`.

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FORMED` (blockstate l’ignore).
- **Art sans Java visuel** : unformed only. Grain / touraille de la formée = pas de BER.
- **Si look formé** : split `_formed` pattern press/roller. Flamme touraille = `LIT` pattern moteur **ou** atlas `_formed`.

## Mini-prompt copiable

```
Sculpte / texture industrial_malt_house_controller (Java 1.19.2, pas Bedrock/GeckoLib).
Contrôleur 1×1 NON FORMÉ. Ref : industrial_malt_house_controller/reference.png.
Acier casing, pupitre −Z, plancher/grain + touraille, accent ambre, voyants off.
MCP Blockbench seul. Skill alcoholic-java-machine-model.
Grey silhouette → UV → paint 512 → stop user.
Classes : MultiblockControllerBlock / BlockEntity (FORMED ; blockstate l’ignore).
Look formé = pattern press _formed. Interdit process / mega-mesh / 4e pipeline.
```
