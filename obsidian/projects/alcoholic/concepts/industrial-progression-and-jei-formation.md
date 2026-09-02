---
title: >-
  Industrial Progression and JEI Formation
category: concepts
tags: [minecraft, software-architecture, compatibility, type/concept, project/alcoholic]
aliases: [ADR-035, multiblock_formed, alcoholic:multiblock_formation]
sources:
  - "C:/Users/djden/source/repos/Alcoholic/docs/adr/ADR-035-industrial-progression-and-jei-formation.md"
summary: >-
  A second tab starts at industrial_root. Formation uses multiblock_formed. JEI shows the min hull, not a mega-mesh.
provenance:
  extracted: 0.88
  inferred: 0.1
  ambiguous: 0.02
created: 2026-08-28T22:30:00+02:00
updated: 2026-08-28T22:30:00+02:00
---

# Industrial Progression and JEI Formation

Players need to learn how to form the minimum [[industrial-multiblock]] hull without Create, Ponder, or an FTB compile dependency. Advancements stay the source of truth ([[advancements-as-progression-source]]). Artisanal IDs stay valid. [[wine-beer-progression-graph]] adds beer-line nodes and owns parent shape.

## Second tab

A second Minecraft tab starts at `alcoholic:industrial_root` (no parent on `alcoholic:root`). One advancement per formed machine family:

- `industrial_root` — `inventory_changed` OR of `industrial_casing` or any of the nine controllers
- `form_industrial_press` → `alcoholic:industrial_press`
- `form_industrial_vat` → `alcoholic:industrial_fermentation_vat`
- `form_industrial_tank` → `alcoholic:industrial_storage_tank`
- `form_industrial_malt_house` → `alcoholic:industrial_malt_house`
- `form_industrial_roller_mill` → `alcoholic:industrial_roller_mill`
- `form_industrial_mash_tun` → `alcoholic:industrial_mash_tun`
- `form_industrial_kettle` → `alcoholic:industrial_brewing_kettle`
- `form_industrial_conditioning` → `alcoholic:industrial_conditioning_vessel`
- `form_industrial_aging` → `alcoholic:industrial_aging_vessel`

The trigger is `alcoholic:multiblock_formed` with a `machine` field. `MultiblockControllerBlockEntity.revalidate` fires it on the rising edge of `formed`. Attribution uses the last actor (use or part placement), nearby players within 16 blocks if none is set, then a pending `formed` queue flushed on the next `touch`. Forming a machine is a progression event even when the last block placed is casing.

Industrial process completions reuse `alcoholic:process_completed`. A press that produces must still grants `produce_must`. The storage tank has formation only.

## JEI

Category `alcoholic:multiblock_formation` shows the min hull from `IndustrialHullPattern` (useful face −Z). The DTO lives in `application` and rebinds on catalogue reload. There is no 3D mega-mesh preview. Recipe viewers never re-detect hull geometry. See [[process-display-and-recipe-viewers]].

## Optional FTB chapter

Chapter `alcoholic_industrial` (hex `A1C0A01C00000002`, quests `…0020`–`…0028`) is nine `AdvancementTask` entries. Flipbooks `item/ftbquests/form_*` assemble the min hull layer by layer. Hover text points at JEI. See [[optional-ftb-quests-chapter]].

## Related

- [[industrial-multiblock]]
- [[industrial-processing]]
- [[formed-multiblock-visual]]
- [[wine-beer-progression-graph]]
- [[process-display-and-recipe-viewers]]
- [[cursor-progression-and-fluids-session]]
- [[alcoholic]]
