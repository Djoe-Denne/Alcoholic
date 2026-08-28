---
title: Vanilla XOR Create Crafts
category: concepts
tags: [minecraft, compatibility, type/concept, project/alcoholic]
aliases: [machine recipes Create, forge:conditional crafts]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/98a497ab-d495-4e83-a8cb-923fcbfe8956/98a497ab-d495-4e83-a8cb-923fcbfe8956.jsonl"
summary: >-
  Twenty-seven machine crafts are Vanilla when Create is absent, Create-shaped when it is loaded. Yeast, bottle, and shears stay Vanilla always.
provenance:
  extracted: 0.90
  inferred: 0.08
  ambiguous: 0.02
created: 2026-08-28T19:15:00+02:00
updated: 2026-08-28T19:15:00+02:00
---

# Vanilla XOR Create Crafts

Every shipped machine block must be craftable from vanilla materials. When Create is on the instance, those same machine crafts switch to Create parts. Alcoholic **process** recipes (PRESS, MILL, mash, and so on) do not change.

## Vanilla path

The 27 block/machine shaped recipes are wrapped `forge:conditional` + `forge:not` / `mod_loaded` `create`. `yeast`, `empty_bottle`, and `pruning_shears` are never wrapped. `electric_motor_ie` requires Immersive Engineering **and** the absence of Create.

## Create path

Each machine has `<id>_create.json` as `minecraft:crafting_shaped` using andesite alloy, shafts, casings, brass, cogwheels. Extra Create process recipes (millstone, crushing, grape compacting) stay as they were. See [[create-press-adapter]].

Datagen lives in `GrapeServerDataProvider`. Contract: `GeneratedResourceContractTest` (27 pairs + 3 always-Vanilla + IE + Create process extras).

## Related

- [[create-press-adapter]]
- [[native-executor-invariant]]
- [[electric-motor]]
- [[artisanal-processing]]
- [[alcoholic]]
