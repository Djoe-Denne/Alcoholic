# ADR-035: Industrial Progression and JEI Formation

- Status: Accepted
- Date: 2026-08-28
- Extends: [ADR-033](ADR-033-advancements-as-progression-source.md),
  [ADR-034](ADR-034-ftb-quests-optional-chapter.md),
  [ADR-017](ADR-017-industrial-hollow-cuboid-machines.md),
  [ADR-032](ADR-032-process-display-and-recipe-viewers.md)

## Context

The artisanal path is already a vanilla advancement tab ([ADR-033](ADR-033-advancements-as-progression-source.md))
with an optional FTB chapter ([ADR-034](ADR-034-ftb-quests-optional-chapter.md)).
Industrial machines are hollow cuboids of variable size
([ADR-017](ADR-017-industrial-hollow-cuboid-machines.md)). Players need to
learn how to form the minimum hull, without Create, Ponder, or an FTB
compile dependency.

## Decision

Advancements stay the source of truth. The eight artisanal IDs from
ADR-033 do not change.

A second Minecraft tab starts at `alcoholic:industrial_root` (no parent
on `alcoholic:root`). One advancement per formed machine family:

- `alcoholic:industrial_root` — `inventory_changed` OR of
  `industrial_casing` or any of the eight controllers
- `alcoholic:form_industrial_press` → machine `alcoholic:industrial_press`
- `alcoholic:form_industrial_vat` → `alcoholic:industrial_fermentation_vat`
- `alcoholic:form_industrial_tank` → `alcoholic:industrial_storage_tank`
- `alcoholic:form_industrial_malt_house` → `alcoholic:industrial_malt_house`
- `alcoholic:form_industrial_roller_mill` → `alcoholic:industrial_roller_mill`
- `alcoholic:form_industrial_mash_tun` → `alcoholic:industrial_mash_tun`
- `alcoholic:form_industrial_kettle` → `alcoholic:industrial_brewing_kettle`
- `alcoholic:form_industrial_conditioning` → `alcoholic:industrial_conditioning_vessel`

The trigger is `alcoholic:multiblock_formed` with a `machine` field.
`MultiblockControllerBlockEntity.revalidate` fires it on the rising edge
of `formed`. Attribution uses the last actor (use or part placement),
nearby players within 16 blocks if none is set, then a pending `formed`
queue flushed on the next `touch`.

Industrial process completions reuse `alcoholic:process_completed`. A
press that produces must still grants `produce_must`. The storage tank
has formation only.

JEI category `alcoholic:multiblock_formation` shows the min hull from
`IndustrialHullPattern` (useful face −Z). The DTO lives in `application`
and rebinds on catalogue reload. There is no 3D mega-mesh preview.

The optional FTB chapter `alcoholic_industrial` (hex
`A1C0A01C00000002`, quests `…0020`–`…0028`) is nine `AdvancementTask`
entries. Flipbooks `item/ftbquests/form_*` assemble the min hull layer
by layer. Hover text points at JEI.

## Consequences

- Forming a machine is a progression event even when the last block
  placed is casing, not a click on the controller.
- Recipe viewers and FTB never re-detect hull geometry.
- Adding a ninth industrial family needs a new advancement ID, a JEI
  recipe from the catalogue, and a matching FTB quest hex.
