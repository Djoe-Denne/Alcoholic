# Item port textures

`master-512` contains the approved 512 atlas and `SHA256SUMS.txt`.
It is the only source used to generate lower-resolution runtime textures.

Do not upscale a reduced atlas or overwrite the master files when preparing a
new pack. The editable source remains in the adjacent Blockbench project.

Steel tiles are copied from `industrial_casing`. The amber mouth trim and
the dark hopper well are unique to this block. It does not use the locked oak
tile. `MODE` is IO config, not a second atlas.

## Brief modelage (agent Blockbench)

- **Id / type** : `item_port` — cube 1×1 hull (port objets).
- **Refs** : `art/blockbench/item_port/reference.png`.
- **Kit hull** : acier `industrial_casing` ; gueule / hoppe carrée −Z ; accent ambre discret. Pas l’item hopper vanilla.
- **Contraintes raid** : cuboïde fermé, ouverture carrée lisible en 16×16.
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Grey silhouette → UV pack → paint 512 → **stop user**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Block | `minecraft-common/src/main/java/com/djden/alcoholic/minecraft/multiblock/ItemPortBlock.java` |
| BlockEntity | `…/multiblock/ItemPortBlockEntity.java` |
| Helpers | `PortBlocks.java` |
| BE type | `AlcoholicIds.ITEM_PORT_ENTITY` = `alcoholic:item_port` via `IndustrialContentRegistrar` |
| Properties | `PortBlocks.FACING`, `PortBlocks.MODE` (`input` / `output` / `both`) |
| Renderer / BER | aucune |
| Modèle / blockstate | handmade `…/models/block/item_port.json` ; generated `facing=*` + `mode=*` (même modèle) |
| Texture défaut | handmade 64×64 `…/textures/block/item_port.png` |
| Datagen | `ItemPortAssetData` (blockstate + item) — **pas** de `cube_all` generated |
| Tags | `alcoholic:industrial_ports` |

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FACING` + `MODE` ; BE `ItemPortBlockEntity`.
- **Art + Java visuel** : gueule −Z, blockstate `facing=*` + `mode=*` (même modèle). Pas d’`OPEN`, pas d’atlas `_input` / `_output`.

## Mini-prompt copiable

```
Sculpte / texture item_port (Java 1.19.2, pas Bedrock/GeckoLib).
Cube 1×1, gueule carrée −Z, accent ambre. Ref : item_port/reference.png.
Acier = industrial_casing. MCP Blockbench seul. Skill alcoholic-java-machine-model.
Grey silhouette → UV → paint 512 → stop user.
Classes : ItemPortBlock / ItemPortBlockEntity / PortBlocks (FACING + MODE).
ItemPortAssetData : facing=* + mode=* (même modèle).
Interdit hopper vanilla / process / 4e pipeline.
```
