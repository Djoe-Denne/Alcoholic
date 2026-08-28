---
title: >-
  World Fluid Textures
category: concepts
tags: [minecraft, type/concept, project/alcoholic]
aliases: [still flow fluids, hopped_wort, grape must tiles]
sources:
  - "C:/Users/djden/source/repos/Alcoholic/resourcepacks/README.md"
  - "C:/Users/djden/source/repos/Alcoholic/docs/guides/shaders.md"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b8ca2270-f9dc-4b57-841d-bcce0617c10d/b8ca2270-f9dc-4b57-841d-bcce0617c10d.jsonl"
summary: >-
  Four painted still/flow fluids downsample from 512. Young wines, aged wines, and wort stay vanilla water plus a Java tint.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-08-28T22:30:00+02:00
updated: 2026-08-28T22:30:00+02:00
---

# World Fluid Textures

Nine world fluids are `LiquidBlock` translucides. Only four are painted and animated like vanilla water (vertical frame strips): `beer`, `hopped_wort`, `red_grape_must`, and `white_grape_must`. Young wines, aged wines, and wort use vanilla water tiles plus a Java tint. Leftover tinted-water world PNGs (`red_wine`, `white_wine`, `wort`, `young_*`) were removed from the packs and the common assets.

The catalogue tag is `#alcoholic:world_fluids` (blocks plus source/flowing). Do not add it to `minecraft:water` (hydration, boats, gameplay).

## Frame counts

Still width is the pack resolution; flow width is twice that.

- 16 / 32 / 64 packs: 32 frames, `frametime` 2
- 128 pack: 24 frames timed to the same 64-tick loop
- 256 / 512 packs: 8 frames, `frametime` 8

All optional item, plant, and painted-fluid textures are generated from archived 512 masters, never from another reduced version. See [[resource-pack-resolution-chain]].

```powershell
python tools/build_fluid_texture_packs.py
python tools/build_fluid_texture_packs.py --clean-tinted
```

Shader waves are a pack-author edit, not a texture-pack file. See [[shader-world-fluids]].

## Related

- [[shader-world-fluids]]
- [[resource-pack-resolution-chain]]
- [[grain-processing]]
- [[liquid-batch]]
- [[cursor-progression-and-fluids-session]]
- [[alcoholic]]
