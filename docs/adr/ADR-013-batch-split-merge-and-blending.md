# ADR-013: Batch Split, Merge, and Blending

- Status: Accepted
- Date: 2026-08-25

## Context

ADR-010 defined compatible merge of the same liquid definition inside
Alcoholic tanks. Phase 5 needs conservation under drain, explicit blending
of distinct definitions, and rejection when a property cannot combine.

## Decision

`LiquidBatch` remains an immutable value object.

- `split(requestedVolume)` returns extracted + remaining. Volumes sum to
  the original. Property bags and provenance are independent copies.
- `merge` still requires the same definition. It fails when a property
  uses `IDENTICAL_OR_REJECT` and the values differ.
- `blend` is the only path that may change definition. It is invoked by
  `alcoholic:blend` from an explicit player action (artisanal crock),
  never by `LiquidTank.fill`.

`PropertyMerge` gains `SUM`, `IDENTICAL_OR_REJECT`, `COMBINE_SET`, and
`CUSTOM`. `CUSTOM` delegates to `LiquidProperty.aggregator()`. There is
no central `switch(propertyId)`.

`maturity` uses volume-weighted average of the degree. Provenance
`totalAgingTime` is a volume-weighted summary, not a recursive tree.

## Consequences

- Drain through `LiquidTank` cannot duplicate volume.
- Distinct definitions stay separate in tanks and Create pipes.
- A cuvée is a process output, not an accidental fill.
