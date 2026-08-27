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
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/95c81b7c-fa88-4055-9741-14cb948964c9/95c81b7c-fa88-4055-9741-14cb948964c9.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/1ca21b6f-a077-4686-8f47-c41ee6e59fc7/1ca21b6f-a077-4686-8f47-c41ee6e59fc7.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/ede9eb58-09ac-4ccc-9fea-74a521453d14/ede9eb58-09ac-4ccc-9fea-74a521453d14.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-15-09-01a03f48-7af6-7513-a3c9-b297e81b7a96.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/d57caa3c-9de3-4ca9-8f7b-d53f8411e614/d57caa3c-9de3-4ca9-8f7b-d53f8411e614.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/d120a5cc-91ff-47b3-99d0-392583b27834/d120a5cc-91ff-47b3-99d0-392583b27834.jsonl"
summary: Loader-independent brewing mod. Grain beer on generic processes; rotary power from native engines, Create, or Crossroads.
provenance:
  extracted: 0.82
  inferred: 0.16
  ambiguous: 0.02
created: 2026-08-25T09:20:00+02:00
updated: 2026-08-27T16:20:00+02:00
---

# Alcoholic

Alcoholic is a Minecraft mod for realistic brewing, winemaking, and distillation. The first supported runtime is Minecraft 1.19.2, Forge 43.5.0, Java 17. Mod id `alcoholic`, package `com.djden.alcoholic`.

## Current delivery

**Phase 1** — red and white grape content, semantic tags, Vinery-aware acquisition, architecture checks, unit tests, and GameTests.

**Phase 2** — [[perennial-viticulture]]: eight stages, harvest without destroying the vine, [[trellis-training]], climate/pruning datapacks, [[harvest-lot-metadata]], and six vine GameTests. No press, fluid, or fermentation.

**Phase 3** — [[beverage-framework]] and [[public-extension-api]]: DAG definitions, process/property registries, datapack reload, and a Java addon surface. No executable machines yet.

**Phase 4** — first operational slice: [[artisanal-processing]] plus optional [[create-press-adapter]], [[liquid-batch]] transport, and [[fermentation-physics]]. Wine grapes → must → young wine is the shipped loop. Cider remains data-only.

**Phase 5** — long-term storage: [[aging-process]], [[vessel-and-environment]], [[blend-versus-tank-merge]], [[batch-provenance]], and [[bottled-beverage-snapshot]]. Wine young → finished → bottle is the shipped loop. Whisky, beer, cider, and rum AGE graphs stay data-only fixtures. Distillation gameplay, drunkenness, and many woods stay out of scope.

**Phase 6** — [[industrial-multiblock]], [[industrial-processing]], and [[industrial-ports]]: variable-size press, fermentation vat, and passive tank. They are extra PRESS/FERMENT executors, not a second wine recipe system. Create remains optional pipes, belts, and shafts.

**Phase 7A** — second beverage family through generic process types: barley and hops agriculture, `MALT` / `MILL` / `MASH` / `BOIL`, malting floor, mash tun, brewing kettle. See [[grain-processing]]. Official DAGs follow the [[native-executor-invariant]]: the Malt Mill plus [[mechanical-drive-port]] keep MILL playable without Create. Create millstone/crushing stay optional extras. Shipped grain beer ends after generic `FERMENT`. AGE stays optional. Whisky remains a structural DAG fixture (`DISTILL` is still a stub).

**Mechanical supplies (ADR-031)** — four ways to feed the same port: primitive combustion engine, [[electric-motor]] (generic FE; IE connector is one provider), optional Create, optional [[crossroads-rotary-adapter]]. Machines never branch on which mod is installed.

**Artisanal visuals** — Codex replaced the cube placeholders for the malting floor, primitive engine, and malt mill with Java voxel models authored in [[blockbench]]. See [[artisanal-machine-voxel-models]] and [[resource-pack-resolution-chain]].

**Recipe viewers (ADR-032)** — JEI is a Forge adapter over [[process-display-and-recipe-viewers]]. Addon processes appear without a core switch.

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
- [[grain-processing]]
- [[mechanical-drive-port]]
- [[electric-motor]]
- [[crossroads-rotary-adapter]]
- [[native-executor-invariant]]
- [[artisanal-machine-voxel-models]]
- [[resource-pack-resolution-chain]]
- [[process-display-and-recipe-viewers]]

## Verification

- [[forge-1.19.2-phase-1-verification]]
- [[forge-1.19.2-phase-2-3-verification]]
- [[forge-1.19.2-phase-4-verification]]
- [[forge-1.19.2-phase-5-verification]]
- [[forge-1.19.2-phase-6-verification]]
- [[forge-1.19.2-phase-7a-verification]]
- [[forge-1.19.2-crossroads-fe-verification]]

## Session sources

- [[cursor-phase-1-foundation-session]]
- [[cursor-phase-2-viticulture-session]]
- [[cursor-phase-3-beverage-framework-session]]
- [[cursor-phase-4-processing-session]]
- [[cursor-phase-5-aging-session]]
- [[cursor-phase-6-industrial-session]]
- [[cursor-phase-7a-grain-session]]
- [[cursor-create-independence-session]]
- [[cursor-crossroads-electric-motor-session]]
- [[codex-ajouter-modeles-3d-minecraft]]
- [[cursor-jei-display-session]]
- [[cursor-artisanal-brewery-guide-session]]
