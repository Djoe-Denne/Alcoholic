# ADR-005: Viticulture Migration and Persistence

- Status: Accepted
- Date: 2026-08-25

## Context

Phase 1 registered simple age-based grapevines and selected either internal or
Vinery acquisition without changing registry identity. Phase 2 introduces
perennial domain state, trained rows, data-driven climate profiles, and harvest
lot metadata. Existing worlds must continue to load, and optional Vinery
support must not become a compile-time dependency.

## Decision

The legacy `age` block-state property remains serialized for world
compatibility. When a vine has no versioned block-entity payload, its bounded
age from 0 through 4 is migrated to the nearest Phase 2 growth stage. New
authoritative lifecycle data lives in the vine block entity; `stage` and
`trained` are synchronized rendering/query properties. Generated block models
select on `stage` and `trained` and intentionally ignore `age`, avoiding five
copies of every visual state.

Phase 1 acquisition policy still controls creative discoverability and wild
generation. It does not disable viticulture. Red and white vine registry
entries remain present and existing vines always grow and harvest. When Vinery
is available, its exact seed items may plant Alcoholic vines only in a valid
trained row; all other interactions are left to Vinery.

Harvested grape stacks carry the item-agnostic
`AlcoholicHarvestLot` compound. Version 2 stores quality, sugar, and acidity as
integers from 0 through 1000 and exposes them as doubles from 0 through 1.
Version 1 double-valued compounds remain readable for migration. The metadata
does not depend on whether the concrete grape item comes from Alcoholic or
Vinery.

Trellis wire is persisted as axis-oriented block segments between structural
posts. The spool creates a bounded contiguous run and spends one durability
point per segment. Wire has no direct drop, preventing segment duplication;
posts remain normal recoverable blocks. Training is derived at runtime only
when a wire run reaches valid posts on both sides within the configured span.

## Consequences

- Existing age-only vines migrate without registry remaps.
- Resource packs need 8 lifecycle stages times trained/untrained, not an
  additional age multiplier.
- Vinery changes acquisition and concrete harvest items, never the lifecycle
  availability of Alcoholic vines.
- Harvest properties survive stack movement with deterministic precision.
- Broken wire must be replaced with the spool, while posts can be recovered.
- This decision introduces no press, fluid, fermentation, or Create process.
