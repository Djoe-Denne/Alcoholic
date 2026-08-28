# Kinetic port textures

`master-512` contains the approved 512 atlas and `SHA256SUMS.txt`.
It is the only source used to generate lower-resolution runtime textures.

Do not upscale a reduced atlas or overwrite the master files when preparing a
new pack. The editable source remains in the adjacent Blockbench project.

Steel tiles are copied from `industrial_casing`. The green hub ring and
the dark shaft well are unique to this block. It does not use the locked oak
tile. There is no `MODE` and no second atlas.

Reference image (concept only, not the atlas): `../reference.png`

## Brief modelage (agent Blockbench)

- **Id / type** : `kinetic_port` — cube 1×1 hull (accouplement d’arbre).
- **Refs** : `art/blockbench/kinetic_port/reference.png`.
- **Kit hull** : acier `industrial_casing` ; palier + bout d’arbre −Z ; accent vert discret. Pas de cogwheel Create.
- **Contraintes raid** : cuboïde fermé, axe central lisible en 16×16.
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Grey silhouette → UV pack → paint 512 → **stop user**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Block (vanilla mod) | `minecraft-common/src/main/java/com/djden/alcoholic/minecraft/multiblock/KineticPortBlock.java` |
| BlockEntity (vanilla mod) | `…/multiblock/KineticPortBlockEntity.java` |
| BE type | `AlcoholicIds.KINETIC_PORT_ENTITY` = `alcoholic:kinetic_port` via `IndustrialContentRegistrar` |
| Properties (vanilla) | `KineticPortBlock.FACING` seulement — **pas** de `MODE` (contrairement fluide/item) |
| Override Create | `integration-create-forge-1.19.2/…/CreateKineticPortBlock.java` + `CreateKineticPortBlockEntity.java` (`FACING`, shafts sur `FACING`) |
| Renderer / BER | aucune |
| Modèle / blockstate | handmade `…/models/block/kinetic_port.json` ; generated `facing=*` (même modèle) |
| Texture défaut | handmade 64×64 `…/textures/block/kinetic_port.png` |
| Datagen | `KineticPortAssetData` (blockstate + item) — **pas** de `cube_all` generated |
| Tags | `alcoholic:industrial_ports` ; **obligatoire** sur press + roller mill (`BuiltinMachines` `required_ports`) |

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FACING` ; BE cinétique (ou Create si le jar est chargé).
- **Art + Java visuel** : arbre −Z, blockstate `facing=*` (même modèle). Pas d’`OPEN`, pas de BER, pas d’atlas `_on`.
- **Si arbre qui tourne** : pièces split + `LIT`/`powered` pattern moteur, **ou** laisser Create animer son shaft. Ne pas forger un BER Create dans le common.

## Mini-prompt copiable

```
Sculpte / texture kinetic_port (Java 1.19.2, pas Bedrock/GeckoLib).
Cube 1×1, palier + arbre −Z, accent vert. Ref : kinetic_port/reference.png.
Acier = industrial_casing. Pas de cogwheel Create. MCP Blockbench seul.
Skill alcoholic-java-machine-model. Grey silhouette → UV → paint 512 → stop user.
Classes : KineticPortBlock / KineticPortBlockEntity (FACING). Create override optionnel.
KineticPortAssetData : facing=* seulement. Interdit process/recipes / 4e pipeline.
```
