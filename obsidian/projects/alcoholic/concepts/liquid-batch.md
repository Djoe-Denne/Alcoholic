---
title: Liquid Batch
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [LiquidDefinition, AlcoholicLiquid]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/a6b5797c-82f8-4021-9d63-10a82fed6899/a6b5797c-82f8-4021-9d63-10a82fed6899.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/c2ca3b27-ad63-4be9-af24-47c49c111f2f/c2ca3b27-ad63-4be9-af24-47c49c111f2f.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/dc49ea79-b9e5-4902-ad7f-795f762f8f52/dc49ea79-b9e5-4902-ad7f-795f762f8f52.jsonl"
summary: LiquidDefinition is datapack identity; LiquidBatch is volume, properties, and flattened provenance. Forge FluidStack is a transport adapter.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-08-25T14:10:00+02:00
updated: 2026-08-25T16:01:00+02:00
---

# Liquid Batch

Phase 4 needs tanks, buckets, and pipes without putting `FluidStack` in domain or application. Two objects stay distinct.

## Definition versus batch

A `LiquidDefinition` is datapack identity such as `alcoholic:red_grape_must`. Files live under `data/<namespace>/alcoholic/liquids/`. Defaults (sugar, acidity, ethanol, temperature) belong on the definition.

A `LiquidBatch` is that identity plus volume in millibuckets plus a typed property bag plus [[batch-provenance]]. There are no `RedWineBatch` subclasses. Category on a [[beverage-framework]] identity remains free metadata, not a Java `switch`. The batch is immutable: `split`, `merge`, and `blend` return new instances.

Volume is a domain `double` rounded when crossing into Forge. It is **not** stored in fluid NBT. The Minecraft stack amount is the volume. Putting volume in NBT would make two amounts of the same lot fail `FluidStack.isFluidEqual`, so Create tanks would refuse to merge them.

## Persistence and adapters

Artisanal machines persist `LiquidBatch` as vanilla NBT (`AlcoholicLiquid`). The Forge adapter converts to `FluidStack` for `IFluidHandler`. Each first-party definition that must travel in world pipes gets a matching Forge fluid registry entry. That fluid type is a transport label, not the domain identity.

[[industrial-multiblock]] controllers own the same batch model. [[industrial-ports]] expose access without owning state. Resizing cannot delete stored liquid.

Cider in the `testpack:` acceptance pack proves the process engine without registering Minecraft cider fluids.

## Split, merge, blend

`split` returns extracted plus remaining. Volumes sum to the original. Property bags and provenance are independent copies. `LiquidTank.drain` uses split so volume cannot be duplicated.

Alcoholic tanks merge batches that share a `LiquidDefinition`. Numeric properties use a volume-weighted average unless the property registry says otherwise. Strategies include `SUM`, `IDENTICAL_OR_REJECT`, `COMBINE_SET`, and `CUSTOM` via `LiquidProperty.aggregator()`. Different definitions do not merge; that is [[blend-versus-tank-merge]].

`LiquidBatchNbt` version 2 stores provenance. Volume stays off fluid NBT so Create tanks can still merge equal lots.

Foreign tanks that honour NBT treat distinct property compounds as distinct fluids. Tanks that strip NBT keep only the fluid id; sugar, acidity, and quality then fall back to definition defaults when the stack is read back. A missing `Version` tag shows a debug tooltip. See [[bottled-beverage-snapshot]]. ^[inferred]

## Related

- [[harvest-lot-metadata]]
- [[fermentation-physics]]
- [[create-press-adapter]]
- [[industrial-multiblock]]
- [[industrial-ports]]
- [[beverage-framework]]
- [[loader-independent-minecraft-architecture]]
- [[process-capability-graph]]
- [[batch-provenance]]
- [[blend-versus-tank-merge]]
- [[aging-process]]
- [[bottled-beverage-snapshot]]
- [[cursor-phase-4-processing-session]]
- [[cursor-phase-5-aging-session]]
