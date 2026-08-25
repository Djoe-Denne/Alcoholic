---
title: Native Executor Invariant
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [ADR-030, Malt Mill, optional Create executors]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/95c81b7c-fa88-4055-9741-14cb948964c9/95c81b7c-fa88-4055-9741-14cb948964c9.jsonl"
summary: Official Alcoholic DAGs must ship a native executor. Create may add extra MILL and PRESS machines, never the only path.
provenance:
  extracted: 0.92
  inferred: 0.06
  ambiguous: 0.02
created: 2026-08-25T18:55:00+02:00
updated: 2026-08-25T18:55:00+02:00
---

# Native Executor Invariant

No official Alcoholic production DAG may require an executor supplied only by an optional integration. Once `PRESS`, `MILL`, `MALT`, `MASH`, `BOIL`, `FERMENT`, `AGE`, or `DISTILL` is part of shipped progression, Alcoholic ships at least one native executor. Create enhances Alcoholic; it is not required to play it. This is ADR-030. It supersedes ADR-028’s “Alcoholic has no mill” decision.

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

Create types live in `integration-create-forge-1.19.2` (heat probes, millstone/crushing adapters, kinetic port, drive probe). Root `checkArchitecture` forbids `com.simibubi.create.` inside `platform-forge-1.19.2`. The Forge root only calls `ForgeCreateIntegration.install()` / `registerIndustrial()`.

## Related

- [[grain-processing]]
- [[mechanical-drive-port]]
- [[create-press-adapter]]
- [[process-capability-graph]]
- [[loader-independent-minecraft-architecture]]
- [[industrial-ports]]
- [[cursor-create-independence-session]]
- [[cursor-phase-7a-grain-session]]
- [[forge-1.19.2-phase-7a-verification]]
