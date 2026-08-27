# Oak barrel textures

`master-512` contains the approved full-resolution atlas.
It is the only source used to generate lower-resolution runtime textures.

Do not upscale a reduced atlas or overwrite the master files when preparing a
new pack. The editable source remains in the adjacent Blockbench project.

Oak staves, heads and feet copy the locked primitive combustion
engine tile `(384,0)–(512,128)`: heads as-is, staves rotated 90°,
feet × 0.88.

## Brief modelage (agent Blockbench)

- **Id / type** : `oak_barrel` — machine artisanale 1×1 sculptée (fût couché).
- **Refs** : `art/blockbench/oak_barrel/reference.png`.
- **Chêne** : tuile verrouillée `art/blockbench/primitive_combustion_engine/textures/master-512/primitive_combustion_engine.png` `(384,0)–(512,128)`. Fer bleu-gris mash tun / moteur.
- **Contraintes** : cube 16³, silhouette fermée, bonde + robinet sur −Z. Pas de façade ouverte type théâtre.
- **Face utile** : −Z. Format **Java Block/Item**, pas Bedrock / GeckoLib.
- **Process** : une machine à la fois, MCP Blockbench **seul**. Grey silhouette → UV pack → paint 512 → **stop user**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Block | `minecraft-common/src/main/java/com/djden/alcoholic/minecraft/process/OakBarrelBlock.java` |
| BlockEntity | `…/process/OakBarrelBlockEntity.java` |
| BE type | `AlcoholicIds.OAK_BARREL_ENTITY` = `alcoholic:oak_barrel` via `ProcessingContentRegistrar` |
| Properties | `FACING` (horizontal) — **pas** de `OPEN` / `LIT` |
| Renderer / BER | aucune |
| Modèle JSON | handmade `minecraft-common/src/main/resources/assets/alcoholic/models/block/oak_barrel.json` |
| Texture défaut | handmade 64×64 `…/textures/block/oak_barrel.png` |
| Blockstate | generated variantes `facing=*` : `…/generated/…/blockstates/oak_barrel.json` |
| Datagen | `OakBarrelAssetData` (blockstate + item) — **pas** de `cube_all` generated |
| Handmade `main` | modèle + atlas 64×64 (comme `mash_tun`) |

## Animations / états → Java

Pas d’état `open` / `lit`. Le fût est directionnel : `FACING` oriente la bonde et le robinet (−Z). Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split) seulement si un 3ᵉ état visuel est ajouté. **Pas de 4ᵉ pipeline.**
