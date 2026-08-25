---
title: Harvest Lot Metadata
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [harvest lot, AlcoholicHarvestLot]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/bff6f9b3-354c-46b7-8b5e-c5162dc38730/bff6f9b3-354c-46b7-8b5e-c5162dc38730.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b282f8bd-e540-4028-93d1-896905419dcd/b282f8bd-e540-4028-93d1-896905419dcd.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/a6b5797c-82f8-4021-9d63-10a82fed6899/a6b5797c-82f8-4021-9d63-10a82fed6899.jsonl"
summary: Harvested grapes carry versioned item NBT. PRESS copies quality, sugar, acidity, and variety onto the produced LiquidBatch.
provenance:
  extracted: 0.9
  inferred: 0.1
  ambiguous: 0.0
created: 2026-08-25T12:50:00+02:00
updated: 2026-08-25T14:10:00+02:00
---

# Harvest Lot Metadata

Phase 2 persists harvest outcomes on the item stack so later processing can read agricultural history without a Minecraft fluid.

## Compound

Harvested grape stacks carry `AlcoholicHarvestLot`. Version 2 stores quality, sugar, and acidity as integers from 0 through 1000 and exposes them as doubles from 0 through 1. Version 1 double-valued compounds remain readable for migration.

The payload is item-agnostic: it does not depend on whether the concrete grape item is Alcoholic or Vinery.

## Boundary with liquids

This metadata stays agricultural on the item. It is not itself a [[liquid-batch]] and must not become a Forge `FluidStack`. [[perennial-viticulture]] remains the upstream crop system.

Phase 4 PRESS copies relevant lot fields (quality, sugar, acidity, variety) onto the produced must through a domain/application transfer, not through block-entity branches. Distinct harvests must not collapse into identical must. Create compacting does not carry this NBT; default definition properties apply there. See [[create-press-adapter]].

Tooltips localize qualitative bands for quality, sugar, and acidity.

## Related

- [[perennial-viticulture]]
- [[semantic-crop-compatibility]]
- [[liquid-batch]]
- [[artisanal-processing]]
- [[alcoholic]]
- [[beverage-framework]]
- [[cursor-phase-2-viticulture-session]]
- [[cursor-phase-4-processing-session]]
- [[forge-1.19.2-phase-2-3-verification]]
