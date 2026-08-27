# Artisanal blending crock textures

`master-512` contains the approved full-resolution atlas.
It is the only source used to generate lower-resolution runtime textures.

Do not upscale a reduced atlas or overwrite the master files when preparing a
new pack. The editable source remains in the adjacent Blockbench project.

Oak lid and darker feet copy the locked primitive combustion
engine tile `(384,0)–(512,128)`: lid as-is, feet × 0.88.
The stoneware body is glazed grey-beige terracotta, not oak staves.

Reference images (concept only, not the atlas):
- `../reference.png` — lid closed
- `../reference_open.png` — lid open, cream enamel interior

## Brief modelage (agent Blockbench)

- **Id / type** : `artisanal_blending_crock` — machine artisanale 1×1 sculptée (jarre / crock, pas un fût).
- **Refs** : `art/blockbench/artisanal_blending_crock/reference.png` (fermé) et `reference_open.png` (couvercle ouvert, émail crème).
- **Chêne** : couvercle + pieds depuis la tuile moteur ; corps = grès émaillé, **pas** de douves.
- **Contraintes** : cube 16³, silhouette fermée par défaut, bec / échancrure sur −Z.
- **Face utile** : −Z. Format **Java Block/Item**, pas Bedrock / GeckoLib.
- **Process** : une machine à la fois, MCP Blockbench **seul**. Grey silhouette → UV pack → paint 512 → **stop user**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Block | `minecraft-common/src/main/java/com/djden/alcoholic/minecraft/process/ArtisanalBlendingCrockBlock.java` |
| BlockEntity | `…/process/ArtisanalBlendingCrockBlockEntity.java` |
| BE type | `AlcoholicIds.ARTISANAL_BLENDING_CROCK_ENTITY` = `alcoholic:artisanal_blending_crock` via `ProcessingContentRegistrar` |
| Properties | `FACING` (horizontal) + `OPEN` (couvercle, pattern mash tun) |
| Renderer / BER | aucune |
| Collision | `VoxelShape` 2–14 / 0–14 / 1–14 dans le Block |
| Modèle JSON | handmade `…/models/block/artisanal_blending_crock.json` + `_open.json` |
| Texture défaut | handmade 64×64 `…/textures/block/artisanal_blending_crock.png` |
| Blockstate | generated `facing=*` + `open=*` : `…/generated/…/blockstates/artisanal_blending_crock.json` |
| Datagen | `ArtisanalBlendingCrockAssetData` (blockstate + item) — **pas** de `cube_all` generated |
| Handmade `main` | modèles fermé/ouvert + atlas 64×64 (comme `mash_tun`) |

## Animations / états → Java

Couvercle animé = pattern exact `mash_tun` : `BooleanProperty OPEN`, `ContainerOpenersCounter` synchronisé à l’ouverture GUI, modèles `artisanal_blending_crock.json` + `_open.json` (22.5° X). `FACING` oriente le bec (−Z). **Pas de 4ᵉ pipeline.**
