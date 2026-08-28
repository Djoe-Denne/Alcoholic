---
name: alcoholic-progression-graph
description: Add or revise an Alcoholic wine/beer progression node when shipping a process, industrial machine family, official crop, or artisanal executor. Use when editing BuiltinRegistrations, BuiltinMachines, ProgressionCatalog, advancements, or FTB quest templates.
---

# Alcoholic progression graph

Vanilla advancements are the detection source ([ADR-033](../../../docs/adr/ADR-033-advancements-as-progression-source.md)).
The **shape** of both tabs and both FTB chapters is [ADR-036](../../../docs/adr/ADR-036-wine-beer-progression-graph.md).

Do not hand-edit `AlcoholicAdvancementProvider` or `modpack/ftbquests/**/*.snbt`.
Edit [`ProgressionCatalog`](../../../application/src/main/java/com/djden/alcoholic/application/progression/ProgressionCatalog.java), then run datagen.

## When this skill applies

- New shipped process in `BuiltinRegistrations` (not `distill` / `infuse` stubs)
- New entry in `BuiltinMachines`
- New official harvest crop
- New artisanal executor for a process that has no node yet

A voxel-only change uses `alcoholic-java-machine-model` and does **not** add a quest.

## Add one node

1. Pick **chapter**: `ARTISANAL` (process / harvest) or `INDUSTRIAL` (`FORMED`).
2. Pick **line**: `WINE` (`x < 0`), `BEER` (`x > 0`), or `SHARED` (`x = 0`).
3. Set **parents** along that line. A shared junction that both lines reach gets both parents and `minRequiredDependencies(1)`.
4. Allocate the next free hex in the `A1C0A01C` family. Do not recycle IDs.
5. Add EN/FR `advancements.alcoholic.<id>.title` / `.description` in `GrapeAssetDataProvider`.
6. Run `.\gradlew :platform-forge-1.19.2:runDataCommon`.
7. Confirm `ProgressionCoverageTest` and `FtbQuestTemplateContractTest` pass.

Machines stay drink-agnostic. Lineage lives only on the `ProgressionNode`.

## Coverage that will fail the build

- Official process with no PROCESS criterion and no FORMED machine that executes it
- `BuiltinMachines` family without exactly one industrial `FORMED` node
- Official crop (`red_grapes`, `white_grapes`, `hops`, `barley`) without a HARVEST criterion
