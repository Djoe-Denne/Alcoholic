---
title: Wiki Index
---

# Wiki Index

*This index is automatically maintained. Last updated: 2026-08-25T15:53:00+02:00*

## Concepts

- [[projects/alcoholic/concepts/loader-independent-minecraft-architecture|Loader-Independent Minecraft Architecture]] — Public API, pure core, vanilla common behavior, and loader adapters. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/semantic-crop-compatibility|Semantic Crop Compatibility]] — Tag-driven ingredient semantics and stable Vinery provider selection. ( #minecraft #compatibility)
- [[projects/alcoholic/concepts/perennial-viticulture|Perennial Viticulture]] — Eight-stage vine lifecycle; harvest never breaks the plant. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/trellis-training|Trellis Training]] — Posts, spool-placed wire, and derived training multipliers. ( #minecraft)
- [[projects/alcoholic/concepts/harvest-lot-metadata|Harvest Lot Metadata]] — Versioned grape NBT copied into must by PRESS. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/beverage-framework|Beverage Framework]] — Data-driven beverage graphs; PRESS, FERMENT, AGE, BLEND, and BOTTLE execute. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/process-capability-graph|Process Capability Graph]] — Acyclic process nodes; no process is mandatory. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/public-extension-api|Public Extension API]] — Versioned alcoholic-api including vessel profiles. ( #minecraft #compatibility)
- [[projects/alcoholic/concepts/liquid-batch|Liquid Batch]] — Datapack liquid identity plus volume, properties, and flattened provenance. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/fermentation-physics|Fermentation Physics]] — Continuous FERMENT kinetics with temperature bands and vented CO2. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/artisanal-processing|Artisanal Processing]] — Small-scale press, fermenter, oak barrel, and blending crock. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/create-press-adapter|Create Press Adapter]] — Optional create:compacting PRESS recipes behind forge:conditional. ( #minecraft #compatibility)
- [[projects/alcoholic/concepts/aging-process|Aging Process]] — Optional alcoholic:age; the engine never injects it. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/vessel-and-environment|Vessel and Environment]] — Vessel and cellar profiles; oak barrel ages only while loaded. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/batch-provenance|Batch Provenance]] — Flattened origin and blend maps with a 16-entry, 0.5% cutoff. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/blend-versus-tank-merge|Blend Versus Tank Merge]] — Same-definition merge versus explicit alcoholic:blend. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/bottled-beverage-snapshot|Bottled Beverage Snapshot]] — Consumer snapshot of a batch, not runtime process state. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/industrial-multiblock|Industrial Multiblock]] — Variable-size hollow cuboid machines; capacity is interior volume. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/industrial-processing|Industrial Processing]] — Extra PRESS and FERMENT executors plus a passive tank. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/industrial-ports|Industrial Ports]] — Fluid, item, and kinetic views on the controller; Create supplies shafts. ( #minecraft #compatibility)

## Entities

## Skills

- [[projects/alcoholic/skills/forge-1.19.2-phase-1-verification|Forge 1.19.2 Phase 1 Verification]] — Repeatable build, datagen, unit-test, and GameTest verification. ( #minecraft #testing)
- [[projects/alcoholic/skills/forge-1.19.2-phase-2-3-verification|Forge 1.19.2 Phase 2 and 3 Verification]] — Purity, catalog reload, and six viticulture GameTests. ( #minecraft #testing)
- [[projects/alcoholic/skills/forge-1.19.2-phase-4-verification|Forge 1.19.2 Phase 4 Verification]] — PRESS, FERMENT, fluid-adapter, and twelve processing GameTests. ( #minecraft #testing)
- [[projects/alcoholic/skills/forge-1.19.2-phase-5-verification|Forge 1.19.2 Phase 5 Verification]] — AGE, vessels, blend, bottle, and nineteen GameTests. ( #minecraft #testing)
- [[projects/alcoholic/skills/forge-1.19.2-phase-6-verification|Forge 1.19.2 Phase 6 Verification]] — Multiblock purity, industrial catalog, and industrial_pad GameTests. ( #minecraft #testing)

## References

- [[projects/alcoholic/references/cursor-phase-1-foundation-session|Cursor Phase 1 Foundation Session]] — Source session for architecture, implementation, and corrections. ( #minecraft #software-architecture)
- [[projects/alcoholic/references/cursor-phase-2-viticulture-session|Cursor Phase 2 Viticulture Session]] — Source session for perennial vines and trellis work. ( #minecraft #software-architecture)
- [[projects/alcoholic/references/cursor-phase-3-beverage-framework-session|Cursor Phase 3 Beverage Framework Session]] — Source session for the public beverage engine. ( #minecraft #software-architecture)
- [[projects/alcoholic/references/cursor-phase-4-processing-session|Cursor Phase 4 Processing Session]] — Source session for PRESS, liquids, and FERMENT. ( #minecraft #software-architecture)
- [[projects/alcoholic/references/cursor-phase-5-aging-session|Cursor Phase 5 Aging Session]] — Source session for aging, vessels, blend, and bottling. ( #minecraft #software-architecture)
- [[projects/alcoholic/references/cursor-phase-6-industrial-session|Cursor Phase 6 Industrial Session]] — Source session for industrial multiblocks and Create ports. ( #minecraft #software-architecture)

## Synthesis

## Journal

## Projects

- [[projects/alcoholic/alcoholic|Alcoholic]] — Loader-independent brewing mod: viticulture, artisanal process, and industrial multiblocks. ( #minecraft #project/alcoholic)
