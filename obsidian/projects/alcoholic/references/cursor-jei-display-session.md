---
title: Cursor JEI Display Session
category: references
tags: [minecraft, software-architecture, compatibility, type/concept, project/alcoholic]
aliases: [ADR-032 session, JEI recipes session]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/57568d0b-dbd3-4dd2-acc5-bcf3d6799ff6/57568d0b-dbd3-4dd2-acc5-bcf3d6799ff6.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/d57caa3c-9de3-4ca9-8f7b-d53f8411e614/d57caa3c-9de3-4ca9-8f7b-d53f8411e614.jsonl"
summary: >-
  JEI must show every process the engine can run. A closed switch and layout-as-category failed review; ProcessDisplaySpec is the port.
provenance:
  extracted: 0.82
  inferred: 0.16
  ambiguous: 0.02
created: 2026-08-27T16:20:00+02:00
updated: 2026-08-27T16:20:00+02:00
---

# Cursor JEI Display Session

The user asked to add process recipes to JEI whenever JEI is on the instance. A first cut lived in uncommitted application + Forge adapter code. A QMD-backed red-team then treated that cut as **not commit-ready**.

## What failed review

A closed `if MILL / else if MASH` in `ProcessDisplayRecipes` plus `catch → fromGeneric` contradicted ADR-006: an addon process would need a core edit, or would show invented ports after a decode failure.

`MachineLayout` was used as the JEI category key. Sharing `TWO_SLOTS` made the mill open malting recipes. An `industrial_` prefix hack made the artisanal press open MILL / MASH / BOIL as well.

Invented 1000 mB volumes, a hardcoded empty bottle, dropped boil `atProgress` / `role`, and `RecipeType` UIDs that used only the process path would collide with another mod's `ferment`.

JEI registered at client boot against an empty catalog, then swallowed reload failures. `notifyCatalogReloaded()` did not rebuild the viewer.

## What shipped after the fix

See [[process-display-and-recipe-viewers]] and ADR-032. The projection is a port of `ProcessType`. Machines advertise `displayedProcessTypes()`. JEI lives in `platform-forge-1.19.2`. Having JEI on the test instance is enough to see categories; world datapacks on a dedicated-server client stay invisible until a catalog sync exists. ^[inferred]

## Distilled pages

- [[process-display-and-recipe-viewers]]
- [[public-extension-api]]
- [[loader-independent-minecraft-architecture]]

## Related

- [[grain-processing]]
- [[artisanal-processing]]
- [[cursor-create-independence-session]]
- [[alcoholic]]
