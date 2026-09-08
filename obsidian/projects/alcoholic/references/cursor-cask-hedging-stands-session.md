---
title: Cursor Cask Hedging Stands Session
category: references
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [ADR-039 session, bottle stand session]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/86cc18fc-6ee4-4cf0-8dbb-32844df14b88/86cc18fc-6ee4-4cf0-8dbb-32844df14b88.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/25e2e859-8571-4ecd-8485-7291dda5b8a4/25e2e859-8571-4ecd-8485-7291dda5b8a4.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/dec23900-7d59-4696-8496-06d52c033486/dec23900-7d59-4696-8496-06d52c033486.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/09/05/rollout-2026-09-05T11-40-53-01a070e9-79ad-70d1-a5b5-171a8679adfd_01a070f1-3ca5-7dc2-be1a-ae4a2fff7bca.jsonl"
summary: >-
  One oak barrel still; imprint stains the next fill. Sickle column harvest shipped. Bottle rack and shelf display bottled snapshots.
provenance:
  extracted: 0.82
  inferred: 0.14
  ambiguous: 0.04
created: 2026-09-08T20:30:00+02:00
updated: 2026-09-08T20:30:00+02:00
---

# Cursor Cask Hedging Stands Session

Early September Cursor and Codex threads covered barrel types, vine/hop harvest, and display furniture. Wood species is still out of scope: a cherry cask would be a new profile, not a tint. Previous occupant chemistry is [[cask-imprint]].

Empty-hand harvest still takes the whole column. A sickle plus `harvestColumn` is the tool path. Create circular harvesters were requested and are not wired. See [[column-harvest]].

Codex authored [[bottle-stands]] (`bottle_rack` / `bottle_shelf`) after a Blockbench check. Each block holds nine bottles and tiles with neighbours.

Prune-from-stem applying to the root vine shipped in GameTests; treat deeper “hedging from history” as the same pruning/harvest slice unless a later ADR splits it. ^[ambiguous]

## Distilled pages

- [[cask-imprint]]
- [[column-harvest]]
- [[bottle-stands]]
- [[vessel-and-environment]]

## Related

- [[aging-process]]
- [[perennial-viticulture]]
- [[alcoholic]]
