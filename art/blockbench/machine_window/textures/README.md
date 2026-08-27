# Machine window textures

`master-512` contains the approved 512 atlas and `SHA256SUMS.txt`.
It is the only source used to generate lower-resolution runtime textures.

Do not upscale a reduced atlas or overwrite the master files when preparing a
new pack. The editable source remains in the adjacent Blockbench project.

Steel tiles are copied from `industrial_casing`. The comic cyan glass tile is
unique to this block. It does not use the locked oak tile.

## Brief modelage (agent Blockbench)

- **Id / type** : `machine_window` — cube 1×1 hull (hublot **rond**).
- **Refs** : `art/blockbench/machine_window/reference.png`.
- **Kit hull** : recopier l’acier de `art/blockbench/industrial_casing/textures/master-512/industrial_casing.png` ; vitre comic bleutée, pas de photo / HDR.
- **Contraintes raid** : cuboïde fermé (tube intérieur), hublot rond (pas de baie rectangulaire), cadre riveté.
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Grey silhouette → UV pack → paint 512 → **stop user**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Block | `MachineWindowBlock` (`PartRole.WINDOW`, `noOcclusion`) via `IndustrialContentRegistrar` / `AlcoholicIds.MACHINE_WINDOW` |
| Fichier | `minecraft-common/src/main/java/com/djden/alcoholic/minecraft/multiblock/MachineWindowBlock.java` |
| BlockEntity | **aucune** |
| Properties | `FACING` seulement — hublot sur −Z du modèle |
| Renderer | aucune — `AlcoholicClient` pose `RenderType.translucent()` |
| Modèles | handmade `…/models/block/machine_window.json` |
| Texture défaut | handmade 64×64 `…/textures/block/machine_window.png` |
| Blockstate | generated `facing=*` : `…/generated/…/blockstates/machine_window.json` |
| Datagen | `MachineWindowAssetData` (blockstate + item) — **pas** de `cube_all` generated |
| Tags | `alcoholic:valid_machine_windows` (`AlcoholicBlockTags.VALID_MACHINE_WINDOWS`) |

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FACING` sur la sous-classe window. Client : translucent.
- **Liquide vu à travers** : aujourd’hui peint sur l’atlas. Un overlay fluide = BER sur le **contrôleur** (pattern `IndustrialPressRenderer`), pas une BE sur le hublot.

## Mini-prompt copiable

```
Sculpte / texture machine_window (Java 1.19.2, pas Bedrock/GeckoLib).
Cube 1×1, hublot ROND −Z, acier = industrial_casing. Ref : machine_window/reference.png.
MCP Blockbench seul. Skill alcoholic-java-machine-model.
Grey silhouette → UV → paint 512 → stop user.
Classe : MachineWindowBlock (WINDOW + FACING), pas de BE.
Client : translucent. Interdit process/recipes / 4e pipeline.
```
