---
title: Cursor Survival Plants Session
category: references
tags: [minecraft, type/concept, project/alcoholic]
aliases: [wild hops session, vine trunk session]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/a30f841a-d551-4974-a9fe-d7eb5c6d8440/a30f841a-d551-4974-a9fe-d7eb5c6d8440.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/6d20de27-6f1a-4557-95d8-aeccb17a8f16/6d20de27-6f1a-4557-95d8-aeccb17a8f16.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/28/rollout-2026-08-28T10-21-04-01a04775-4a1a-7b80-807e-9819ea5e87b4.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/a7306cd0-8e92-41f3-8d5b-caf85717ddcb/a7306cd0-8e92-41f3-8d5b-caf85717ddcb.jsonl"
summary: >-
  Survival wild hops, then crossed foliage plus a height-gated wood trunk. End post and trellis wire joined the 512 chain.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-08-28T19:15:00+02:00
updated: 2026-08-28T19:15:00+02:00
---

# Cursor Survival Plants Session

Wiki-query on hops/barley/malt used the **Alcoholic** vault (`obsidian/`), not `~/.obsidian-wiki/config` (that file still points at texturetonevariations).

## Survival gap

Hops had no vanilla start. [[wild-hops]] (`WildHopsBlock`, no BlockItem) worldgens in selected biomes and drops rhizome + cone. Cultivated bines still need a wire.

## Visual correction

The middle “stick” was replaced by crossed leaves. The user then said the wood is the **trunk**, not a tutor. It must not appear on a one-block plant. Hop `segment` and vine `extended` models share a continuous central stem. See [[climbing-plant-visual]].

A “nothing changed in game” report after deploy was a **one-block plant** plus stale columns, not a bad copy: the instance jar matched the Forge build bit-for-bit.

## Vineyard 512

`end_post` and `trellis_wire` had only 16×16. They now have masters in `Alcoholic-512x` and downsample through `PLANT_TEXTURES`. Prompts: `art/prompts/items-plants-vineyard-512.md`.

## Distilled pages

- [[wild-hops]]
- [[climbing-plant-visual]]
- [[resource-pack-resolution-chain]]

## Related

- [[perennial-viticulture]]
- [[trellis-training]]
- [[grain-processing]]
- [[curseforge-create2-deploy]]
- [[alcoholic]]
