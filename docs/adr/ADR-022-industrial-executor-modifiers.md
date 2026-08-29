# ADR-022: Industrial Executor Modifiers

- Status: Accepted
- Date: 2026-08-25
- Extended by: [ADR-037](ADR-037-emergent-quality-profile.md)

## Context

Industrial machines must not invent `IndustrialWineRecipe` types. They
must still feel larger and slightly more efficient than artisanal
equipment.

## Decision

`ExecutorModifiers` live on `ProcessContext`:

- `yieldModifier`
- `speedModifier`
- `thermalStability`
- `maxBatchUnits`

The process type and registry stay authoritative. An industrial PRESS
executor queries the same PRESS definitions as the artisanal press.
It applies modifiers only while executing.

Shipped defaults:

- Industrial press: yield 1.05, speed 2.0, unbounded batch units
- Industrial vat: thermal stability 4.0
- Native Malt Mill: yield 1.0, speed 1.0, batch 1
- Identity modifiers produce the same domain output as artisanal
  equipment

`PressProcessor` aggregates homogeneous input
(`available / inputAmount`, capped by `maxBatchUnits`) instead of
looping one item at a time.

## Consequences

A third-party hydraulic press registers another executor for
`alcoholic:press`. It does not edit wine JSON. Differences between
machines are executor parameters, not duplicate recipes.
