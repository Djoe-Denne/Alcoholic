---
title: Alcoholic
category: project
tags: [minecraft, software-architecture, type/project, project/alcoholic]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b9ff420b-fd71-4089-83bc-cc4766880b14/b9ff420b-fd71-4089-83bc-cc4766880b14.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/bff6f9b3-354c-46b7-8b5e-c5162dc38730/bff6f9b3-354c-46b7-8b5e-c5162dc38730.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b282f8bd-e540-4028-93d1-896905419dcd/b282f8bd-e540-4028-93d1-896905419dcd.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/a6b5797c-82f8-4021-9d63-10a82fed6899/a6b5797c-82f8-4021-9d63-10a82fed6899.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/c2ca3b27-ad63-4be9-af24-47c49c111f2f/c2ca3b27-ad63-4be9-af24-47c49c111f2f.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/dc49ea79-b9e5-4902-ad7f-795f762f8f52/dc49ea79-b9e5-4902-ad7f-795f762f8f52.jsonl"
summary: Loader-independent brewing mod. Phase 7A adds generic malt/mill/mash/boil; the engine stays beverage-agnostic.
provenance:
  extracted: 0.84
  inferred: 0.14
  ambiguous: 0.02
created: 2026-08-25T09:20:00+02:00
updated: 2026-08-25T15:53:00+02:00
---

# Alcoholic

Alcoholic is a Minecraft mod for realistic brewing, winemaking, and distillation. The first supported runtime is Minecraft 1.19.2, Forge 43.5.0, Java 17. Mod id `alcoholic`, package `com.djden.alcoholic`.

## Current delivery

**Phase 1** — red and white grape content, semantic tags, Vinery-aware acquisition, architecture checks, unit tests, and GameTests.

**Phase 2** — [[perennial-viticulture]]: eight stages, harvest without destroying the vine, [[trellis-training]], climate/pruning datapacks, [[harvest-lot-metadata]], and six vine GameTests. No press, fluid, or fermentation.

**Phase 3** — [[beverage-framework]] and [[public-extension-api]]: DAG definitions, process/property registries, datapack reload, and a Java addon surface. No executable machines yet.

**Phase 4** — first operational slice: [[artisanal-processing]] plus optional [[create-press-adapter]], [[liquid-batch]] transport, and [[fermentation-physics]]. Wine grapes → must → young wine is the shipped loop. Cider remains data-only.

**Phase 5** — long-term storage: [[aging-process]], [[vessel-and-environment]], [[blend-versus-tank-merge]], [[batch-provenance]], and [[bottled-beverage-snapshot]]. Wine young → finished → bottle is the shipped loop. Whisky, beer, cider, and rum AGE graphs stay data-only fixtures. Distillation gameplay, drunkenness, and many woods stay out of scope.

**Phase 6** — [[industrial-multiblock]], [[industrial-processing]], and [[industrial-ports]]: variable-size press, fermentation vat, and passive tank. They are extra PRESS/FERMENT executors, not a second wine recipe system. Create remains pipes, belts, and shafts.

**Phase 7A** — second beverage family through generic process types: barley and hops agriculture, `MALT` / `MILL` / `MASH` / `BOIL`, malting floor, mash tun, brewing kettle, Create millstone/crushing executors. Shipped grain beer ends after generic `FERMENT`. AGE stays optional. Whisky remains a structural DAG fixture (`DISTILL` is still a stub).

The Forge artifact still embeds the inward-pointing modules. Fabric remains a future adapter.

## Key concepts

- [[loader-independent-minecraft-architecture]]
- [[semantic-crop-compatibility]]
- [[perennial-viticulture]]
- [[beverage-framework]]
- [[process-capability-graph]]
- [[liquid-batch]]
- [[artisanal-processing]]
- [[aging-process]]
- [[vessel-and-environment]]
- [[blend-versus-tank-merge]]
- [[bottled-beverage-snapshot]]
- [[industrial-multiblock]]
- [[industrial-processing]]
- [[industrial-ports]]

## Verification

- [[forge-1.19.2-phase-1-verification]]
- [[forge-1.19.2-phase-2-3-verification]]
- [[forge-1.19.2-phase-4-verification]]
- [[forge-1.19.2-phase-5-verification]]
- [[forge-1.19.2-phase-6-verification]]

## Session sources

- [[cursor-phase-1-foundation-session]]
- [[cursor-phase-2-viticulture-session]]
- [[cursor-phase-3-beverage-framework-session]]
- [[cursor-phase-4-processing-session]]
- [[cursor-phase-5-aging-session]]
- [[cursor-phase-6-industrial-session]]
