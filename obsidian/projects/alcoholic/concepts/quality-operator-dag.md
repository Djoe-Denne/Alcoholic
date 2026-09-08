---
title: Quality Operator DAG
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [ADR-038, QualityOperator, quality datapack]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/e7dd3c64-d28a-4c16-9d2a-d2dc3be2a3d2/e7dd3c64-d28a-4c16-9d2a-d2dc3be2a3d2.jsonl"
  - "C:/Users/djden/source/repos/Alcoholic/docs/adr/ADR-038-quality-operator-dag.md"
summary: >-
  Quality is a second composition DAG. Java registers operators; datapacks wire graphs per beverage. Ethanol is never an input.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-09-08T20:30:00+02:00
updated: 2026-09-08T20:30:00+02:00
---

# Quality Operator DAG

Folding wine and beer chemistry into one Java `QualityProfile.derive` recreated drink-family logic the production DAG had already eliminated. ADR-038 splits interpretation into the same composition pattern as [[process-capability-graph]].

## Two graphs

The production DAG still writes chemistry and stamps `complexity_cap` / `purity_floor` ([[emergent-quality-profile]]). A second graph evaluates a [[liquid-batch]] snapshot (bottle, blend, inspect) without replaying production.

- Java registers `QualityOperator` primitives on `AlcoholicApi` (frozen with processes and properties).
- Datapacks live under `data/<ns>/alcoholic/quality/*.json`.
- A beverage may name `"quality": "alcoholic:wine"`. Omitted means `alcoholic:generic`.
- Resolution: beverage identity, then `baseLiquid` as beverage or graph id, then generic.

Wine and beer share FERMENT but interpret different axes. `roast_intensity` is a defect on wine and a flavour axis on spirit/beer/generic ([[cask-imprint]]).

Shipped operators include `harvest_complexity`, `distance_balance`, `weighted_present`, `oxygen_curve`, `wood_sweet_spot`, `aging_maturity`, `stress`, `cap_floor`, and `fold_summary`.

`#alcoholic:yeast` remains a FERMENT ingredient tag ([[fermentation-physics]]); it is not a quality operator. ^[inferred]

## Related

- [[emergent-quality-profile]]
- [[beverage-framework]]
- [[public-extension-api]]
- [[cask-imprint]]
- [[cursor-quality-and-craft-session]]
- [[alcoholic]]
