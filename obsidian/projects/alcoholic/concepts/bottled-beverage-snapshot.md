---
title: Bottled Beverage Snapshot
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [BeverageBottleItem, alcoholic:bottle]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/c2ca3b27-ad63-4be9-af24-47c49c111f2f/c2ca3b27-ad63-4be9-af24-47c49c111f2f.jsonl"
summary: A filled bottle stores a consumer snapshot, not runtime process state. Right-click a vessel with an empty bottle.
provenance:
  extracted: 0.9
  inferred: 0.08
  ambiguous: 0.02
created: 2026-08-25T14:40:00+02:00
updated: 2026-08-25T14:40:00+02:00
---

# Bottled Beverage Snapshot

A filled bottle is a consumer item, not a portable process vessel.

## Snapshot, not clock

`alcoholic:bottle` writes definition, ethanol, residual sugar, acidity, maturity, origin fractions, and quality. It omits yeast, last-processed time, and vessel clock. Default volume is 250 mB. There are no player potion effects.

Right-click an oak barrel, blending crock, or fermenter with `alcoholic:empty_bottle`. Re-pouring into a tank cannot restore process clock state.

## Foreign-tank loss

A stack that returns from a foreign tank without a `Version` tag shows a debug tooltip. That is the ADR-010 edge: the fluid id survives, batch properties may fall back to defaults. See [[liquid-batch]].

`/alcoholic inspect` dumps the targeted vessel or the held snapshot.

## Related

- [[liquid-batch]]
- [[batch-provenance]]
- [[artisanal-processing]]
- [[aging-process]]
- [[cursor-phase-5-aging-session]]
