---
title: Emergent Quality Profile
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [ADR-037, QualityProfile, complexity cap]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/e7dd3c64-d28a-4c16-9d2a-d2dc3be2a3d2/e7dd3c64-d28a-4c16-9d2a-d2dc3be2a3d2.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/9d899962-c13d-474a-9cb7-59e0ef74ca71/9d899962-c13d-474a-9cb7-59e0ef74ca71.jsonl"
  - "C:/Users/djden/source/repos/Alcoholic/docs/adr/ADR-037-emergent-quality-profile.md"
summary: >-
  Drink quality is derived (purity, complexity, maturity, balance, defects). Executors stamp caps, not a farmable 0–100 score.
provenance:
  extracted: 0.84
  inferred: 0.14
  ambiguous: 0.02
created: 2026-09-08T20:30:00+02:00
updated: 2026-09-08T20:30:00+02:00
---

# Emergent Quality Profile

A first three-scale balance wanted each executor to write `alcoholic:quality`. That recreates a magic 0–100 score and lets waiting or speed farm a better drink. ADR-037 forbids that.

## Derived, not stored

`QualityProfile` computes purity, complexity, maturity, balance, and defects from the [[liquid-batch]] bag and [[batch-provenance]]. Ethanol is displayed separately and never enters the profile. Harvest-lot `alcoholic:quality` stays a grape/field signal, not the bottled drink score.

Processors write chemistry and provenance only. They stamp `alcoholic:complexity_cap` (merge `MIN`) and `alcoholic:purity_floor` (merge `MAX`) so industrial ceilings survive [[bottled-beverage-snapshot]] and blending.

## Three scales, one definition

`ExecutorModifiers` add `processFidelity`, `complexityCap`, and `purityFloor`. Shipped defaults:

- Artisanal: fidelity 1.00, cap 1.00, floor 0.00
- Craft: fidelity 0.94, cap 0.82, floor 0.04 — see [[craft-scale-machines]]
- Industrial: fidelity 0.70, cap 0.55, floor 0.12–0.15 — see [[industrial-processing]]

Volume and throughput stay on ADR-022 knobs. `speedModifier` finishes FERMENT / AGE / CONDITION sooner without raising the cap.

Interpretation of those axes is a second datapack graph: [[quality-operator-dag]].

## Related

- [[quality-operator-dag]]
- [[liquid-batch]]
- [[craft-scale-machines]]
- [[industrial-processing]]
- [[cursor-quality-and-craft-session]]
- [[alcoholic]]
