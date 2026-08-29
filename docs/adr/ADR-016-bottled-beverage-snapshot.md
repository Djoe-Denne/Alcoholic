# ADR-016: Bottled Beverage Snapshot

- Status: Accepted
- Date: 2026-08-25

## Context

A filled bottle is a consumer item, not a portable process vessel. Persisting
yeast state, last-processed time, or kinetics on the item would leak
runtime into inventory and invite desync when the bottle returns to a tank.

## Decision

`BeverageBottleItem` stores a snapshot NBT (definition, ethanol, residual
sugar, acidity, maturity, origin fractions, derived quality summary,
purity, complexity, balance, and defects). Version is `2`. Version `1`
snapshots remain readable. It omits runtime fields (yeast, lastProcessed,
vessel clock).
Harvest `alcoholic:quality` stays on the lot; bottle `Quality` is
`QualityProfile.summary()` (see [ADR-037](ADR-037-emergent-quality-profile.md)).

Bottling is `alcoholic:bottle`: right-click a barrel, crock, or fermenter
with an empty bottle. Default volume is 250 mB. No player potion effects.

A stack that returns from a foreign tank without a `Version` tag shows a
debug tooltip. That loss is the ADR-010 edge, not silent corruption.

## Consequences

- Drinking / drunkenness stays out of scope.
- Re-pouring a bottle into a tank cannot restore process clock state.
- Inspect and tooltips read the snapshot, not a live batch simulation.
