---
title: Climbing Plant Visual
category: concepts
tags: [minecraft, type/concept, project/alcoholic]
aliases: [vine trunk, hop segment, feuillage croisé]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/6d20de27-6f1a-4557-95d8-aeccb17a8f16/6d20de27-6f1a-4557-95d8-aeccb17a8f16.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/28/rollout-2026-08-28T10-21-04-01a04775-4a1a-7b80-807e-9819ea5e87b4.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/48cf13a3-9ec8-4348-b31e-37330cedae92/48cf13a3-9ec8-4348-b31e-37330cedae92.jsonl"
summary: >-
  Crossed foliage every storey. A wood trunk appears only when the column is taller than one block. It is the vine itself, not a trellis stake.
provenance:
  extracted: 0.84
  inferred: 0.14
  ambiguous: 0.02
created: 2026-08-28T19:15:00+02:00
updated: 2026-08-28T19:15:00+02:00
---

# Climbing Plant Visual

Cultivated red/white vines and hop bines must read as a plant, not as a stick in the middle of two leaves.

## Crossed foliage

Every storey uses crossed leaf planes. The old single central stem model was rejected: it looked like a pole.

## Trunk, not stake

The vertical wood is the **trunk of the vine** (or hop bine). It is not a [[trellis-training]] post. Do not texture it as `vineyard_post`.

The trunk appears only when the plant is taller than one block, including the top canopy. A one-block plant (untrained, or trained without an extended stem) is foliage only. Hop `single` never shows a trunk.

## Hop segments

Hop bines gained `segment=single|bottom|middle|top`, independent of `age=0..2`. Growth and neighbour add/remove recompute the segment. Max height, survival, and harvest stay unchanged.

Twelve `age × segment` variants: `single` keeps the previous look; other segments add a narrow continuous central stem reused from existing plant UVs. No new plant bitmaps were required for that stem. ^[extracted]

Wild hops are a separate block; see [[wild-hops]].

## Existing columns

A JAR that already contains `SEGMENT` will not restyle plants placed before the update. Break/replace the top block (or replant) so the state refreshes. A one-block plant looks unchanged on purpose.

## Related

- [[perennial-viticulture]]
- [[trellis-training]]
- [[grain-processing]]
- [[wild-hops]]
- [[cursor-survival-plants-session]]
- [[alcoholic]]
