# Fluid port textures

`master-512` contains the approved 512 atlas and `SHA256SUMS.txt`.
It is the only source used to generate lower-resolution runtime textures.

Do not upscale a reduced atlas or overwrite the master files when preparing a
new pack. The editable source remains in the adjacent Blockbench project.

Steel tiles are copied from `industrial_casing`. The blue ring/valve tile and
the dark orifice tile are unique to this block. It does not use the locked oak
tile. `MODE` is IO config, not a second atlas.

This is an industrial steel cube (same language as `industrial_casing`),
not oak. Front face (−Z): flange + orifice + valve, discrete blue accent.

## Brief modelage (agent Blockbench)

- **Id / type** : `fluid_port` — cube 1×1 hull (port fluide).
- **Refs** : `art/blockbench/fluid_port/reference.png`.
- **Kit hull** : acier `industrial_casing` ; bride ronde + vanne −Z ; accent bleu discret.
- **Contraintes raid** : cuboïde fermé, orifice lisible en 16×16.
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Grey silhouette → UV pack → paint 512 → **stop user**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Block | `minecraft-common/src/main/java/com/djden/alcoholic/minecraft/multiblock/FluidPortBlock.java` |
| BlockEntity | `…/multiblock/FluidPortBlockEntity.java` |
| Helpers | `PortBlocks.java` (`FACING`, `MODE`) |
| BE type | `AlcoholicIds.FLUID_PORT_ENTITY` = `alcoholic:fluid_port` via `IndustrialContentRegistrar` |
| Properties | `PortBlocks.FACING`, `PortBlocks.MODE` (`ConfiguredPortMode` : `input` / `output` / `both`) |
| Renderer / BER | aucune |
| Modèle / blockstate | handmade `…/models/block/fluid_port.json` ; generated `facing=*` + `mode=*` (même modèle) |
| Texture défaut | handmade 64×64 `…/textures/block/fluid_port.png` |
| Datagen | `FluidPortAssetData` (blockstate + item) — **pas** de `cube_all` generated |
| Tags | `alcoholic:industrial_ports` |

`MODE` est de la config IO (sneak / clé Create), pas un look.

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FACING` + `MODE` ; BE liée au contrôleur (`ControllerBound`).
- **Art + Java visuel** : bride −Z, blockstate `facing=*` + `mode=*` (même modèle). Pas d’atlas `_input` / `_output`.

## Mini-prompt copiable

```
Sculpte / texture fluid_port (Java 1.19.2, pas Bedrock/GeckoLib).
Cube 1×1, bride ronde + vanne −Z, accent bleu. Ref : fluid_port/reference.png.
Acier = industrial_casing. MCP Blockbench seul. Skill alcoholic-java-machine-model.
Grey silhouette → UV → paint 512 → stop user.
Classes : FluidPortBlock / FluidPortBlockEntity / PortBlocks (FACING + MODE).
FluidPortAssetData : facing=* + mode=* (même modèle).
Interdit process/recipes / 4e pipeline.
```
