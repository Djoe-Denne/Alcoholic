# ADR-025: Generic Solid/Liquid Multi-Input Process Model

- Status: Accepted
- Date: 2026-08-25

## Context

Wine PRESS is one solid in, one liquid out. Grain mash and boil need mixed
ports: solids plus a liquid, or a liquid plus ingredient additions. A
drink-family input type would force `if (isBeer)` into the engine.

## Decision

`ProcessInputs` already maps named ports to solids and liquids. Process
configs may implement `SolidAccepting` and `LiquidAccepting` together.
`ProcessRecipeResolver` matches an offered item and an offered liquid against
any registered type.

A node may require one solid and one liquid, or more solids in the graph.
Timed boil additions are represented as an ordered list of
`{selector, atProgress}` on the boil config. Phase 7A uses a single addition
at progress `0.0`. Later schedules append entries; they do not replace the
process type.

Vanilla water uses `minecraft:water`. There is no brewing-water fluid.

## Consequences

MASH, BOIL, and future mixed processes share the same input model. Graph
fixtures (wheat plus malted grain, non-grain mash) validate without engine
branches.
