---
title: >-
  Wine / Beer Progression Graph
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [ADR-036, ProgressionCatalog, wine beer quest graph]
sources:
  - "C:/Users/djden/source/repos/Alcoholic/docs/adr/ADR-036-wine-beer-progression-graph.md"
  - "C:/Users/djden/source/repos/Alcoholic/.cursor/skills/alcoholic-progression-graph/SKILL.md"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/07a9f6b4-cb15-4290-9926-14a35cd01c5a/07a9f6b4-cb15-4290-9926-14a35cd01c5a.jsonl"
  - "C:/Users/djden/source/repos/Alcoholic/docs/audits/current-playability-audit.md"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/112827ce-4901-4cfd-8dad-1e626367ee8f/112827ce-4901-4cfd-8dad-1e626367ee8f.jsonl"
summary: >-
  ProgressionCatalog owns tab and FTB shape. Wine left, beer right, shared center. Craft FORMED nodes and condition_beer sit on the graph.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-08-28T22:30:00+02:00
updated: 2026-09-09T16:00:00+02:00
---

# Wine / Beer Progression Graph

`ProgressionCatalog` in `application` is the only place that names quest lineages. Detection stays in vanilla advancements ([[advancements-as-progression-source]]). FTB SNBT and advancement JSON are generated. Do not hand-edit `AlcoholicAdvancementProvider` or `modpack/ftbquests/**/*.snbt`.

This graph is the player-facing path. It is not the beverage [[process-capability-graph]]. Machines and process types stay drink-agnostic; lineage lives only on the `ProgressionNode`.

## Chapters and columns

- Two chapters: artisanal and industrial ([[optional-ftb-quests-chapter]], [[industrial-progression-and-jei-formation]]).
- Two lineages plus a shared center: wine (`x < 0`), shared (`x = 0`), beer (`x > 0`).
- Shared junctions (`ferment_beverage`, `form_industrial_vat`) use FTB `min_required_dependencies: 1`. Vanilla advancements have a single parent, so those nodes parent the chapter root.

A player can follow only wine or only beer and still reach the shared ferment / vat node.

## Artisanal IDs

`produce_must` keeps its ID but is press-only. Beer steps are `harvest_barley`, `malt`, `mill`, `mash`, and `boil`. Worlds that already earned `produce_must` via mash keep that advancement; new mash completions grant `alcoholic:mash` instead.

Existing hex IDs in the `A1C0A01C` family stay. New artisanal IDs occupy `…0018`–`…001C`. See `modpack/ftbquests/README.md`.

Triggers do not change: `alcoholic:crop_harvested`, `alcoholic:process_completed`, `alcoholic:multiblock_formed`.

## Craft and industrial follow-ups (2026-09-09)

Artisanal continues `root` → wine harvest **or** barley/hops → press / malt→mill→mash→boil → `ferment_beverage` (OR) → `age_wine` | hidden `blend` | `bottle`. Five `form_craft_*` nodes sit after malt, mill, mash, boil, and `ferment_beverage`.

Industrial continues `industrial_root` → press | tank | malt→mill→mash→kettle → vat (OR) → conditioning | aging. FTB still accepts vat **or** conditioning. Vanilla `form_industrial_aging` parents `form_industrial_vat` (vanilla advancements have a single parent). Process quest `condition_beer` hangs under conditioning.

Beer ferment on the beverage graph publishes `young`. See [[playability-audit]] and [[grain-processing]].

## Coverage

CI fails if a shipped process (except `distill` / `infuse` stubs), a `BuiltinMachines` family, a `BuiltinCraftMachines` family, or an official crop has no node. Adding a new formed family or promoting a stub process requires a `ProgressionNode` (chapter, line, parents) before CI passes. How-to: [[alcoholic-progression-graph]].

## Related

- [[advancements-as-progression-source]]
- [[optional-ftb-quests-chapter]]
- [[industrial-progression-and-jei-formation]]
- [[process-capability-graph]]
- [[grain-processing]]
- [[process-display-and-recipe-viewers]]
- [[cursor-progression-and-fluids-session]]
- [[playability-audit]]
- [[alcoholic]]
