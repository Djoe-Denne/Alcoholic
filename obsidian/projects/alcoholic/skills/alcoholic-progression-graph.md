---
title: >-
  Alcoholic Progression Graph
category: skills
tags: [minecraft, software-architecture, type/procedure, project/alcoholic]
aliases: [add ProgressionNode, ProgressionCoverageTest]
sources:
  - "C:/Users/djden/source/repos/Alcoholic/.cursor/skills/alcoholic-progression-graph/SKILL.md"
  - "C:/Users/djden/source/repos/Alcoholic/.cursor/rules/progression-graph.mdc"
summary: >-
  Edit ProgressionCatalog, then datagen. Never hand-edit the advancement provider or FTB SNBT.
provenance:
  extracted: 0.92
  inferred: 0.06
  ambiguous: 0.02
created: 2026-08-28T22:30:00+02:00
updated: 2026-08-28T22:30:00+02:00
---

# Alcoholic Progression Graph

Use this when shipping a process, industrial machine family, official crop, or artisanal executor. Voxel-only work uses [[blockbench-java-block-workflow]] and does **not** add a quest.

Vanilla advancements are the detection source ([[advancements-as-progression-source]]). Shape is [[wine-beer-progression-graph]].

## Add one node

1. Pick **chapter**: `ARTISANAL` (process / harvest) or `INDUSTRIAL` (`FORMED`).
2. Pick **line**: `WINE` (`x < 0`), `BEER` (`x > 0`), or `SHARED` (`x = 0`).
3. Set **parents** along that line. A shared junction that both lines reach gets both parents and `minRequiredDependencies(1)`.
4. Allocate the next free hex in the `A1C0A01C` family. Do not recycle IDs.
5. Add EN/FR `advancements.alcoholic.<id>.title` / `.description` in `GrapeAssetDataProvider`.
6. Run `.\gradlew :platform-forge-1.19.2:runDataCommon`.
7. Confirm `ProgressionCoverageTest` and `FtbQuestTemplateContractTest` pass.

Machines stay drink-agnostic. Lineage lives only on the `ProgressionNode`.

## Coverage that fails the build

- Official process with no PROCESS criterion and no FORMED machine that executes it
- `BuiltinMachines` family without exactly one industrial `FORMED` node
- Official crop (`red_grapes`, `white_grapes`, `hops`, `barley`) without a HARVEST criterion

Stubs `distill` / `infuse` are exempt until they become gameplay.

## Related

- [[wine-beer-progression-graph]]
- [[optional-ftb-quests-chapter]]
- [[industrial-progression-and-jei-formation]]
- [[forge-1.19.2-phase-1-verification]]
- [[alcoholic]]
