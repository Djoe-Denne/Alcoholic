---
title: Native Executor Invariant
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [ADR-030, Malt Mill, optional Create executors]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/95c81b7c-fa88-4055-9741-14cb948964c9/95c81b7c-fa88-4055-9741-14cb948964c9.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/1ca21b6f-a077-4686-8f47-c41ee6e59fc7/1ca21b6f-a077-4686-8f47-c41ee6e59fc7.jsonl"
summary: Official DAGs ship a native executor. Create, Crossroads, and IE may add supplies or extra machines, never the only path.
provenance:
  extracted: 0.90
  inferred: 0.08
  ambiguous: 0.02
created: 2026-08-25T18:55:00+02:00
updated: 2026-08-25T20:05:00+02:00
---

# Native Executor Invariant

No official Alcoholic production DAG may require an executor supplied only by an optional integration. Once `PRESS`, `MILL`, `MALT`, `MASH`, `BOIL`, `FERMENT`, `AGE`, or `DISTILL` is part of shipped progression, Alcoholic ships at least one native executor. Create, Crossroads, and Immersive Engineering enhance Alcoholic; they are not required to play it. This is ADR-030, extended by ADR-031. It supersedes ADR-028’s “Alcoholic has no mill” decision.

The [[electric-motor]] is native content (generic FE). It is not an IE machine. Crossroads and Create only translate foreign rotary networks into [[mechanical-drive-port]].

## MILL executors

```text
MILL
 ├─ Alcoholic Malt Mill
 ├─ Create Millstone          [optional]
 └─ Create Crushing Wheels    [optional]
```

`alcoholic:malt_mill` executes generic `MILL`. It stays enabled when Create is present. Throughput differences use `ExecutorModifiers.maltMill()` or Create recipe times, not duplicated process definitions. Definitions marked `create_compatible` still generate `create:milling` and `create:crushing` through `CreateMillRecipeTranslator` in `integration-create`.

Phase 7A first used the malting floor as a MILL fallback and treated Create as the intended mill. That left official grain progression unplayable without Create. The Malt Mill plus [[mechanical-drive-port]] closed that gap. The floor is `MALT`-only again.

## Create isolation

Create types live in `integration-create-forge-1.19.2`. Crossroads types live in `integration-crossroads-1.19.2`. Root `checkArchitecture` forbids `com.simibubi.create.` and `com.Da_Technomancer` in core modules and in `platform-forge-1.19.2`. The Forge root only calls install hooks. Machines must not contain `if (createInstalled)` / `if (crossroadsInstalled)` / `if (immersiveEngineeringInstalled)`.

## Related

- [[grain-processing]]
- [[mechanical-drive-port]]
- [[electric-motor]]
- [[crossroads-rotary-adapter]]
- [[create-press-adapter]]
- [[process-capability-graph]]
- [[loader-independent-minecraft-architecture]]
- [[industrial-ports]]
- [[cursor-create-independence-session]]
- [[cursor-crossroads-electric-motor-session]]
- [[cursor-phase-7a-grain-session]]
- [[forge-1.19.2-phase-7a-verification]]
- [[forge-1.19.2-crossroads-fe-verification]]
