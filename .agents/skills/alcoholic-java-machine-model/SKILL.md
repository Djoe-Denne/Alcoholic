---
name: alcoholic-java-machine-model
description: Author one Alcoholic Java 1.19.2 voxel block from a reference board using Blockbench MCP, a 512 BDcraft master, and the resource-pack downsample chain. Use when adding or revising malting_floor, primitive_combustion_engine, malt_mill, mash_tun, brewing_kettle, or another machine model, or when the user mentions BDcraft, resourcepacks, or a 512 master atlas.
---

# Alcoholic Java machine model

One machine per chat. Copy the **three finished examples** (engine + malting floor + mash tun). Do not invent a fourth pipeline.

Also load `$blockbench-mcp-modeling` for Blockbench tool use. Final deliverable is **Java Block/Item**, never Bedrock or GeckoLib.

## Canonical examples

Read [examples.md](examples.md) before touching files.

| Machine | Why it is the example |
|---|---|
| `primitive_combustion_engine` | Full chain: `.bbmodel`, master-512 off+on, SHA-256, split models, packs, 64×64 in the mod. **Canonical oak tile.** |
| `malting_floor` | Same paint language, no `on` state, 64×64 in the mod, 512/256/128 packs |
| `mash_tun` | Same paint chain as the floor. **Lid `open`**, not `lit`. Reuses the engine oak tile (rotated for staves). |

`malt_mill` has a 512 master and review shots but is **not** a finished example until it has the same pack + default export as those rows.

## Hard rules

- Do not change block ids, recipes, loot, or process Java.
- Do not edit the other machines' art, packs, or `assets/alcoholic` files.
- Paint **512×512** first. Stop for user approval. No downsample and no mod replace before that.
- Every smaller atlas is resized **from the 512 master only**, never from a previous reduction.
- Default in the mod is **64×64** (each material column ≈ vanilla 16×16 on a 4-column atlas). Packs hold 16 / 32 / 128 / 256 / 512. Do not add a redundant 64× pack for a machine whose 64 already ships in the mod.
- Style: BDcraft / painted comic — soft gradients, rivets, grain. No dirty 1px face grid.
- **Locked oak** — copy the engine / mash_tun oak, do not invent a darker chocolate wood. See below.
- Functional face `-Z` unless the user says otherwise.
- Grey silhouette → fix shape → pack UVs → paint 512 → screenshots → wait.

## Locked oak (every later machine)

Reuse the **primitive combustion engine** master-512 oak tile. The approved mash tun is the proof: copy those pixels, do not invent a new brown.

- Source: `art/blockbench/primitive_combustion_engine/textures/master-512/primitive_combustion_engine.png`, tile `(384,0)–(512,128)` (also `resourcepacks/Alcoholic-512x/.../primitive_combustion_engine.png`).
- Color: warm medium oak, hue ≈ 28°, mean RGB ≈ `(108, 69, 35)`, high local contrast. Not chocolate, not honey-floor posts.
- Motifs: painted planks, flowing grain, **oval knots**. No hard 4-stripe bars, no square knots, no log-cylinder bevels.
- How to apply: lid / tops = tile as-is (horizontal grain); staves / posts = same tile rotated 90° (vertical grain); feet / darker members = same tile × ~0.88.
- Next machines (`brewing_kettle`, mill export, fermenter, barrel, press, crock) must start from this tile unless the user names a different wood.
- Forbidden: re-painting oak from scratch, sampling the malting-floor honey posts, or shifting the hue/value “to taste”.

## Layout to copy

```
art/blockbench/<id>/
  <id>.bbmodel
  reference.png
  textures/master-512/<id>.png
  textures/master-512/SHA256SUMS.txt    # plus <id>_on.png if the block has a lit state
  textures/README.md                    # “master is the only source”

resourcepacks/Alcoholic-<N>x/assets/alcoholic/textures/block/<id>.png

minecraft-common/src/main/resources/assets/alcoholic/
  models/block/<id>.json                # plus _on / _open / moving parts if needed
  textures/block/<id>.png               # 64×64 default
```

Handmade **block** model and 64×64 atlas live in `main`. Blockstate + item model go through `*AssetData` into `src/generated/resources`, like the floor and mash tun. Do not copy those two JSON files into `main` as well — Gradle rejects the duplicate. The engine keeps them in `main` only because of `lit` + split models.

The mash tun lid is **`open`**, not `lit`: closed model by default, `mash_tun_open` (22.5° X) while the GUI is open. Same barrel opener counter. No second atlas.

`pack.mcmeta` is pack_format **9** (Java 1.19.2).

## Checklist

```
- [ ] Named a single <id>
- [ ] Read examples.md and opened the engine, floor, and mash tun references
- [ ] Oak copied from the locked engine tile (staves 90°, lid as-is, feet × ~0.88)
- [ ] Blockbench open, MCP on 8787, get_status OK
- [ ] New Java project; grey silhouette approved
- [ ] UVs packed; 512 painted; user said the master is good
- [ ] SHA-256 written next to the master
- [ ] 16/32/128/256/512 packs updated for this id only
- [ ] 64×64 copied into the mod resources
- [ ] Recipes/loot/generated data untouched
```

## Next machines

Same skill, same oak, same examples: `malt_mill` (finish export), then `brewing_kettle`, press, fermenter, barrel, crock when the user names one.
