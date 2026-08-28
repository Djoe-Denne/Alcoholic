# Electric motor textures

Reference boards live in the parent folder:

- `reference.png` — unpowered / off
- `electric_motor_on.png` — powered / on (same silhouette, amber vents)

`master-512` contains the approved 512 atlas (off + on) and `SHA256SUMS.txt`.
It is the only source used to generate lower-resolution runtime textures.

Do not upscale a reduced atlas or overwrite the master files when preparing a
pack. The editable source remains in the adjacent Blockbench project.

## Brief modelage (agent Blockbench)

- **Id / type** : `electric_motor` — machine artisanale / actionneur 1×1 sculpté (pas IE, pas Create).
- **Refs** : `art/blockbench/electric_motor/reference.png` (éteint) et `electric_motor_on.png` (allumé, mêmes volumes).
- **Kit** : acier gris-bleu industriel (même langage que le casing). Cuivre = détails seulement. Arbre de sortie sur −Z.
- **Contraintes** : cube 16³, boîtier fermé, pas de câbles IE. Atlas 512 **off + on** (comme le moteur à combustion).
- **Face utile** : −Z. Format **Java Block/Item**, pas Bedrock / GeckoLib.
- **Process** : une machine à la fois, MCP Blockbench **seul**. Grey silhouette → UV pack → paint 512 → **stop user**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Block | `minecraft-common/src/main/java/com/djden/alcoholic/minecraft/mechanical/ElectricMotorBlock.java` |
| BlockEntity | `…/mechanical/ElectricMotorBlockEntity.java` |
| Settings | `…/mechanical/ElectricMotorSettings.java` |
| BE type | `AlcoholicIds.ELECTRIC_MOTOR_ENTITY` = `alcoholic:electric_motor` via `ProcessingContentRegistrar` |
| Properties | `FACING`, `LIT` (`BlockStateProperties.LIT`) — lumière 7 si `LIT` |
| Renderer / BER | `ElectricMotorRenderer` — arbre split `electric_motor_shaft` si `LIT` |
| Modèles | handmade `…/models/block/electric_motor.json` + `_on.json` |
| Texture défaut | handmade 64×64 `…/textures/block/electric_motor.png` (+ `_on`) |
| Blockstate | generated `facing=*` + `lit=*` : `…/generated/…/blockstates/electric_motor.json` |
| Datagen | `ElectricMotorAssetData` (blockstate + item) — **pas** de `cube_all` generated |
| Client | `AlcoholicClient` : `RenderType.cutout()` |
| Handmade `main` | modèles off/on + split `_shaft` + atlas 64×64 (pattern moulin / moteur à combustion) |

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FACING` + `LIT` ; la BE pose `LIT` selon l’alimentation (`ElectricMotorBlockEntity`).
- **Art** : off / on **alignés** sur `LIT`. Pas d’écart d’état.
- **Arbre** : split `electric_motor_shaft.json` + BER `ElectricMotorRenderer` tant que `LIT`. Pas de property `SPINNING`.

## Mini-prompt copiable

```
Sculpte electric_motor (Java 1.19.2 Block/Item, pas Bedrock/GeckoLib).
Moteur 1×1 acier Alcoholic. Refs : reference.png + electric_motor_on.png.
Arbre de sortie −Z. Même silhouette off/on, voyants ambre si lit.
MCP Blockbench seul. Skill alcoholic-java-machine-model.
Grey silhouette → UV → paint 512 (off + on) → stop user.
Classes : ElectricMotorBlock / BlockEntity (FACING + LIT déjà là). ElectricMotorAssetData.
Pièces split = pattern primitive_combustion_engine. Interdit process/recipes / 4e pipeline.
```
