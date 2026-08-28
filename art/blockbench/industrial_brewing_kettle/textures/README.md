# Industrial brewing kettle textures

`master-512` is the approved formed-overview atlas (hull steel + kettle
copper). `SHA256SUMS.txt` sits next to it. This id has **no game block**
and **no** resource-pack / 64×64 downsample — the world is still a hollow
cuboid of unit cubes + `industrial_brewing_kettle_controller`.

Do not upscale a reduced atlas or invent a mega-mesh `models/block/industrial_brewing_kettle.json`.

Exterior steel matches industrial casing. Copper language relates to
the artisanal brewing kettle, not a new brass.

## Brief modelage (agent Blockbench)

- **Id / type** : `industrial_brewing_kettle` — **vue d’ensemble formée**. Ce n’est **pas** le modèle jeu et **pas** `brewing_kettle` (artisanal). Le jeu assemble des cubes + `industrial_brewing_kettle_controller`. **Interdit** : mega-mesh unique.
- **Refs** : `art/blockbench/industrial_brewing_kettle/reference.png` et `industrial_brewing_kettle_formed.png`.
- **Kit hull** : cuboïde creux de blocs jeu. Obligatoire : `industrial_casing` + 1× `industrial_brewing_kettle_controller`. Optionnels (montrés sur la vue formée) : `access_hatch`, `machine_window`, `fluid_port`, `item_port`. **Pas** de `kinetic_port` (`KineticRequirement.none()`, `required_ports: []`). Dôme cuivre / cheminée = art only, pas un bloc.
- **Contraintes raid** : cuboïde creux. Typique art **5×5×6** (empreinte 5×5, hauteur 6 pour le dôme cuivre + cheminée) ; code min 3×4×3 max 7×8×7. Pas de chêne, pas de houblon photo.
- **Face utile** : −Z. Java Block/Item. MCP Blockbench **seul**. Skill : `.cursor/skills/alcoholic-java-machine-model/SKILL.md`. Grey → UV → paint 512 → **stop user**.

## Entité / classes Minecraft

| Rôle | Chemin réel |
|---|---|
| Bloc formé | **aucun** |
| Contrôleur | `MultiblockControllerBlock` / `MultiblockControllerBlockEntity` ; id `alcoholic:industrial_brewing_kettle_controller` |
| Artisanal (autre id) | `BrewingKettleBlock` / `BrewingKettleBlockEntity` — **ne pas** les toucher ici |
| Properties | `FORMED` (blockstate ignore) |
| BER formé | **aucune** |
| Définition | `BuiltinMachines.INDUSTRIAL_BREWING_KETTLE` ; JSON `…/machines/industrial_brewing_kettle.json` |
| Former | `revalidate()` + `HollowCuboidValidator` + `WorldStructureSampler` |
| Casing | `alcoholic:pressure_safe_casing` |

## Animations / états → Java

Si le modèle 3D introduit une animation ou un état visuel, mettre à jour BlockState, BlockEntity (tick / sync), modèles split, blockstate, éventuellement BER. Copier `mash_tun` (`open`) ou `primitive_combustion_engine` (`lit` + pièces split). **Pas de 4ᵉ pipeline.**

- **Déjà dans le code** : `FORMED` contrôleur.
- **Art sans Java** : dôme cuivre / cheminée de vapeur — pas de BER, pas de `LIT`.
- **Si vapeur / dôme animés** : BER sur `brewingKettleControllerEntity` **ou** `LIT` pattern moteur. Pas de mega-mesh.

## Mini-prompt copiable

```
NE sculpte PAS un mega-mesh industrial_brewing_kettle comme bloc jeu.
Vue d’ensemble : reference.png + industrial_brewing_kettle_formed.png.
Modèle jeu = hull + industrial_brewing_kettle_controller. Cuboïde creux ~5×5×5.
Acier + cuivre intérieur. PAS brewing_kettle artisanal. Hublots ronds, 1 contrôleur, −Z.
MCP Blockbench seul. Skill alcoholic-java-machine-model.
Grey → UV → paint 512 → stop user.
Java : MultiblockControllerBlockEntity + HollowCuboidValidator. Pas BrewingKettleBlock.
Vapeur animée = BER ou LIT. Interdit process / 4e pipeline.
```
