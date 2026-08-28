# Industrial press controller textures

`master-512` contains the approved 512 atlas and `SHA256SUMS.txt`.
It is the only source used to generate lower-resolution runtime textures.

Do not upscale a reduced atlas or overwrite the master files when preparing a
new pack. The editable source remains in the adjacent Blockbench project.

Steel tiles are copied from `industrial_casing`. The dark-red desk accents and
the lamp / slot well are unique to this block. It does not use the locked oak
tile. There is no `_formed` atlas yet: both `formed=*` variants use this cube.

Reference image (concept only, not the atlas): `../reference.png`

## Brief modelage (agent Blockbench)

- **Id / type** : `industrial_press_controller` — contrôleur unitaire 1×1 (machine **non** formée).
- **Refs** : `art/blockbench/industrial_press_controller/reference.png`. Vue formée = `art/blockbench/industrial_press/` (pas ce cube).
- **Kit hull** : acier `industrial_casing` ; pupitre −Z ; pictogramme presse / plaque ; accent rouge sombre ; voyants éteints.
- **Contraintes raid** : cuboïde fermé, 1 contrôleur acier, pas de lettrage, pas de chêne.
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Grey silhouette → UV pack → paint 512 → **stop user**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Block | `MultiblockControllerBlock` (`BuiltinMachines.INDUSTRIAL_PRESS`) via `IndustrialContentRegistrar` |
| Fichier Block | `minecraft-common/src/main/java/com/djden/alcoholic/minecraft/multiblock/MultiblockControllerBlock.java` |
| BlockEntity | `…/multiblock/MultiblockControllerBlockEntity.java` |
| BE type | `AlcoholicIds.INDUSTRIAL_PRESS_ENTITY` = `alcoholic:industrial_press_controller` |
| Properties | `MultiblockControllerBlock.FORMED` — **pas** de `FACING` |
| Renderer | `platform-forge-1.19.2/…/client/IndustrialPressRenderer.java` (platen fer + outline F3+B) enregistré dans `AlcoholicClient` sur **cette** BE |
| Modèle / blockstate | handmade `…/models/block/industrial_press_controller.json` ; generated `formed=*` (même modèle) |
| Texture défaut | handmade 64×64 `…/textures/block/industrial_press_controller.png` |
| Datagen | `IndustrialPressControllerAssetData` (blockstate + item) — **pas** de `cube_all` generated |
| Définition | `application/…/BuiltinMachines.industrialPress()` ; JSON `data/alcoholic/alcoholic/machines/industrial_press.json` |
| Formation | `MultiblockControllerBlockEntity.revalidate()` → `HollowCuboidValidator` + `WorldStructureSampler` ; pose debug `HollowCuboidPlacer` |
| Client | `RenderType.cutout()` sur ce bloc |

Le jeu **n’a pas** de bloc `industrial_press`. La formée = cubes hull + ce contrôleur. Le BER n’est **pas** un mega-mesh.

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FORMED` ; `strokeCycle` / `PressStrokeState` sur la BE ; BER platen = `Blocks.IRON_BLOCK`.
- **Art + Java visuel** : pupitre −Z, blockstate `formed=*` (même modèle). Pas de `FACING`, pas d’atlas `_formed`.
- **Si atlas formé peint** : `master-512/industrial_press_controller_formed.png` + variante `formed=true` distincte.
- **Si platen custom** : remplacer le `IRON_BLOCK` dans `IndustrialPressRenderer`, pas un mega-mesh formé.

## Mini-prompt copiable

```
Sculpte / texture industrial_press_controller (Java 1.19.2 cube, pas Bedrock/GeckoLib).
Contrôleur 1×1 NON FORMÉ. Ref : industrial_press_controller/reference.png.
Acier casing, pupitre −Z, pictogramme platen, accent rouge sombre, voyants off.
MCP Blockbench seul. Skill alcoholic-java-machine-model.
Grey silhouette → UV → paint 512 → stop user.
Classes : MultiblockControllerBlock / MultiblockControllerBlockEntity (FORMED).
BER platen = IndustrialPressRenderer (pas un mega-mesh). Interdit process/recipes / 4e pipeline.
```
