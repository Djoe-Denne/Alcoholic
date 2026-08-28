# Industrial casing textures

`master-512` contains the approved 512 atlas and `SHA256SUMS.txt`.
It is the only source used to generate lower-resolution runtime textures.

Do not upscale a reduced atlas or overwrite the master files when preparing a
new pack. The editable source remains in the adjacent Blockbench project.

This block is painted industrial steel (grey-blue plates, rivets).
It does not use the locked oak tile. Later hull cubes (window, ports,
controllers) copy these steel tiles instead of inventing a new blue-grey.

## Brief modelage (agent Blockbench)

- **Id / type** : `industrial_casing` — cube 1×1 hull partagé (revêtement). **Pas** une machine.
- **Refs** : `art/blockbench/industrial_casing/reference.png`.
- **Kit hull** : ce cube **est** le master visuel à recopier pour hublot / trappe / ports / contrôleurs.
- **Contraintes raid** : cuboïde fermé, tileable, pas de façade ouverte, pas de fenêtre, pas de logo. Acier gris-bleu peint, pas de rouille, pas de chrome Create.
- **Face utile** : −Z (faces identiques — pas de `FACING`).
- **Format** : Java Block/Item, pas Bedrock / GeckoLib. MCP Blockbench **seul**. Grey silhouette → UV pack → paint 512 → **stop user**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Block | `IndustrialPartBlock` (`PartRole.CASING`) via `IndustrialContentRegistrar` / `AlcoholicIds.INDUSTRIAL_CASING` |
| Fichier | `minecraft-common/src/main/java/com/djden/alcoholic/minecraft/multiblock/IndustrialPartBlock.java` |
| BlockEntity | **aucune** |
| Properties | `formed` — `RenderShape.INVISIBLE` dès que le contrôleur lié est formé |
| Renderer | aucune (le hull soudé est le BER 9-slice du contrôleur) |
| Modèles | handmade `…/models/block/industrial_casing.json` |
| Texture défaut | handmade 64×64 `…/textures/block/industrial_casing.png` |
| Blockstate | generated variantes `formed=false` / `formed=true` |
| Datagen | `IndustrialCasingAssetData` (blockstate + item) — **pas** de `cube_all` generated |
| Tags | `alcoholic:pressure_safe_casing`, `fermenter_casing`, `industrial_tank_casing` |

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : cube statique tileable, aucun état.
- **Si face unique / rivets non tileables** : ajouter `FACING` sur une sous-classe, pas sur tout `IndustrialPartBlock`.

## Mini-prompt copiable

```
Sculpte / texture industrial_casing (Java 1.19.2, pas Bedrock/GeckoLib).
Cube 1×1 acier gris-bleu riveté, tileable. Ref : industrial_casing/reference.png.
Pas de fenêtre, pas de chêne. MCP Blockbench seul.
Skill alcoholic-java-machine-model. Grey silhouette → UV → paint 512 → stop user.
Classe : IndustrialPartBlock (CASING), aucune BE / property.
IndustrialCasingAssetData. Interdit process/recipes / 4e pipeline.
```
