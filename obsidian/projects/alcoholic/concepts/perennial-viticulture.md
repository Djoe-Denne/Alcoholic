---
title: Perennial Viticulture
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [viticulture, vine lifecycle]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/bff6f9b3-354c-46b7-8b5e-c5162dc38730/bff6f9b3-354c-46b7-8b5e-c5162dc38730.jsonl"
summary: Alcoholic vines are perennial: establishment happens once, harvest never breaks the plant, and later seasons restart from dormancy.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-08-25T12:50:00+02:00
updated: 2026-08-28T19:15:00+02:00
---

# Perennial Viticulture

Phase 2 models a vineyard that can be planted, harvested, pruned, and harvested again without replanting. Pressing, fluids, and fermentation stay out of this slice.

## Domain objects

The loader-independent model includes `Vine`, `VineVariety`, `VineGrowthStage`, `VineEnvironment`, `VineHealth`, and `GrapeHarvest`. Each vine keeps at least `ageCycles`, `hasEstablished`, `lastHarvest`, `pruningLevel`, and `health`. Civil years are not counted; a completed harvest may increment `ageCycles`.

## Growth stages

First cycle:

`PLANTED` → `ESTABLISHING` → `VEGETATIVE` → `FLOWERING` → `GREEN_FRUIT` → `RIPENING` → `HARVEST_READY` → `DORMANT`

Later cycles skip establishment:

`DORMANT` → `FLOWERING` → `GREEN_FRUIT` → `RIPENING` → `HARVEST_READY` → `DORMANT`

`ESTABLISHING` and `VEGETATIVE` are first-growth only. A later `BUD_BREAK` stage was deferred.

## Harvest invariant

Harvest never destroys the vine. A `HARVEST_READY` plant drops fruit, then becomes `DORMANT` and can restart. This is an explicit test invariant from the Phase 2 brief.

## Climate and quality

Biome and climate influence what a site produces. Datapack variety profiles drive growth chance, yield, quality, sugar, and acidity. Training via [[trellis-training]] further scales yield and quality. Harvested stacks carry [[harvest-lot-metadata]] rather than baking those values into a fluid.

## Persistence

Legacy Phase 1 `age` 0–4 vines migrate into the nearest Phase 2 stage. Authoritative lifecycle lives on the vine block entity; `stage` and `trained` are synchronized render/query properties. See [[loader-independent-minecraft-architecture]] and ADR-005 in the repository.

## Related

- [[alcoholic]]
- [[trellis-training]]
- [[climbing-plant-visual]]
- [[harvest-lot-metadata]]
- [[cursor-phase-2-viticulture-session]]
- [[semantic-crop-compatibility]]
- [[forge-1.19.2-phase-2-3-verification]]
