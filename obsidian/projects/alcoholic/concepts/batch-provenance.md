---
title: Batch Provenance
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [BatchProvenance, origin composition]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/c2ca3b27-ad63-4be9-af24-47c49c111f2f/c2ca3b27-ad63-4be9-af24-47c49c111f2f.jsonl"
summary: Flattened origin and blend fraction maps plus compact summaries. Merge renormalizes. No recursive parent tree.
provenance:
  extracted: 0.9
  inferred: 0.08
  ambiguous: 0.02
created: 2026-08-25T14:40:00+02:00
updated: 2026-08-25T14:40:00+02:00
---

# Batch Provenance

A [[liquid-batch]] carries a compact history snapshot so [[blend-versus-tank-merge]] and inspect can show origins without a tick journal.

## Flattened maps

- `originComposition` — ingredient or variety fractions
- `blendComposition` — liquid-definition fractions
- summaries — fermentation stress, total aging time, wood exposure, oxidation exposure

Each merge or blend flattens. At most 16 entries per map. Fractions below 0.5% are dropped. Remaining weights renormalize to 1.0. There is no recursive parent pointer into a previous batch.

`totalAgingTime` is a volume-weighted summary, not a naive average of ages used as a merge strategy for `maturity`. Maturity itself stays a volume-weighted degree. ^[inferred]

## Persistence

`LiquidBatchNbt` version 2 stores the snapshot. Version 1 migrates to empty provenance. Unknown or malformed tags become `Optional.empty()` with a debug log, not silent corruption.

PRESS may seed origin from `alcoholic:variety` when that string parses as a `ResourceId`.

## Related

- [[liquid-batch]]
- [[blend-versus-tank-merge]]
- [[aging-process]]
- [[bottled-beverage-snapshot]]
- [[artisanal-processing]]
- [[beverage-framework]]
- [[cursor-phase-5-aging-session]]
