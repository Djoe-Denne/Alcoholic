# Access hatch textures

`master-512` contains the approved 512 atlas and `SHA256SUMS.txt`.
It is the only source used to generate lower-resolution runtime textures.

Do not upscale a reduced atlas or overwrite the master files when preparing a
new pack. The editable source remains in the adjacent Blockbench project.

Steel language matches the other industrial casing cubes (blue-grey painted
plates, rivets, small brass latch on the hatch face).

## Brief modelage (agent Blockbench)

- **Id / type** : `access_hatch` — cube 1×1 hull (trappe rivetée, pas de hublot).
- **Refs** : `art/blockbench/access_hatch/reference.png`.
- **Kit hull** : acier = `art/blockbench/industrial_casing/reference.png` ; loquet laiton-ambre discret.
- **Contraintes raid** : cuboïde fermé, porte peinte sur −Z, pas de fenêtre.
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Grey silhouette → UV pack → paint 512 → **stop user**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Block | `AccessHatchBlock` (`PartRole.HATCH`) via `IndustrialContentRegistrar` / `AlcoholicIds.ACCESS_HATCH` |
| Fichier | `minecraft-common/src/main/java/com/djden/alcoholic/minecraft/multiblock/AccessHatchBlock.java` |
| BlockEntity | **aucune** |
| Properties | `FACING` seulement — porte fermée, pas d’`OPEN` |
| Renderer | aucune |
| Modèles | handmade `…/models/block/access_hatch.json` |
| Texture défaut | handmade 64×64 `…/textures/block/access_hatch.png` |
| Blockstate | generated `facing=*` : `…/generated/…/blockstates/access_hatch.json` |
| Datagen | `AccessHatchAssetData` (blockstate + item) — **pas** de `cube_all` generated |
| Tags | inclus dans les trois tags casing (`AlcoholicBlockTags`) avec le casing |

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FACING` sur la sous-classe hatch. Porte peinte fermée.
- **Si trappe qui s’ouvre** : `OPEN` pattern `mash_tun` sur `AccessHatchBlock` (pas sur tout `IndustrialPartBlock`) + modèles `_open`.

## Mini-prompt copiable

```
Sculpte / texture access_hatch (Java 1.19.2, pas Bedrock/GeckoLib).
Cube 1×1, trappe rivetée −Z, loquet laiton discret. Ref : access_hatch/reference.png.
Acier = industrial_casing. MCP Blockbench seul. Skill alcoholic-java-machine-model.
Grey silhouette → UV → paint 512 → stop user.
Classe : AccessHatchBlock (HATCH + FACING), pas de BE / OPEN aujourd’hui.
Trappe animée = mash_tun OPEN sur AccessHatchBlock. Interdit process/recipes / 4e pipeline.
```
