---
title: Industrial Multiblock
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [hollow cuboid, multiblock framework]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/dc49ea79-b9e5-4902-ad7f-795f762f8f52/dc49ea79-b9e5-4902-ad7f-795f762f8f52.jsonl"
summary: Variable-size hollow cuboid machines. Capacity is interior volume. State lives on the controller.
provenance:
  extracted: 0.9
  inferred: 0.1
  ambiguous: 0.0
created: 2026-08-25T15:30:00+02:00
updated: 2026-08-25T16:01:00+02:00
---

# Industrial Multiblock

Phase 6 adds a reusable hollow-cuboid family, not beverage-specific plants. Data-driven definitions describe size constraints, allowed shell parts, required ports, and capacity rules. See [[industrial-processing]] and [[industrial-ports]].

## Exterior versus interior

The validator walks a connected shell, then measures the bounding box. Only empty cells strictly inside that box count as volume. Casing, windows, hatches, ports, and the controller never add capacity.

## Validation

Structural changes invalidate a cached formation state; periodic rechecks are only a fallback. An unloaded chunk is `INCOMPLETE`, not `INVALID`, so cross-chunk machines pause instead of being torn down.

## State

One controller owns the [[liquid-batch]], inventory, process clocks, and geometry. [[industrial-ports]] are views. An invalid shell pauses I/O without losing contents. A structure cannot reform at a capacity smaller than its stored volume.

## Extension

Semantic casing tags allow compatible materials without framework changes. Future beer machinery adds definitions and executors; the validator never learns beverage-specific rules.

## Related

- [[industrial-processing]]
- [[industrial-ports]]
- [[process-capability-graph]]
- [[liquid-batch]]
- [[public-extension-api]]
- [[cursor-phase-6-industrial-session]]
- [[forge-1.19.2-phase-6-verification]]
