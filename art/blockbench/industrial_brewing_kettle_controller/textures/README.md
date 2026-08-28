# Industrial brewing kettle controller textures

`master-512` is not painted yet. This folder currently holds only
the Blockbench reference board (`../reference.png`).

When the 512 atlas is approved, it becomes the only source used to
generate lower-resolution runtime textures. Do not upscale a reduced
atlas or overwrite the master files when preparing a new pack.

Unformed standalone cube: steel casing + copper kettle pictogram.
No lettering on the painted face.

## Brief modelage (agent Blockbench)

- **Id / type** : `industrial_brewing_kettle_controller` — contrôleur unitaire 1×1 (non formé).
- **Refs** : `art/blockbench/industrial_brewing_kettle_controller/reference.png`. Vue formée = `art/blockbench/industrial_brewing_kettle/`.
- **Kit hull** : acier `industrial_casing` (pressure-safe) ; pupitre −Z ; pictogramme chaudron / dôme + vapeur ; accent cuivre **plus présent** que les autres contrôleurs, pas un cube tout cuivre.
- **Contraintes raid** : cuboïde fermé, 1 contrôleur, pas de houblon photo, pas de chêne.
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Grey silhouette → UV pack → paint 512 → **stop user**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Block | `MultiblockControllerBlock` (`BuiltinMachines.INDUSTRIAL_BREWING_KETTLE`) |
| Fichiers | `MultiblockControllerBlock.java` + `MultiblockControllerBlockEntity.java` |
| BE type | `AlcoholicIds.INDUSTRIAL_BREWING_KETTLE_ENTITY` = `alcoholic:industrial_brewing_kettle_controller` |
| Properties | `FORMED` — pas de `FACING` |
| Renderer / BER | **aucune** |
| Modèle / blockstate | handmade `minecraft:block/block` + variante `""` — **ignore** `FORMED` |
| Datagen | `GrapeAssetDataProvider.addHandmadeSimpleBlock("industrial_brewing_kettle_controller")` |
| Définition | `BuiltinMachines.industrialBrewingKettle()` ; JSON `…/machines/industrial_brewing_kettle.json` |
| Formation | `revalidate()` → `HollowCuboidValidator` + `WorldStructureSampler` |

Ce n’est **pas** `BrewingKettleBlock` (artisanal). Pas de bloc `industrial_brewing_kettle`. Casing `alcoholic:pressure_safe_casing`.

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FORMED` (blockstate l’ignore).
- **Art sans Java visuel** : unformed only. Dôme / vapeur de la formée = pas de BER.
- **Si look formé** : split `_formed` pattern press. Vapeur / chaleur = `LIT` pattern moteur **ou** atlas `_formed`.

## Mini-prompt copiable

```
Sculpte / texture industrial_brewing_kettle_controller (Java 1.19.2, pas Bedrock/GeckoLib).
Contrôleur 1×1 NON FORMÉ. Ref : industrial_brewing_kettle_controller/reference.png.
Acier casing, pupitre −Z, chaudron/dôme, accent cuivre, voyants off.
MCP Blockbench seul. Skill alcoholic-java-machine-model.
Grey silhouette → UV → paint 512 → stop user.
Classes : MultiblockControllerBlock / BlockEntity — PAS BrewingKettleBlock.
Look formé = pattern press _formed. Interdit process / mega-mesh / 4e pipeline.
```
