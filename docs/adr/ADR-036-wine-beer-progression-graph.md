# ADR-036: Wine / Beer Progression Graph

- Status: Accepted
- Date: 2026-08-28
- Supersedes in part: [ADR-033](ADR-033-advancements-as-progression-source.md),
  [ADR-034](ADR-034-ftb-quests-optional-chapter.md),
  [ADR-035](ADR-035-industrial-progression-and-jei-formation.md)
- Extends: [ADR-001](ADR-001-loader-independent-architecture.md)

## Context

ADR-033 and ADR-035 shipped two advancement tabs and two optional FTB
chapters, but both graphs were flat. `produce_must` mixed grape press
and beer mash. Industrial `form_*` nodes all hung from
`industrial_root`. Adding a machine meant editing the advancement
provider, SNBT, translations, and hex tables by hand.

Official production DAGs are already split: wine is PRESS → FERMENT →
AGE; beer is BARLEY → MALT → MILL → MASH → BOIL → FERMENT.

## Decision

`ProgressionCatalog` in `application` is the only place that names
quest lineages. Machines and process types stay drink-agnostic.

- Two chapters: artisanal and industrial.
- Two lineages plus a shared center column: wine (`x < 0`), shared
  (`x = 0`), beer (`x > 0`).
- Shared junctions (`ferment_beverage`, `form_industrial_vat`) use
  FTB `min_required_dependencies: 1`. Vanilla advancements have a
  single parent, so those nodes parent the chapter root.
- `produce_must` keeps its ID but is press-only. Beer steps are
  `harvest_barley`, `malt`, `mill`, `mash`, and `boil`.
- Existing hex IDs in the `A1C0A01C` family stay. New artisanal IDs
  occupy `…0018`–`…001C`.
- Coverage tests fail if a shipped process (except `distill` /
  `infuse` stubs), a `BuiltinMachines` family, or an official crop
  has no node.
- Advancements and `modpack/ftbquests` SNBT are generated from the
  catalogue.

Triggers from ADR-033 / ADR-035 do not change:
`alcoholic:crop_harvested`, `alcoholic:process_completed`,
`alcoholic:multiblock_formed`.

## Consequences

- A player can follow only wine or only beer and still reach the
  shared ferment / vat node.
- Adding a ninth industrial family or promoting a stub process to
  gameplay requires a `ProgressionNode` (chapter, line, parents)
  before CI passes.
- Worlds that already earned `produce_must` via mash keep that
  advancement; new mash completions grant `alcoholic:mash` instead.
