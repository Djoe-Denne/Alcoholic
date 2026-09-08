---
title: Bottle Stands
category: concepts
tags: [minecraft, type/concept, project/alcoholic]
aliases: [bottle rack, bottle shelf, BottleStandStyle]
sources:
  - "C:/Users/djden/.codex/sessions/2026/09/05/rollout-2026-09-05T11-40-53-01a070e9-79ad-70d1-a5b5-171a8679adfd_01a070f1-3ca5-7dc2-be1a-ae4a2fff7bca.jsonl"
summary: >-
  Two display blocks: a lying-bottle rack and a shelf. Each block holds nine bottled snapshots and can tile with neighbours.
provenance:
  extracted: 0.82
  inferred: 0.16
  ambiguous: 0.02
created: 2026-09-08T20:30:00+02:00
updated: 2026-09-08T20:30:00+02:00
---

# Bottle Stands

Display furniture for [[bottled-beverage-snapshot]] items, not a process executor. `BottleStandStyle` is `RACK` (`bottle_rack`, bottles lying in cells) or `SHELF` (`bottle_shelf`). Each block entity owns nine one-bottle slots and accepts only `BeverageBottleItem`.

Adjacent stands tile; GameTests place 2×2 racks. They are not hollow-cuboid machines and do not form a controller. ^[inferred]

Java voxels were authored in Blockbench after the architecture landed; downsample still follows [[resource-pack-resolution-chain]].

## Related

- [[bottled-beverage-snapshot]]
- [[artisanal-machine-voxel-models]]
- [[cursor-cask-hedging-stands-session]]
- [[alcoholic]]
