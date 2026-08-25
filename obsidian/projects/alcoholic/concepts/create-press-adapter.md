---
title: Create Press Adapter
category: concepts
tags: [minecraft, compatibility, type/concept, project/alcoholic]
aliases: [CreatePressRecipeTranslator, create compacting]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/a6b5797c-82f8-4021-9d63-10a82fed6899/a6b5797c-82f8-4021-9d63-10a82fed6899.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/dc49ea79-b9e5-4902-ad7f-795f762f8f52/dc49ea79-b9e5-4902-ad7f-795f762f8f52.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/95c81b7c-fa88-4055-9741-14cb948964c9/95c81b7c-fa88-4055-9741-14cb948964c9.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06.jsonl"
summary: Optional Create PRESS and MILL recipes live in integration-create. They enhance native executors; they are never the only official path.
provenance:
  extracted: 0.88
  inferred: 0.1
  ambiguous: 0.02
created: 2026-08-25T14:10:00+02:00
updated: 2026-08-25T18:55:00+02:00
---

# Create Press Adapter

Create remains an optional first-class integration. It is another executor of `alcoholic:press` and `alcoholic:mill`, not a recipe target hardcoded in beverage JSON. That continues ADR-003, ADR-008, and the [[native-executor-invariant]]. Create types belong in `integration-create-forge-1.19.2`, not `platform-forge-1.19.2`.

## Why compacting

Create's Mechanical Press over a Basin consumes items and emits fluids through `create:compacting`. `create:pressing` is the wrong recipe type for this loop. A pure-Java translator in `integration-create` (`CreatePressRecipeTranslator`) turns PRESS definitions marked `create_compatible` into compacting recipes. Domain and application still do not import Create types.

## Conditional data

A bare `create:compacting` file crashes `RecipeManager` when Create is absent. Generated recipes wrap the compacting payload in `forge:conditional` with `forge:mod_loaded` / `create`. Datagen writes `press_red_grapes_create.json` and `press_white_grapes_create.json`.

## What Create does not preserve

Compacting outputs the registered Forge fluid plus a required solid byproduct. Recipe JSON does not carry harvest-lot NBT, so Create pressing produces default-property must. Agricultural fidelity stays on the [[artisanal-processing]] press.

Create pipes, pumps, and tanks attach to standard `IFluidHandler`. Alcoholic does not own a pipe network. GameTests that place `create:fluid_tank` run only with `-PwithCreate=true`; without Create they succeed as no-ops.

Phase 6 adds a separate Create surface: [[industrial-ports|kinetic ports]] drive industrial machines when Create shafts are present. A [[mechanical-drive-port|primitive engine]] can power the same port without Create.

## Optional MILL recipes

Definitions marked `create_compatible` also generate `create:milling` and `create:crushing` via `CreateMillRecipeTranslator`. A runtime bridge copies malt NBT onto Create mill output so kiln properties survive. Official [[grain-processing]] uses the Malt Mill; these recipes are extras.

## Related

- [[artisanal-processing]]
- [[liquid-batch]]
- [[process-capability-graph]]
- [[loader-independent-minecraft-architecture]]
- [[semantic-crop-compatibility]]
- [[industrial-ports]]
- [[industrial-processing]]
- [[grain-processing]]
- [[native-executor-invariant]]
- [[mechanical-drive-port]]
- [[cursor-phase-4-processing-session]]
- [[cursor-phase-6-industrial-session]]
- [[cursor-phase-7a-grain-session]]
- [[cursor-create-independence-session]]
- [[forge-1.19.2-phase-4-verification]]
- [[forge-1.19.2-phase-6-verification]]
- [[forge-1.19.2-phase-7a-verification]]
