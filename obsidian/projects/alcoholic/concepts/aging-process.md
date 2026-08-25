---
title: Aging Process
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [AGE, AgingPhysics, AgingConfig]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/c2ca3b27-ad63-4be9-af24-47c49c111f2f/c2ca3b27-ad63-4be9-af24-47c49c111f2f.jsonl"
summary: alcoholic:age is optional and data-driven. The engine never injects it. Completion can rename a batch or leave identity unchanged.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-08-25T14:40:00+02:00
updated: 2026-08-25T14:40:00+02:00
---

# Aging Process

Phase 4 registered `alcoholic:age` as a UNIT stub. Phase 5 replaces that stub with a real process type that mirrors FERMENT architecturally and stays beverage-agnostic.

## Optional, never injected

A vessel ages a [[liquid-batch]] only when `ProcessRecipeResolver` finds a compatible `alcoholic:age` definition or graph node for the stored liquid. The [[process-capability-graph]] remains the authority. The engine does not add AGE because a drink “should” age.

An empty or missing config is valid. Maturity can still progress. If `output.liquid` is absent, the batch keeps its definition when aging completes. If present, completion renames the batch the same way FERMENT does.

## Physics

`AgingPhysics.step` evolves numeric properties (`alcoholic:maturity`, `alcoholic:wood_exposure`, `alcoholic:oxidation_exposure`) and flattened [[batch-provenance]] summaries. Completion is `maturity >= threshold` (default 1.0). Rate is the product of temperature-band factor, [[vessel-and-environment]] aging factor, and vessel seasoning. The step is linear in `deltaTicks`, so catch-up `step(N)` matches `N × step(1)` within floating-point error.

Temperature bands follow the FERMENT pattern: preferred runs at full rate, operating is slower, outside operating stalls. No biome IDs enter domain code.

## Resolver

`ProcessRecipeResolver` no longer hard-decodes `FermentConfig`. Each registered `ProcessType` answers `acceptsLiquid`. Empty `input_liquid` on AGE or FERMENT does not match every liquid. ^[inferred]

## Related

- [[vessel-and-environment]]
- [[batch-provenance]]
- [[fermentation-physics]]
- [[process-capability-graph]]
- [[artisanal-processing]]
- [[cursor-phase-5-aging-session]]
- [[forge-1.19.2-phase-5-verification]]
