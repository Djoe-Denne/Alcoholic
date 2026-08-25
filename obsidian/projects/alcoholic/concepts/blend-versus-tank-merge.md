---
title: Blend Versus Tank Merge
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [BLEND, PropertyMerge, artisanal blending crock]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/c2ca3b27-ad63-4be9-af24-47c49c111f2f/c2ca3b27-ad63-4be9-af24-47c49c111f2f.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06.jsonl"
summary: Tanks merge only the same liquid definition. Distinct definitions blend only through an explicit alcoholic:blend action.
provenance:
  extracted: 0.88
  inferred: 0.1
  ambiguous: 0.02
created: 2026-08-25T14:40:00+02:00
updated: 2026-08-25T18:55:00+02:00
---

# Blend Versus Tank Merge

ADR-010 allowed compatible merge of the same `LiquidDefinition` inside Alcoholic tanks. Phase 5 adds conservation under drain and an explicit blend path for distinct definitions.

## Split and merge

`LiquidBatch.split` returns extracted plus remaining. Volumes sum to the original. Property bags and [[batch-provenance]] are independent copies. `LiquidTank.drain` uses split so volume cannot be duplicated.

`merge` still requires the same definition. It fails when a property uses `IDENTICAL_OR_REJECT` and the values differ. Numeric defaults stay volume-weighted. New strategies: `SUM`, `IDENTICAL_OR_REJECT`, `COMBINE_SET`, `CUSTOM`. `CUSTOM` delegates to `LiquidProperty.aggregator()`. There is no central `switch(propertyId)`.

## Blend is a process, not a fill

`blend` is the only path that may change definition. `alcoholic:blend` runs from an explicit player action on the artisanal crock (two tanks, sneak plus empty hand). Both crock tanks must accept fill; a review found `canFillTank` limited to index 0, which blocked the second liquid and made `blend_*` recipes dead in-world. That is fixed. `LiquidTank.fill` never merges distinct definitions. The oak barrel stays a single lot.

A datapack names accepted inputs, optional minimum fractions, and an output liquid (for example a cuvée). Addons declare aggregator semantics on the property, not in the tank.

## Related

- [[liquid-batch]]
- [[batch-provenance]]
- [[artisanal-processing]]
- [[aging-process]]
- [[public-extension-api]]
- [[cursor-phase-5-aging-session]]
- [[cursor-create-independence-session]]
