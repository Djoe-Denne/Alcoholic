---
title: Create Press Adapter
category: concepts
tags: [minecraft, compatibility, type/concept, project/alcoholic]
aliases: [CreatePressRecipeTranslator, create compacting]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/a6b5797c-82f8-4021-9d63-10a82fed6899/a6b5797c-82f8-4021-9d63-10a82fed6899.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/dc49ea79-b9e5-4902-ad7f-795f762f8f52/dc49ea79-b9e5-4902-ad7f-795f762f8f52.jsonl"
summary: Optional Create PRESS uses create:compacting (Press + Basin), generated in integration-create. Recipes are forge:conditional so RecipeManager stays valid without Create.
provenance:
  extracted: 0.9
  inferred: 0.08
  ambiguous: 0.02
created: 2026-08-25T14:10:00+02:00
updated: 2026-08-25T16:01:00+02:00
---

# Create Press Adapter

Create remains an optional first-class integration. It is another executor of `alcoholic:press`, not a recipe target hardcoded in beverage JSON. That continues ADR-003 and ADR-008.

## Why compacting

Create's Mechanical Press over a Basin consumes items and emits fluids through `create:compacting`. `create:pressing` is the wrong recipe type for this loop. A pure-Java translator in `integration-create` (`CreatePressRecipeTranslator`) turns PRESS definitions marked `create_compatible` into compacting recipes. Domain and application still do not import Create types.

## Conditional data

A bare `create:compacting` file crashes `RecipeManager` when Create is absent. Generated recipes wrap the compacting payload in `forge:conditional` with `forge:mod_loaded` / `create`. Datagen writes `press_red_grapes_create.json` and `press_white_grapes_create.json`.

## What Create does not preserve

Compacting outputs the registered Forge fluid plus a required solid byproduct. Recipe JSON does not carry harvest-lot NBT, so Create pressing produces default-property must. Agricultural fidelity stays on the [[artisanal-processing]] press.

Create pipes, pumps, and tanks attach to standard `IFluidHandler`. Alcoholic does not own a pipe network. GameTests that place `create:fluid_tank` run only with `-PwithCreate=true`; without Create they succeed as no-ops.

Phase 6 adds a separate Create surface: [[industrial-ports|kinetic ports]] drive industrial machines. This remains distinct from the compacting recipe adapter.

## Related

- [[artisanal-processing]]
- [[liquid-batch]]
- [[process-capability-graph]]
- [[loader-independent-minecraft-architecture]]
- [[semantic-crop-compatibility]]
- [[industrial-ports]]
- [[industrial-processing]]
- [[cursor-phase-4-processing-session]]
- [[cursor-phase-6-industrial-session]]
- [[forge-1.19.2-phase-4-verification]]
- [[forge-1.19.2-phase-6-verification]]
