---
title: Cursor Phase 3 Beverage Framework Session
category: references
tags: [minecraft, software-architecture, testing, project/alcoholic]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b282f8bd-e540-4028-93d1-896905419dcd/b282f8bd-e540-4028-93d1-896905419dcd.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b282f8bd-e540-4028-93d1-896905419dcd/subagents/5362e3cf-53fc-4eff-a34f-7cf2342088a9.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b282f8bd-e540-4028-93d1-896905419dcd/subagents/76fb1a5f-75ec-4a6a-9436-6a55a7cd0256.jsonl"
summary: Cursor session that designed and implemented a data-driven beverage engine and versioned public API before any press or fermenter.
provenance:
  extracted: 0.94
  inferred: 0.06
  ambiguous: 0.0
created: 2026-08-25T12:50:00+02:00
updated: 2026-08-25T12:50:00+02:00
---

# Cursor Phase 3 Beverage Framework Session

The user required this plumbing before coding a wine press, otherwise `WineService` and similar types would spread. The core must not know a finite drink list. Adding cider later must not edit existing engine classes.

## Decisions locked in the session

- Keep namespace `alcoholic:` and module `alcoholic-api`; `distillery:*` remains conceptual.
- Model v1 as an explicit DAG with named nodes and ports, not a strict linear list.
- Public codecs stay pure Java 17 `DataNode` / `DataCodec`. Mojang `Codec` is not the addon contract.
- Respect ADR-001, ADR-003, ADR-004, and ADR-005: inward dependencies, no `FluidStack` in domain, no speculative Create executor.

## Implementation outcome

Public API, domain graph, catalogs, datapack reload, builtin process and property registrations, six data-only fixtures plus a rice-polishing addon test, purity Gradle check, ADR-006, and addon/datapack docs. Java 17 forced `instanceof` instead of pattern switches. `clean build` and architecture checks passed; two `runData` runs left generated content deterministic aside from Minecraft `.cache` files.

## Distilled pages

- [[beverage-framework]]
- [[process-capability-graph]]
- [[public-extension-api]]
- [[alcoholic]]
- [[forge-1.19.2-phase-2-3-verification]]
- [[cursor-phase-4-processing-session]]
