---
title: Artisanal Processing
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [artisanal press, artisanal fermenter, oak barrel, blending crock]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/a6b5797c-82f8-4021-9d63-10a82fed6899/a6b5797c-82f8-4021-9d63-10a82fed6899.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/c2ca3b27-ad63-4be9-af24-47c49c111f2f/c2ca3b27-ad63-4be9-af24-47c49c111f2f.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/dc49ea79-b9e5-4902-ad7f-795f762f8f52/dc49ea79-b9e5-4902-ad7f-795f762f8f52.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/95c81b7c-fa88-4055-9741-14cb948964c9/95c81b7c-fa88-4055-9741-14cb948964c9.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/1ca21b6f-a077-4686-8f47-c41ee6e59fc7/1ca21b6f-a077-4686-8f47-c41ee6e59fc7.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-15-09-01a03f48-7af6-7513-a3c9-b297e81b7a96.jsonl"
summary: Small-scale machines execute process capabilities: wine vessels plus malting floor, mash tun, kettle, and malt mill.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-08-25T14:10:00+02:00
updated: 2026-08-27T13:30:00+02:00
---

# Artisanal Processing

Phase 4 shipped press and fermenter. Phase 5 adds the oak barrel and artisanal blending crock. They remain valid after Phase 6. [[industrial-processing]] adds larger executors for PRESS and FERMENT; it does not replace these machines. Phase 7A adds the malting floor, mash tun, brewing kettle, and Malt Mill for [[grain-processing]].

## Capability, not identity

The press knows only that it executes `alcoholic:press`. The fermenter knows only `alcoholic:ferment`. The oak barrel knows only `alcoholic:age`. The crock knows only `alcoholic:blend`. The malting floor knows only `alcoholic:malt`. The mash tun knows only `alcoholic:mash`. The kettle knows only `alcoholic:boil`. The Malt Mill knows only `alcoholic:mill`. Beverage JSON never names those machine ids. The same PRESS, MILL, or AGE definition can later run on a Create adapter or another vessel. See [[process-capability-graph]], [[native-executor-invariant]], and [[create-press-adapter]].

Recipe binding walks catalog process definitions. The machines must not contain `if (inputIsRedGrape)` or `if (makingWine)`.

## Artisanal press

The press accepts solid inputs (item or tag, including [[semantic-crop-compatibility]] grape tags), runs a timed batch (`processing_time` 20 ticks on shipped wine recipes), stores output [[liquid-batch]] internally, and may emit a solid byproduct such as `alcoholic:grape_pomace`. Players extract fluid through the Forge `IFluidHandler` on the block entity.

[[harvest-lot-metadata]] on the incoming stack is copied onto the produced must by the generic PRESS handler, not by the block entity. Distinct lots stay distinct unless an Alcoholic tank later merges compatible definitions.

## Artisanal fermenter

The fermenter stores a [[liquid-batch]], accepts `#alcoholic:yeast`, and ticks [[fermentation-physics]]. Fluid can arrive from the press, a bucket, or Create pipes attached to the same capability surface.

## Oak barrel and blending crock

The oak barrel is one adapter of [[vessel-and-environment]]: 8000 mB, capability `alcoholic:age`, loaded-only catch-up. It exposes `IFluidHandler` for Create. History records previous contents; a used barrel applies seasoning.

The artisanal blending crock has two 4000 mB tanks. Both tanks must accept fill (`canFillTank` for index 0 and 1). A review found only the first tank fillable, which made `blend()` unreachable; that is fixed. Filling never auto-merges distinct definitions. Sneak plus empty hand runs [[blend-versus-tank-merge]].

## Grain machines

The malting floor is `MALT`-only (overlapping pale/amber/dark via sneak-cycle). The mash tun is a two-tank thermal executor heated from below. The brewing kettle extracts hop properties into a liquid. The Malt Mill needs [[mechanical-drive-port]] power (primitive engine, [[electric-motor]], Create, or [[crossroads-rotary-adapter]]) and stalls without it. Hopped wort then reuses the artisanal fermenter.

The floor, mill, and primitive engine are gaining [[artisanal-machine-voxel-models]] (Java Blockbench models, not GeckoLib). Visuals live on [[malting-floor-visual]], [[malt-mill-visual]], and [[primitive-combustion-engine-visual]]. IDs and process bindings do not change.

Right-click a vessel with `alcoholic:empty_bottle` to write a [[bottled-beverage-snapshot]].

## What they persist

Block entities store domain `LiquidTank` / `LiquidBatch` with vanilla NBT. Forge attaches `IFluidHandler` on top. Domain and application never import Forge fluid types.

## Related

- [[liquid-batch]]
- [[fermentation-physics]]
- [[aging-process]]
- [[vessel-and-environment]]
- [[blend-versus-tank-merge]]
- [[bottled-beverage-snapshot]]
- [[create-press-adapter]]
- [[industrial-processing]]
- [[industrial-multiblock]]
- [[grain-processing]]
- [[mechanical-drive-port]]
- [[electric-motor]]
- [[crossroads-rotary-adapter]]
- [[native-executor-invariant]]
- [[artisanal-machine-voxel-models]]
- [[resource-pack-resolution-chain]]
- [[public-extension-api]]
- [[alcoholic]]
- [[codex-ajouter-modeles-3d-minecraft]]
- [[cursor-phase-4-processing-session]]
- [[cursor-phase-5-aging-session]]
- [[cursor-phase-7a-grain-session]]
- [[forge-1.19.2-phase-4-verification]]
- [[forge-1.19.2-phase-5-verification]]
- [[forge-1.19.2-phase-7a-verification]]
