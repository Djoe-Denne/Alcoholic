---
title: Process Display and Recipe Viewers
category: concepts
tags: [minecraft, software-architecture, compatibility, type/concept, project/alcoholic]
aliases: [ADR-032, JEI, ProcessDisplaySpec]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/57568d0b-dbd3-4dd2-acc5-bcf3d6799ff6/57568d0b-dbd3-4dd2-acc5-bcf3d6799ff6.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/d57caa3c-9de3-4ca9-8f7b-d53f8411e614/d57caa3c-9de3-4ca9-8f7b-d53f8411e614.jsonl"
  - "C:/Users/djden/source/repos/Alcoholic/docs/adr/ADR-035-industrial-progression-and-jei-formation.md"
summary: Recipe viewers adapt ProcessDisplaySpec. JEI also shows industrial min-hull formation.
provenance:
  extracted: 0.88
  inferred: 0.1
  ambiguous: 0.02
created: 2026-08-27T16:00:00+02:00
updated: 2026-08-28T22:30:00+02:00
---

# Process Display and Recipe Viewers

JEI (and later REI/EMI) is a loader adapter. Process IO is projected by the registered `ProcessType` as `ProcessDisplaySpec`. A config implements `ProcessDisplaying`, or `ProcessType.of` takes an explicit display function. Otherwise viewers use `SolidAccepting` plus declared item inputs.

Missing volumes stay empty. Viewers must not invent 1000 mB. Decode failures hide the recipe instead of inventing generic ports.

`MachineAccess.displayedProcessTypes()` is the click target. Sharing `MachineLayout.TWO_SLOTS` does not make the mill open malting recipes. JEI `RecipeType` UIDs are the full `ResourceId`.

JEI rebinds on `AlcoholicApi.notifyCatalogReloaded()`. Client resources / the Alcoholic jar are bootstrap only. World datapacks on a dedicated-server client stay invisible until a catalog sync exists. ^[inferred]

Category `alcoholic:multiblock_formation` shows the minimum industrial hull from `IndustrialHullPattern` (useful face −Z). The DTO lives in `application` and rebinds on catalogue reload. There is no 3D mega-mesh preview. Formation detection stays on `alcoholic:multiblock_formed`, not in the viewer. See [[industrial-progression-and-jei-formation]].

## Related

- [[public-extension-api]]
- [[loader-independent-minecraft-architecture]]
- [[process-capability-graph]]
- [[grain-processing]]
- [[industrial-progression-and-jei-formation]]
- [[wine-beer-progression-graph]]
- [[cursor-jei-display-session]]
- [[alcoholic]]
