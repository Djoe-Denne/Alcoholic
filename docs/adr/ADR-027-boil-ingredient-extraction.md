# ADR-027: BOIL Ingredient Extraction Model

- Status: Accepted
- Date: 2026-08-25

## Context

Hop additions extract bitterness and aroma into a liquid without replacing
FERMENT. Bitterness must not live as a drink-family field on `LiquidBatch`.

## Decision

`alcoholic:boil` heats a liquid and consumes solid additions. Config names
input/output liquids, an addition selector, duration, temperature, and a
`HopProfile` (`bitternessPotential`, `aromaPotential`).

Extracted values write to typed properties (`alcoholic:bitterness`,
`alcoholic:aroma`). Unrelated batch properties are preserved by transforming
the same `LiquidBatch` identity/volume.

Hop varieties are data. Phase 7A ships one generic hop item whose harvest
NBT may carry those properties; identity remains the `#alcoholic:hops` tag.

Timed schedules: `BoilConfig.additions` is the extension point. Phase 7A
does not implement an arbitrary timeline engine.

The brewing kettle executes `BOIL`. Existing fermenters then run generic
`FERMENT` on hopped wort. AGE is not injected.

## Consequences

Third-party liquids ignore bitterness if they never set it. Future hop
cultivars are JSON. Barrel-aged grain beverages can add an AGE node later.
