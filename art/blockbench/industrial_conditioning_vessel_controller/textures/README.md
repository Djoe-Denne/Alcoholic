# Industrial conditioning vessel controller textures

`master-512` will contain the approved full-resolution atlas.
It is the only source used to generate lower-resolution runtime textures.

Do not upscale a reduced atlas or overwrite the master files when preparing a
new pack. The editable source remains in the adjacent Blockbench project.

This unformed controller is industrial painted steel with a discreet
cold-blue accent. It does not use the locked oak tile.

Reference image (concept only, not the atlas): `../reference.png`

## Brief modelage (agent Blockbench)

- **Id / type** : `industrial_conditioning_vessel_controller` — contrôleur unitaire 1×1 (garde, non formé).
- **Refs** : `art/blockbench/industrial_conditioning_vessel_controller/reference.png`. Vue formée = `art/blockbench/industrial_conditioning_vessel/`.
- **Kit hull** : acier `industrial_casing` ; pupitre −Z ; pictogramme cuve chemisée + flocon ; accent bleu-froid ; voyants éteints.
- **Contraintes raid** : cuboïde fermé, 1 contrôleur, ambiance calme (pas de bulles de ferment, pas de presse).
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Grey silhouette → UV pack → paint 512 → **stop user**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Block | `MultiblockControllerBlock` (`BuiltinMachines.INDUSTRIAL_CONDITIONING_VESSEL`) |
| Fichiers | `MultiblockControllerBlock.java` + `MultiblockControllerBlockEntity.java` |
| BE type | `AlcoholicIds.INDUSTRIAL_CONDITIONING_VESSEL_ENTITY` = `alcoholic:industrial_conditioning_vessel_controller` |
| Properties | `FORMED` — pas de `FACING` |
| Renderer / BER | **aucune** |
| Modèle / blockstate | handmade `minecraft:block/block` + variante `""` — **ignore** `FORMED` |
| Datagen | `GrapeAssetDataProvider.addHandmadeSimpleBlock("industrial_conditioning_vessel_controller")` |
| Définition | `BuiltinMachines.industrialConditioningVessel()` ; JSON `…/machines/industrial_conditioning_vessel.json` |
| Formation | `revalidate()` → `HollowCuboidValidator` + `WorldStructureSampler` |

Pas de bloc `industrial_conditioning_vessel`. Casing `alcoholic:fermenter_casing`.

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FORMED` (blockstate l’ignore).
- **Art sans Java visuel** : unformed only. Chemise froide de la formée = pas de BER.
- **Si look formé** : split `_formed` pattern press/roller.

## Mini-prompt copiable

```
Sculpte / texture industrial_conditioning_vessel_controller (Java 1.19.2, pas Bedrock/GeckoLib).
Contrôleur 1×1 NON FORMÉ. Ref : industrial_conditioning_vessel_controller/reference.png.
Acier casing, pupitre −Z, cuve chemisée + froid, accent bleu, voyants off.
MCP Blockbench seul. Skill alcoholic-java-machine-model.
Grey silhouette → UV → paint 512 → stop user.
Classes : MultiblockControllerBlock / BlockEntity (FORMED ; blockstate l’ignore).
Look formé = pattern press _formed. Interdit process / mega-mesh / 4e pipeline.
```
