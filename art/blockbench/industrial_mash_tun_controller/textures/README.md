# Industrial mash tun controller textures

`reference.png` is the concept board for the UNFORMED 1×1 controller cube
(BDcraft / painted comic). Lights off. Pictogram: insulated vat + paddle.

`master-512` will be the only source used to generate lower-resolution
runtime textures once the atlas is painted after Blockbench UVs.

Do not upscale a reduced atlas or overwrite the master files when preparing a
new pack. The editable source remains in the adjacent Blockbench project.

Steel blue-grey casing. Tiny copper accent only (sister of the oak mash tun).
The formed multiblock lives in `art/blockbench/industrial_mash_tun/`.

## Brief modelage (agent Blockbench)

- **Id / type** : `industrial_mash_tun_controller` — contrôleur unitaire 1×1 (non formé).
- **Refs** : `art/blockbench/industrial_mash_tun_controller/reference.png`.
- **Kit hull** : acier `industrial_casing` ; pupitre −Z ; pictogramme cuve + pale ; **petit** accent cuivre.
- **Contraintes raid** : cuboïde fermé, 1 contrôleur acier (pas tout cuivre, pas de chêne).
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Grey silhouette → UV pack → paint 512 → **stop user**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Block | `MultiblockControllerBlock` (`BuiltinMachines.INDUSTRIAL_MASH_TUN`) |
| Fichiers | `MultiblockControllerBlock.java` + `MultiblockControllerBlockEntity.java` |
| BE type | `AlcoholicIds.INDUSTRIAL_MASH_TUN_ENTITY` = `alcoholic:industrial_mash_tun_controller` |
| Properties | `FORMED` — pas de `FACING` |
| Renderer / BER | **aucune** |
| Modèle / blockstate | handmade `minecraft:block/block` + variante `""` — **ignore** `FORMED` |
| Datagen | `GrapeAssetDataProvider.addHandmadeSimpleBlock("industrial_mash_tun_controller")` |
| Définition | `BuiltinMachines.industrialMashTun()` ; JSON `…/machines/industrial_mash_tun.json` |
| Formation | `revalidate()` → `HollowCuboidValidator` + `WorldStructureSampler` |

Ce n’est **pas** `MashTunBlock` (artisanal chêne, `OPEN`). Pas de bloc `industrial_mash_tun`.

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FORMED` (blockstate l’ignore).
- **Art sans Java visuel** : unformed only. Pale intérieure de la formée = pas de BER.
- **Si look formé** : split `_formed` pattern press. Pale animée = BER pattern `IndustrialPressRenderer`, pas `MashTunBlock.OPEN` (mauvaise machine).

## Mini-prompt copiable

```
Sculpte / texture industrial_mash_tun_controller (Java 1.19.2, pas Bedrock/GeckoLib).
Contrôleur 1×1 NON FORMÉ. Ref : industrial_mash_tun_controller/reference.png.
Acier casing, pupitre −Z, cuve + pale, tout petit cuivre, voyants off.
MCP Blockbench seul. Skill alcoholic-java-machine-model.
Grey silhouette → UV → paint 512 → stop user.
Classes : MultiblockControllerBlock / BlockEntity — PAS MashTunBlock.
Look formé = pattern press _formed. Interdit process / mega-mesh / 4e pipeline.
```
