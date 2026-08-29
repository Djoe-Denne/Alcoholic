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
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/d120a5cc-91ff-47b3-99d0-392583b27834/d120a5cc-91ff-47b3-99d0-392583b27834.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/57568d0b-dbd3-4dd2-acc5-bcf3d6799ff6/57568d0b-dbd3-4dd2-acc5-bcf3d6799ff6.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/9c25457e-4350-4b33-9dc9-83e0eb1511d1/9c25457e-4350-4b33-9dc9-83e0eb1511d1.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/f1fcf432-2298-4c57-b7c8-573f8bf487ed/f1fcf432-2298-4c57-b7c8-573f8bf487ed.jsonl"
summary: Small-scale machines execute process capabilities: wine vessels plus malting floor, mash tun, kettle, and malt mill.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-08-25T14:10:00+02:00
updated: 2026-08-28T19:15:00+02:00
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

The voxel fermenter is directional: `FACING` (hatch on −Z of the model, copper tap on +X) and `OPEN` (`artisanal_fermenter_open`, hatch cubes rotate 22.5° on X). Hitbox includes hatch, airlock, and tap. `animateTick` emits `BUBBLE_POP` and a little `CLOUD` from the airlock once liquid is present and yeast has started (or CO₂ has already vented).

## Oak barrel and blending crock

The oak barrel is one adapter of [[vessel-and-environment]]: 4000 mB, capability `alcoholic:age`, loaded-only catch-up. It exposes `IFluidHandler` for Create. History records previous contents; a used barrel applies seasoning. Artisanal press, fermenter, mash tun, and kettle hold 2000 mB.

The artisanal blending crock has two 4000 mB tanks. Both tanks must accept fill (`canFillTank` for index 0 and 1). A review found only the first tank fillable, which made `blend()` unreachable; that is fixed. Filling never auto-merges distinct definitions. Sneak plus empty hand runs [[blend-versus-tank-merge]]. Clicking the jar to open its inventory also opens the voxel lid.

Visible copper/iron/clay taps on fermenter, barrel, mash tun, and kettle are **decoration**. `BlockHitResult` is unused. Fluid I/O is `IFluidHandler` on every face. Aligning two taps is cosmetic; industrial `fluid_port` is the directional fluid face. See [[cursor-machine-port-audit-session]].

## Grain machines

The malting floor is `MALT`-only (overlapping pale/amber/dark via sneak-cycle). The mash tun is a two-tank thermal executor heated from below. The brewing kettle extracts hop properties into a liquid. The Malt Mill needs [[mechanical-drive-port]] power (primitive engine, [[electric-motor]], Create, or [[crossroads-rotary-adapter]]) and stalls without it. Hopped wort then reuses the artisanal fermenter.

The floor, mill, engine, mash tun, kettle, fermenter, barrel, crock, and press now have [[artisanal-machine-voxel-models]] (Java Blockbench, not GeckoLib). See also [[mash-tun-visual]] and [[brewing-kettle-visual]]. IDs and process bindings do not change. Machine item crafts follow [[vanilla-xor-create-crafts]].

Empty hand (no sneak) opens the machine screen. JEI clicks the process types from `displayedProcessTypes()`, not a shared GUI layout. See [[process-display-and-recipe-viewers]].

Operator debug placement for the beer line is `/alcoholic debug place` — see [[alcoholic-debug-commands]] and [[cursor-artisanal-brewery-guide-session]].

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
- [[process-display-and-recipe-viewers]]
- [[cursor-artisanal-brewery-guide-session]]
- [[mash-tun-visual]]
- [[brewing-kettle-visual]]
- [[vanilla-xor-create-crafts]]
- [[alcoholic-debug-commands]]
- [[cursor-machine-port-audit-session]]
- [[cursor-voxel-campaign-session]]
- [[codex-ajouter-modeles-3d-minecraft]]
- [[cursor-phase-4-processing-session]]
- [[cursor-phase-5-aging-session]]
- [[cursor-phase-7a-grain-session]]
- [[forge-1.19.2-phase-4-verification]]
- [[forge-1.19.2-phase-5-verification]]
- [[forge-1.19.2-phase-7a-verification]]
