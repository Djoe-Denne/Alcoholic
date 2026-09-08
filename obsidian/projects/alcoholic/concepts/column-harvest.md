---
title: Column Harvest
category: concepts
tags: [minecraft, type/concept, project/alcoholic]
aliases: [sickle, vine harvest, hop column]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/25e2e859-8571-4ecd-8485-7291dda5b8a4/25e2e859-8571-4ecd-8485-7291dda5b8a4.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/dec23900-7d59-4696-8496-06d52c033486/dec23900-7d59-4696-8496-06d52c033486.jsonl"
summary: >-
  Bare-hand harvest still takes the whole vine or hop column. A sickle harvests the column with Fortune. Create circular harvesters are not wired.
provenance:
  extracted: 0.8
  inferred: 0.12
  ambiguous: 0.08
created: 2026-09-08T20:30:00+02:00
updated: 2026-09-08T20:30:00+02:00
---

# Column Harvest

Right-click on a ripe vine or hop bine used to take the entire column (stump through canopy) in one action. That is still the empty-hand path. [[perennial-viticulture]] harvest still never breaks the plant.

## Sickle

`SickleItem` is an iron hoe-tier digger so Fortune applies. Holding it and using a vine or hop bine runs `ColumnHarvest` / `harvestColumn` from stump to canopy. Pruning shears stay a separate tool (`LIGHT` / `BALANCED` / `SEVERE`); prune from a stem segment applies to the root vine.

Create's recommended circular-harvester pattern was requested for automatic pickup. There is no Create harvester adapter in the repo yet. ^[ambiguous]

## Related

- [[perennial-viticulture]]
- [[wild-hops]]
- [[climbing-plant-visual]]
- [[trellis-training]]
- [[cursor-cask-hedging-stands-session]]
- [[alcoholic]]
