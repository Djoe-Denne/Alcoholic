---
title: Resource Pack Resolution Chain
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [BDcraft atlas, 512 master texture, Alcoholic resourcepacks]
sources:
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-56-07-01a03f6d-f9dd-71c2-8d26-3fc369bd905a.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-48-08-01a03f66-acf1-7931-95db-a9b0b9d8961f.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T21-27-11-01a03f89-8bc5-7140-aa48-215bf4ba51ed_01a03f8a-6d29-7862-943b-e0e09c706834.jsonl"
summary: Paint a 512×512 BDcraft-like master, downsample each size from that master, and ship a mid-res atlas as the mod default.
provenance:
  extracted: 0.84
  inferred: 0.1
  ambiguous: 0.06
created: 2026-08-27T13:30:00+02:00
updated: 2026-08-27T13:30:00+02:00
---

# Resource Pack Resolution Chain

Machine textures are no longer vanilla-noisy 16×16 cubes. Codex locked a **master-first** pipeline after the user rejected large Minecraft pixels in favor of a painted BDcraft-like look.

## Master

Paint once at **512×512** in Blockbench. Stop for user validation before any downsample or runtime export. Archive the master with the `.bbmodel` and, for the engine, under `art/blockbench/primitive_combustion_engine/textures/master-512/` with SHA-256 sums.

Style: comic / semi-realistic materials — soft gradients, rivets, wood grain, copper warmth, metal bevels. No harsh per-pixel noise and no dirty 1px grid on every face.

## Downsample rule

Every smaller atlas comes **directly from the 512 master**, never from a previous reduction. That avoids stacked blur.

Existing pack directories under `resourcepacks/`:

- `Alcoholic-16x`, `Alcoholic-32x`, `Alcoholic-64x`, `Alcoholic-128x`, `Alcoholic-256x`, `Alcoholic-512x`

Each directory is a standalone Java 1.19.2 resource pack. `resourcepacks/README.md` currently documents the engine packs and says the 64×64 atlas ships in the mod.

## Default resolution

For a 4-column material atlas, atlas size maps to per-material pixels:

| Atlas | Material region |
|---:|---:|
| 16×16 | 4×4 |
| 32×32 | 8×8 |
| 64×64 | 16×16 |
| 128×128 | 32×32 |
| 512×512 | 128×128 |

**64×64** is the size that gives each material a vanilla 16×16 worth of pixels. The engine and malting-floor forks settled on 64 as the mod default after briefly shipping 16. The mill fork later asked for **32×32** as its default. Treat 64 as the documented engine/floor default and 32 as the mill fork's last instruction until the repo is unified. ^[ambiguous]

## Related

- [[artisanal-machine-voxel-models]]
- [[primitive-combustion-engine-visual]]
- [[malting-floor-visual]]
- [[malt-mill-visual]]
- [[blockbench-java-block-workflow]]
- [[codex-ajouter-modeles-3d-minecraft]]
