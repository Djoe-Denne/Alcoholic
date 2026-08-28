# Industrial roller mill controller textures

`master-512` contains the approved 512 atlas and `SHA256SUMS.txt`.
It is the only source used to generate lower-resolution runtime textures.

Do not upscale a reduced atlas or overwrite the master files when preparing a
new pack. The editable source remains in the adjacent Blockbench project.

Steel tiles are copied from `industrial_casing`. The grey-yellow desk accents,
hazard stripe and lamp wells are unique to this block. It does not use the
locked oak tile. There is no `_formed` atlas yet: both `formed=*` variants use
this cube.

Reference image (concept only, not the atlas): `../reference.png`

## Brief modelage (agent Blockbench)

- **Id / type** : `industrial_roller_mill_controller` — contrôleur unitaire 1×1 (non formé).
- **Refs** : `art/blockbench/industrial_roller_mill_controller/reference.png`. Vue formée = `art/blockbench/industrial_roller_mill/`.
- **Kit hull** : acier `industrial_casing` ; pupitre −Z ; pictogramme deux cylindres ; accent gris-jaune ; voyants éteints.
- **Contraintes raid** : cuboïde fermé, 1 contrôleur, pas de grain en tas, pas de chêne.
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Grey silhouette → UV pack → paint 512 → **stop user**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Block | `MultiblockControllerBlock` (`BuiltinMachines.INDUSTRIAL_ROLLER_MILL`, `noOcclusion`) |
| Fichiers | `MultiblockControllerBlock.java` + `MultiblockControllerBlockEntity.java` |
| BE type | `AlcoholicIds.INDUSTRIAL_ROLLER_MILL_ENTITY` = `alcoholic:industrial_roller_mill_controller` |
| Properties | `FORMED` — pas de `FACING` |
| Renderer / BER | **aucune** (seul le press a un BER) |
| Modèle / blockstate | handmade `…/models/block/industrial_roller_mill_controller.json` ; generated `formed=*` (même modèle) |
| Texture défaut | handmade 64×64 `…/textures/block/industrial_roller_mill_controller.png` |
| Datagen | `IndustrialRollerMillControllerAssetData` (blockstate + item) — **pas** de `cube_all` generated |
| Définition | `BuiltinMachines.industrialRollerMill()` ; JSON `…/machines/industrial_roller_mill.json` |
| Formation | `revalidate()` → `HollowCuboidValidator` + `WorldStructureSampler` |

Pas de bloc `industrial_roller_mill`. Port cinétique **obligatoire**.

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FORMED`.
- **Art + Java visuel** : pupitre −Z, blockstate `formed=*` (même modèle). Pas de `FACING`, pas d’atlas `_formed`.
- **Si atlas formé peint** : `master-512/industrial_roller_mill_controller_formed.png` + variante `formed=true` distincte.
- **Si cylindres tournent** : BER sur **cette** BE (copier `IndustrialPressRenderer` / `MaltMillRenderer`), pas un mega-mesh.

## Mini-prompt copiable

```
Sculpte / texture industrial_roller_mill_controller (Java 1.19.2, pas Bedrock/GeckoLib).
Contrôleur 1×1 NON FORMÉ. Ref : industrial_roller_mill_controller/reference.png.
Acier casing, pupitre −Z, deux cylindres peints, voyants off.
MCP Blockbench seul. Skill alcoholic-java-machine-model.
Grey silhouette → UV → paint 512 → stop user.
Classes : MultiblockControllerBlock / BlockEntity (FORMED). Pas de BER aujourd’hui.
Cylindres animés = BER pattern press/mill, pas un mega-mesh. Interdit process / 4e pipeline.
```
