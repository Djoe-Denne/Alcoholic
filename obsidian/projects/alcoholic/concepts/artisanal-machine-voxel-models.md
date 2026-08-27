---
title: Artisanal Machine Voxel Models
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [voxel machine models, Java block models]
sources:
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-15-09-01a03f48-7af6-7513-a3c9-b297e81b7a96.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-48-08-01a03f66-acf1-7931-95db-a9b0b9d8961f.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-56-07-01a03f6d-f9dd-71c2-8d26-3fc369bd905a.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T21-27-11-01a03f89-8bc5-7140-aa48-215bf4ba51ed_01a03f8a-6d29-7862-943b-e0e09c706834.jsonl"
summary: Three grain machines stay one-block logic but ship detailed Java voxel models authored in Blockbench, not GeckoLib.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-08-27T13:30:00+02:00
updated: 2026-08-27T16:20:00+02:00
---

# Artisanal Machine Voxel Models

`malting_floor`, `primitive_combustion_engine`, and `malt_mill` already existed as functional [[artisanal-processing]] cubes. The Codex work replaces those cubes with multi-cuboid Java 1.19.2 models that read as machines, not as painted boxes.

## What does not change

Block ids, recipes, loot, and process capabilities stay the same. The mill still needs [[mechanical-drive-port]]. The floor still executes `alcoholic:malt`. The engine still supplies rotary power. A dedicated model authoring path must not overwrite unrelated datagen output.

## Shared visual rules

- Format: Blockbench **Java Block/Item**. No Bedrock or GeckoLib deliverable, and GeckoLib is not added as a dependency.
- Author in `art/blockbench/<machine>/`. Export blockstates, block/item models, and textures into `assets/alcoholic/`.
- Face the functional side toward `-Z` unless a machine-specific page says otherwise.
- Grey silhouette first, then proportions, then UV pack, then painted atlas. Iterate with screenshots.
- Oak on later machines copies the locked engine tile. See [[resource-pack-resolution-chain]].
- The parent plan allowed visual overflow past the collision box. Later mill review pulled the lower receptacle back inside 16×16. Treat overflow as allowed until a machine page forbids it. ^[ambiguous]

## Motion without GeckoLib

The repo already had a client renderer for the artisanal press, so light client animation is compatible. The parent plan: the mill rotates only while milling; the engine rotates while lit. Parts that move should be groups from the first silhouette (shaft, flywheel, rollers).

## Per-machine pages

- [[malting-floor-visual]]
- [[primitive-combustion-engine-visual]]
- [[malt-mill-visual]]

## Related

- [[resource-pack-resolution-chain]]
- [[blockbench-java-block-workflow]]
- [[blockbench]]
- [[codex-ajouter-modeles-3d-minecraft]]
- [[grain-processing]]
- [[alcoholic]]
