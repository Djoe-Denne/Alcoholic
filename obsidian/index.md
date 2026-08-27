---
title: Wiki Index
---

# Wiki Index

*This index is automatically maintained. Last updated: 2026-08-27T16:20:00+02:00*

## Concepts

- [[projects/alcoholic/concepts/loader-independent-minecraft-architecture|Loader-Independent Minecraft Architecture]] — Public API, pure core, vanilla common behavior, and loader adapters. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/semantic-crop-compatibility|Semantic Crop Compatibility]] — Tag-driven ingredient semantics; Vinery grapes and Brewery barley/hops. ( #minecraft #compatibility)
- [[projects/alcoholic/concepts/perennial-viticulture|Perennial Viticulture]] — Eight-stage vine lifecycle; harvest never breaks the plant. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/trellis-training|Trellis Training]] — Posts, spool-placed wire, and derived training multipliers. ( #minecraft)
- [[projects/alcoholic/concepts/harvest-lot-metadata|Harvest Lot Metadata]] — Versioned grape NBT copied into must by PRESS. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/beverage-framework|Beverage Framework]] — Data-driven beverage graphs; wine and grain beer execute without drink-family Java. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/process-capability-graph|Process Capability Graph]] — Acyclic process nodes; no process is mandatory; official nodes need a native executor. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/public-extension-api|Public Extension API]] — Versioned alcoholic-api including vessel profiles. ( #minecraft #compatibility)
- [[projects/alcoholic/concepts/process-display-and-recipe-viewers|Process Display and Recipe Viewers]] — ProcessDisplaySpec port; JEI is a Forge adapter; machines advertise process types. ( #minecraft #compatibility)
- [[projects/alcoholic/concepts/liquid-batch|Liquid Batch]] — Datapack liquid identity plus volume, properties, and flattened provenance. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/fermentation-physics|Fermentation Physics]] — Continuous FERMENT kinetics with temperature bands and vented CO2. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/artisanal-processing|Artisanal Processing]] — Small-scale wine vessels plus malting floor, mash tun, kettle, and malt mill. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/create-press-adapter|Create Press Adapter]] — Optional Create PRESS and MILL recipes; they never replace native executors. ( #minecraft #compatibility)
- [[projects/alcoholic/concepts/aging-process|Aging Process]] — Optional alcoholic:age; the engine never injects it. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/vessel-and-environment|Vessel and Environment]] — Vessel and cellar profiles; oak barrel ages only while loaded. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/batch-provenance|Batch Provenance]] — Flattened origin and blend maps with a 16-entry, 0.5% cutoff. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/blend-versus-tank-merge|Blend Versus Tank Merge]] — Same-definition merge versus explicit alcoholic:blend. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/bottled-beverage-snapshot|Bottled Beverage Snapshot]] — Consumer snapshot of a batch, not runtime process state. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/industrial-multiblock|Industrial Multiblock]] — Variable-size hollow cuboid machines; capacity is interior volume. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/industrial-processing|Industrial Processing]] — Extra PRESS and FERMENT executors plus a passive tank; kinetic power is not Create-only. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/industrial-ports|Industrial Ports]] — Fluid, item, and kinetic views; one kinetic port wins per tick for gate and billing. ( #minecraft #compatibility)
- [[projects/alcoholic/concepts/grain-processing|Grain Processing]] — Beer DAG through generic MALT, MILL, MASH, and BOIL; no drink-family branches. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/mechanical-drive-port|Mechanical Drive Port]] — Loader-independent rotary port; four supplies; consumeWork bills the winner. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/electric-motor|Electric Motor]] — Native FE consumer that emits MechanicalDrivePort; idle machines do not drain. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/crossroads-rotary-adapter|Crossroads Rotary Adapter]] — Optional IAxleHandler on Alcoholic inputs; joule billing stays in the adapter. ( #minecraft #compatibility)
- [[projects/alcoholic/concepts/native-executor-invariant|Native Executor Invariant]] — Official DAGs ship a native executor; Create, Crossroads, and IE stay optional. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/artisanal-machine-voxel-models|Artisanal Machine Voxel Models]] — Java Blockbench models for floor, engine, and mill; no GeckoLib. ( #minecraft #software-architecture)
- [[projects/alcoholic/concepts/resource-pack-resolution-chain|Resource Pack Resolution Chain]] — 512 master, downsample from master only, mid-res default. ( #minecraft)
- [[projects/alcoholic/concepts/malting-floor-visual|Malting Floor Visual]] — Fork 2 oak tray, mat, and barley; 64 default after 512 master. ( #minecraft)
- [[projects/alcoholic/concepts/primitive-combustion-engine-visual|Primitive Combustion Engine Visual]] — Fork 3 off/lit BDcraft atlases and archived 512 master. ( #minecraft)
- [[projects/alcoholic/concepts/malt-mill-visual|Malt Mill Visual]] — Fork 4 hopper and rollers; no crank; 32 default. ( #minecraft)

## Entities

- [[projects/alcoholic/entities/blockbench|Blockbench]] — Desktop 5.1.x plus MCP on 127.0.0.1:8787 for Java block models. ( #minecraft)

## Skills

- [[projects/alcoholic/skills/forge-1.19.2-phase-1-verification|Forge 1.19.2 Phase 1 Verification]] — Repeatable build, datagen, unit-test, and GameTest verification. ( #minecraft #testing)
- [[projects/alcoholic/skills/forge-1.19.2-phase-2-3-verification|Forge 1.19.2 Phase 2 and 3 Verification]] — Purity, catalog reload, and six viticulture GameTests. ( #minecraft #testing)
- [[projects/alcoholic/skills/forge-1.19.2-phase-4-verification|Forge 1.19.2 Phase 4 Verification]] — PRESS, FERMENT, fluid-adapter, and twelve processing GameTests. ( #minecraft #testing)
- [[projects/alcoholic/skills/forge-1.19.2-phase-5-verification|Forge 1.19.2 Phase 5 Verification]] — AGE, vessels, blend, bottle, and nineteen GameTests. ( #minecraft #testing)
- [[projects/alcoholic/skills/forge-1.19.2-phase-6-verification|Forge 1.19.2 Phase 6 Verification]] — Multiblock purity, industrial catalog, and industrial_pad GameTests. ( #minecraft #testing)
- [[projects/alcoholic/skills/forge-1.19.2-phase-7a-verification|Forge 1.19.2 Phase 7A Verification]] — Grain GameTests, mechanical mill, Create isolation, and debug-NBT compile gate. ( #minecraft #testing)
- [[projects/alcoholic/skills/forge-1.19.2-crossroads-fe-verification|Forge 1.19.2 Crossroads and FE Verification]] — Architecture, mapping tests, FE GameTests, optional Crossroads and IE client paths. ( #minecraft #testing)
- [[projects/alcoholic/skills/blockbench-java-block-workflow|Blockbench Java Block Workflow]] — One machine, grey silhouette, locked oak, 512 paint, user stop, then downsample. ( #minecraft)
- [[projects/alcoholic/skills/context-mode-project-mcp|Context-Mode Project MCP]] — One context-mode server per repo via .cursor/mcp.json, never the user mcp.json. ( #software-architecture)

## References

- [[projects/alcoholic/references/cursor-phase-1-foundation-session|Cursor Phase 1 Foundation Session]] — Source session for architecture, implementation, and corrections. ( #minecraft #software-architecture)
- [[projects/alcoholic/references/cursor-phase-2-viticulture-session|Cursor Phase 2 Viticulture Session]] — Source session for perennial vines and trellis work. ( #minecraft #software-architecture)
- [[projects/alcoholic/references/cursor-phase-3-beverage-framework-session|Cursor Phase 3 Beverage Framework Session]] — Source session for the public beverage engine. ( #minecraft #software-architecture)
- [[projects/alcoholic/references/cursor-phase-4-processing-session|Cursor Phase 4 Processing Session]] — Source session for PRESS, liquids, and FERMENT. ( #minecraft #software-architecture)
- [[projects/alcoholic/references/cursor-phase-5-aging-session|Cursor Phase 5 Aging Session]] — Source session for aging, vessels, blend, and bottling. ( #minecraft #software-architecture)
- [[projects/alcoholic/references/cursor-phase-6-industrial-session|Cursor Phase 6 Industrial Session]] — Source session for industrial multiblocks and Create ports. ( #minecraft #software-architecture)
- [[projects/alcoholic/references/cursor-phase-7a-grain-session|Cursor Phase 7A Grain Session]] — Source session for beer DAG, barley/hops, and generic malt/mill/mash/boil. ( #minecraft #software-architecture)
- [[projects/alcoholic/references/cursor-create-independence-session|Cursor Create Independence Session]] — Red-team P0/P1 fixes, native mill, then compile-time debug kinetic NBT. ( #minecraft #software-architecture)
- [[projects/alcoholic/references/cursor-crossroads-electric-motor-session|Cursor Crossroads Electric Motor Session]] — Optional Crossroads axle bridge, generic FE motor, then winning-port billing. ( #minecraft #software-architecture)
- [[projects/alcoholic/references/codex-ajouter-modeles-3d-minecraft|Codex Ajouter Modèles 3D Minecraft]] — Parent plan plus three machine forks and the Blockbench MCP install. ( #minecraft #software-architecture)
- [[projects/alcoholic/references/cursor-jei-display-session|Cursor JEI Display Session]] — JEI recipes via ProcessDisplaySpec after a red-team of the first closed switch. ( #minecraft #software-architecture)
- [[projects/alcoholic/references/cursor-artisanal-brewery-guide-session|Cursor Artisanal Brewery Guide Session]] — QMD beer guide plus /alcoholic debug place layouts. ( #minecraft #software-architecture)

## Synthesis

## Journal

## Projects

- [[projects/alcoholic/alcoholic|Alcoholic]] — Loader-independent brewing mod: viticulture, grain beer, artisanal process, and industrial multiblocks. ( #minecraft #project/alcoholic)
