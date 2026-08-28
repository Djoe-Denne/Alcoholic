# Finished examples

Inspect these on disk. Match folder names, atlas role, and what is *not* generated.

## 1. Primitive combustion engine (full)

- Art: `art/blockbench/primitive_combustion_engine/`
- Master: `textures/master-512/primitive_combustion_engine.png` and `primitive_combustion_engine_on.png` (512×512)
- Checksums: `textures/master-512/SHA256SUMS.txt`
- Policy: `textures/README.md` — never overwrite the master; never upsample a small atlas
- Runtime 64×64: `minecraft-common/src/main/resources/assets/alcoholic/textures/block/primitive_combustion_engine.png` (+ `_on`)
- Models: `models/block/primitive_combustion_engine.json`, `_on`, `_shaft`, `_flywheel` (moving parts as separate models)
- Packs: `resourcepacks/Alcoholic-{16,32,128,256,512}x/.../primitive_combustion_engine.png` (+ `_on`). No 64× pack for the engine.

## 2. Malting floor (same paint, simpler states)

- Art: `art/blockbench/malting_floor/` (`malting_floor.bbmodel`, `malting_floor_512_master.bbmodel`, `reference.png`)
- Runtime 64×64: `minecraft-common/src/main/resources/assets/alcoholic/textures/block/malting_floor.png`
- Packs: 128 / 256 / 512 (and a 64× pack copy). No `on` atlas.

## 3. Mash tun (same chain as the floor; locked oak)

- Art: `art/blockbench/mash_tun/`
- Master: `textures/master-512/mash_tun.png` (512×512, no `on`)
- Checksums: `textures/master-512/SHA256SUMS.txt`
- Oak: engine tile `(384,0)–(512,128)` — staves = 90° rotate, lid = as-is, feet = slightly darker
- Runtime 64×64: `minecraft-common/src/main/resources/assets/alcoholic/textures/block/mash_tun.png`
- Models: `models/block/mash_tun.json` (lid closed, default) and `mash_tun_open.json` (lid 22.5° X, the approved look). Parent `minecraft:block/block`, no `format_version` / `texture_size`
- Blockstate: `facing` + `open` (vanilla barrel). `open=true` while the GUI is open.
- Packs: `resourcepacks/Alcoholic-{16,32,128,256,512}x/.../mash_tun.png`. No 64× pack.

## Shared look

Masters are painted BDcraft-like (wood, iron, copper, readable silhouettes). Later machines reuse the **engine oak tile** locked on the mash tun, not a new brown.

## Not an example yet

`art/blockbench/malt_mill/` has `malt_mill.bbmodel`, `textures/master-512/malt_mill.png`, and `review/*.png`. It is not finished until it follows the same default-64 + pack set as the three rows above (unless the user explicitly keeps 32 as mill default).
