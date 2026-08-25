# ADR-011: Aging Process Model

- Status: Accepted
- Date: 2026-08-25

## Context

Phase 4 registered `alcoholic:age` as a UNIT stub. Beverage graphs could
name the capability, but no vessel could execute it. Wine gameplay needs a
finished liquid after young wine, and later families need the same step
without a wine-specific engine.

## Decision

`alcoholic:age` is an optional, data-driven process type. The engine never
injects an AGE node. A vessel ages a batch only when
`ProcessRecipeResolver` finds a compatible `alcoholic:age` definition or
graph node for the stored liquid.

`AgingConfig` is decoded from datapack JSON. An empty or missing config is
valid and keeps the liquid identity unchanged. When `output.liquid` is
present, completion renames the batch the same way FERMENT does.

Progress is property evolution (`maturity`, wood, oxidation) plus flattened
provenance summaries. Completion is `maturity >= threshold` (default 1.0).
No beverage identity is consulted in `domain/process`.

## Consequences

- A datapack can add `young_mead → AGE → aged_mead` with no Java.
- Fixtures (whisky, beer, cider, rum) prove the same engine with AGE data.
- Vessels that cannot resolve an AGE recipe leave the batch unchanged.
