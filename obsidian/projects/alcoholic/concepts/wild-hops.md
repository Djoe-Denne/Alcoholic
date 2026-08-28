---
title: Wild Hops
category: concepts
tags: [minecraft, type/concept, project/alcoholic]
aliases: [alcoholic:wild_hops, hop rhizome, houblon sauvage]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/a30f841a-d551-4974-a9fe-d7eb5c6d8440/a30f841a-d551-4974-a9fe-d7eb5c6d8440.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/27/rollout-2026-08-27T23-08-36-01a0450d-a29e-78a3-b545-9a95ead96c0d.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/5a877b5d-04a2-4caf-bd44-f9114c295ec6/5a877b5d-04a2-4caf-bd44-f9114c295ec6.jsonl"
summary: >-
  Survival bootstrap for hops: a worldgen bush with no BlockItem. Breaking it drops one rhizome and one hop cone.
provenance:
  extracted: 0.88
  inferred: 0.10
  ambiguous: 0.02
created: 2026-08-28T19:15:00+02:00
updated: 2026-08-28T19:15:00+02:00
---

# Wild Hops

A player with an empty inventory and no commands must still find a first hop. Cultivated bines still require a trellis.

## Block

`alcoholic:wild_hops` lives in `minecraft-common` as `WildHopsBlock`. It has **no BlockItem** and no Forge-only type. There is no `/give` for the bush.

Worldgen follows barley (rarer, different biomes): forest, flower forest, birch forest, taiga, river. Generation is skipped when `brewery:hops` is present.

Breaking the bush drops **1 rhizome + 1 hop**. Plant the rhizome under wire as before.

Place for tests:

```
/setblock ~ ~-1 ~ minecraft:grass_block
/setblock ~ ~ ~ alcoholic:wild_hops
```

The block dies unless grass, dirt, farmland, or coarse dirt is underneath.

## Texture

The model uses `alcoholic:block/wild_hops`. The 16× PNG must live **in the mod jar**, not only in optional resource packs. A missing PNG shows magenta/black and does not fall back. Pick-block shows the rhizome overlay; that is expected.

Wild hops later gained the same two-plane + central stem volume as cultivated columns. See [[climbing-plant-visual]] and [[curseforge-create2-deploy]].

## Related

- [[grain-processing]]
- [[climbing-plant-visual]]
- [[semantic-crop-compatibility]]
- [[cursor-survival-plants-session]]
- [[alcoholic]]
