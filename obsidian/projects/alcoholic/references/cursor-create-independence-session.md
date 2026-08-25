---
title: Cursor Create Independence Session
category: references
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [architecture review session, red-team Phase 7A]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/90c9366f-54dc-42d2-a75c-8cd21aeccd26/90c9366f-54dc-42d2-a75c-8cd21aeccd26.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/1669d4f7-1702-43e2-af12-4c03301d7b17/1669d4f7-1702-43e2-af12-4c03301d7b17.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/14805ad4-dec0-4781-92a1-c718c5113dde/14805ad4-dec0-4781-92a1-c718c5113dde.jsonl"
summary: Red-team reviews of Phase 7A, native mill, then compile-time stripping of debug kinetic NBT.
provenance:
  extracted: 0.85
  inferred: 0.11
  ambiguous: 0.04
created: 2026-08-25T18:55:00+02:00
updated: 2026-08-25T20:05:00+02:00
---

# Cursor Create Independence Session

Three chats after the last wiki sync reviewed uncommitted Phase 7A work, then removed Create as a required production dependency.

## Review mandate

Architecture and maintainability first, not style. A second angle covered testability, observability, error handling, determinism, and multi-loader cost. Early verdicts treated the tree as **not commit-ready**: unit tests were green but missed several boundary bugs.

## P0s that were fixed

An unbound industrial fluid port could accept fill and swallow liquid. Isolated ports must reject fill until bound.

The blending crock exposed two tanks but `canFillTank` only allowed index 0. Bucket, pipe, and click never reached `second`, so `blend()` and `blend_*` recipes were dead in-world. See [[blend-versus-tank-merge]].

## P1s that were fixed (no gameplay redesign)

- Industrial tick dispatches by `ProcessType` (`IndustrialRuntime.strategy`), not `MachineKind`.
- `ProcessRuntime.executor(ResourceId)` creates executors on demand so an addon type does not need a new field.
- MALT heat through `HeatSources`; humidity from biome (`moisture_requirement` 0.4). Unit tests without a world keep humidity 1.0.
- `trySet` refuses overflow; `set` may widen capacity. Industrial load resizes **before** NBT. Forge caps are invalidated. Malt `ProcessId` is fail-soft.
- Create-only MILL was deferred by the user, then handled as its own refactor.

P2s that would have changed gameplay were left alone.

## Create independence refactor

The user then required Alcoholic to be fully playable without Create. ADR-030 records the [[native-executor-invariant]]. The session added [[mechanical-drive-port]], the primitive combustion engine, and the Malt Mill. Create types moved into `integration-create-forge-1.19.2`. `checkArchitecture` now forbids Create imports in `platform-forge-1.19.2`.

The malting floor stopped executing MILL.

## Verification in this session

`checkArchitecture`, `checkBeverageFrameworkPurity`, Forge compile, and unit tests (`domain`, `application`, `minecraft-common`, `integration-create`) passed. Grain GameTests for the mill and engine were added; a full `runGameTestServer` after the mechanical cut was not closed in this chat. ^[ambiguous]

## Follow-up: debug kinetic NBT

A later red-team pass on uncommitted local changes found no critical or high issues. The remaining medium finding was persisted `Rpm` / `DebugRpm` on kinetic ports. The fix is compile-time: Gradle writes `AlcoholicDebug.ENABLED`; `javac` strips save and load when false. `javap` confirmed the keys are gone from published bytecode. See [[mechanical-drive-port#Debug RPM is compile-time]].

## Distilled pages

- [[native-executor-invariant]]
- [[mechanical-drive-port]]
- [[grain-processing]]
- [[industrial-ports]]
- [[blend-versus-tank-merge]]
- [[forge-1.19.2-phase-7a-verification]]

Later supplies (FE motor, Crossroads axle) are [[cursor-crossroads-electric-motor-session]].

## Related

- [[cursor-phase-7a-grain-session]]
- [[cursor-phase-6-industrial-session]]
- [[loader-independent-minecraft-architecture]]
- [[create-press-adapter]]
