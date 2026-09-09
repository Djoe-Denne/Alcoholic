---
title: Bottle Stands
category: concepts
tags: [minecraft, type/concept, project/alcoholic]
aliases: [bottle rack, bottle shelf, BottleStandStyle]
sources:
  - "C:/Users/djden/.codex/sessions/2026/09/05/rollout-2026-09-05T11-40-53-01a070e9-79ad-70d1-a5b5-171a8679adfd_01a070f1-3ca5-7dc2-be1a-ae4a2fff7bca.jsonl"
  - "C:/Users/djden/source/repos/Alcoholic/docs/audits/current-playability-audit.md"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/112827ce-4901-4cfd-8dad-1e626367ee8f/112827ce-4901-4cfd-8dad-1e626367ee8f.jsonl"
summary: >-
  Two display blocks: a lying-bottle rack and a shelf. Vanilla crafts; adjacent tiling no longer crashes.
provenance:
  extracted: 0.84
  inferred: 0.14
  ambiguous: 0.02
created: 2026-09-08T20:30:00+02:00
updated: 2026-09-09T16:00:00+02:00
---

# Bottle Stands

Display furniture for [[bottled-beverage-snapshot]] items, not a process executor. `BottleStandStyle` is `RACK` (`bottle_rack`, bottles lying in cells) or `SHELF` (`bottle_shelf`). Each block entity owns nine one-bottle slots and accepts only `BeverageBottleItem`.

Adjacent stands tile; GameTests place 2×2 racks. They are not hollow-cuboid machines and do not form a controller. `BottleStandNetwork` used to crash when tiling; that path is guarded ([[playability-audit]]).

Vanilla recipes (no Create XOR): `bottle_rack` is planks + stick + glass bottle; `bottle_shelf` is slabs + planks.

Java voxels were authored in Blockbench after the architecture landed; downsample still follows [[resource-pack-resolution-chain]].

## Related

- [[bottled-beverage-snapshot]]
- [[artisanal-machine-voxel-models]]
- [[cursor-cask-hedging-stands-session]]
- [[playability-audit]]
- [[alcoholic]]
