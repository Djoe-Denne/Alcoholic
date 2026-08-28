---
title: Formed Multiblock Visual
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [9-slice hull, mega-mesh, FormedMultiblockRenderer]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/7709dbb4-6adc-48f3-ad6f-7de969fd9878/7709dbb4-6adc-48f3-ad6f-7de969fd9878.jsonl"
summary: >-
  A formed machine draws a welded 9-slice hull at any legal size. A mega-mesh overlay exists only at the art size used by debug place.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-08-28T19:15:00+02:00
updated: 2026-08-28T19:15:00+02:00
---

# Formed Multiblock Visual

`/alcoholic debug place beer industrial` used to drop loose cubes that did not match the formed recipe. The validator and interior capacity did not change. Only the client look of a **formed** shell changed.

## Any formed size

`FormedMultiblockRenderer` draws a 9-slice cuboid from `industrial_casing` (1×1 corners, repeating edges and faces). Casing and the controller use `RenderShape.INVISIBLE` while `formed` is true. Collision and I/O stay on the real blocks. Ports, windows, hatches, and the controller cube remain visible 1×1 fittings.

## Art size only

Each of the eight machines has a formed `.bbmodel` exported to `models/block/formed/*.json`. That mega-mesh overlays **only** the size used by debug place / the art board. Other legal sizes keep the 9-slice hull plus fittings. The press platen is an extra overlay.

There is no new block id for the formed look. Geometry already travelled in the controller packet NBT; the `formed` casing flag is pushed on form/unform.

## Related

- [[industrial-multiblock]]
- [[industrial-processing]]
- [[industrial-ports]]
- [[artisanal-machine-voxel-models]]
- [[alcoholic-debug-commands]]
- [[cursor-formed-hull-session]]
- [[alcoholic]]
