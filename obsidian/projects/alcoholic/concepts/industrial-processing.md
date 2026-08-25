---
title: Industrial Processing
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [industrial press, industrial vat, industrial tank]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/dc49ea79-b9e5-4902-ad7f-795f762f8f52/dc49ea79-b9e5-4902-ad7f-795f762f8f52.jsonl"
summary: Industrial machines are extra PRESS and FERMENT executors plus a passive tank. Create remains the transport layer.
provenance:
  extracted: 0.9
  inferred: 0.1
  ambiguous: 0.0
created: 2026-08-25T15:30:00+02:00
updated: 2026-08-25T16:01:00+02:00
---

# Industrial Processing

Industrial machines are additional executors for existing process types. They do not add industrial wine recipes.

```
PRESS
 ├─ artisanal
 ├─ Create
 └─ industrial

FERMENT
 ├─ artisanal
 └─ industrial

STORAGE
 └─ passive tank
```

The wine DAG is unchanged: grapes → PRESS → must → FERMENT → young wine → AGE → wine. Artisanal press, fermenter, oak barrel, and Create Mechanical Press stay valid.

## Industrial press

The press consumes Create kinetic power, aggregates large homogeneous batches, and reuses generic PRESS outputs and byproducts. Executor modifiers improve throughput and yield without changing recipe semantics.

Its crush-zone easter egg is active only during the compression stroke; incidental edge contact is safe.

## Industrial fermentation vat

The vat executes the same FERMENT process as the artisanal fermenter, with larger volume and greater thermal stability. One vat holds one [[liquid-batch]], and progress scales with elapsed time rather than per-unit simulation.

## Industrial storage tank

The tank is a passive vessel, not a process executor. It preserves batch metadata and exposes the same [[industrial-ports]]. Storage never implies FERMENT, AGE, or BLEND.

## Create

Alcoholic does not add pipes, pumps, belts, or shafts. The intended factory is vineyard → belts → industrial press → pipes → tank → pipes → vat → pipes → existing barrel aging. See [[create-press-adapter]] for the separate Mechanical Press + Basin compacting path.

## Related

- [[industrial-multiblock]]
- [[industrial-ports]]
- [[artisanal-processing]]
- [[create-press-adapter]]
- [[fermentation-physics]]
- [[cursor-phase-6-industrial-session]]
- [[forge-1.19.2-phase-6-verification]]
