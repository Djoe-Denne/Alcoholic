# Craft casing textures

`master-512` contains the approved 512 atlas and `SHA256SUMS.txt`.
It is the only source used to generate lower-resolution runtime textures.

Do not upscale a reduced atlas or overwrite the master files when preparing a
new pack. The editable source remains in the adjacent Blockbench project.

This block is painted workshop copper (plates, rivets, seams). Geometry is
cloned from `industrial_casing`; the atlas is a luminance recolor of that
steel master using the `brewing_kettle` copper stops. Do not write this
atlas back onto `industrial_casing`.

## Brief modelage (agent Blockbench)

- **Id / type** : `craft_casing` — cube 1×1 hull partagé (revêtement). **Pas** une machine.
- **Refs** : `art/blockbench/craft_casing/reference.png`.
- **Kit hull** : ce cube **est** le master cuivre à recopier pour les contrôleurs craft.
- **Contraintes raid** : cuboïde fermé, tileable, pas de façade ouverte, pas de fenêtre, pas de logo. Cuivre d’atelier peint, pas de laiton steampunk, pas d’acier usine.
- **Face utile** : −Z (faces identiques — pas de `FACING`).
- **Format** : Java Block/Item, pas Bedrock / GeckoLib. MCP Blockbench **seul**.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Block | `IndustrialPartBlock` (`PartRole.CASING`) via `CraftContentRegistrar` / `AlcoholicIds.CRAFT_CASING` |
| BlockEntity | **aucune** |
| Properties | `formed` — `RenderShape.INVISIBLE` dès que le contrôleur lié est formé |
| Modèles | handmade `…/models/block/craft_casing.json` |
| Texture défaut | handmade 64×64 `…/textures/block/craft_casing.png` |
| Blockstate | generated variantes `formed=false` / `formed=true` |
| Datagen | `CraftAssetData` (blockstate + item) — **pas** de `cube_all` generated |
| Tags | `alcoholic:craft_casing` |

## Mini-prompt copiable

```
Sculpte / texture craft_casing (Java 1.19.2, pas Bedrock/GeckoLib).
Cube 1×1 cuivre d’atelier riveté, tileable. Ref : craft_casing/reference.png.
Géométrie = industrial_casing. Pas d’acier, pas de chêne.
MCP Blockbench seul. Skill alcoholic-java-machine-model.
CraftAssetData blockstate+item only. Interdit process/recipes.
```
