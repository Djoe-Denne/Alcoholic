# Artisanal press textures

`master-512` contains the approved full-resolution atlas.
It is the only source used to generate lower-resolution runtime textures.

Do not upscale a reduced atlas or overwrite the master files when preparing a
new pack. The editable source remains in the adjacent Blockbench project.

Oak staves, platen and feet copy the locked primitive combustion engine
tile `(384,0)–(512,128)`: platen / tops as-is, staves rotated 90°, feet × 0.88.
Iron screw, nut, hoops and spout use the mash-tun blue-grey riveted metal.

## Brief modelage (agent Blockbench)

- **Id / type** : `artisanal_press` — machine artisanale 1×1 sculptée (pressoir à vis).
- **Refs** : `art/blockbench/artisanal_press/reference.png`.
- **Chêne** : cage / plateau / pieds depuis la tuile moteur ; vis + cercles = fer mash tun.
- **Contraintes** : tout dans 16³, cage fermée (fentes de jus OK), bec d’écoulement −Z.
- **Face utile** : −Z. Format **Java Block/Item**, pas Bedrock / GeckoLib.
- **Process** : une machine à la fois, MCP Blockbench **seul**. Grey silhouette → UV pack → paint 512 → **stop user**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Block | `minecraft-common/src/main/java/com/djden/alcoholic/minecraft/process/ArtisanalPressBlock.java` |
| BlockEntity | `…/process/ArtisanalPressBlockEntity.java` |
| BE type | `AlcoholicIds.ARTISANAL_PRESS_ENTITY` = `alcoholic:artisanal_press` via `ProcessingContentRegistrar` |
| Properties | `FACING` seulement — **pas** de `OPEN` / `LIT` |
| Renderer / BER | aucune (le BER `IndustrialPressRenderer` est le **multibloc** industriel) |
| Collision | `VoxelShape` composite dans le Block |
| Modèle JSON | handmade `minecraft-common/src/main/resources/assets/alcoholic/models/block/artisanal_press.json` |
| Texture défaut | handmade 64×64 `…/textures/block/artisanal_press.png` |
| Blockstate | generated variantes `facing=*` : `…/generated/…/blockstates/artisanal_press.json` |
| Datagen | `ArtisanalPressAssetData` (blockstate + item) — **pas** de modèle generated |
| Client | `AlcoholicClient` : `RenderType.cutout()` sur ce bloc |

## Animations / états → Java

Pas d’état `open` / `lit`. Vis et platen sont une pose statique. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split) seulement si un 3ᵉ état visuel est ajouté. **Pas de 4ᵉ pipeline.**
