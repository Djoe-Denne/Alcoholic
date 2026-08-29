# ADR-037: Emergent Quality Profile

- Status: Accepted
- Date: 2026-08-29
- Extends: [ADR-022](ADR-022-industrial-executor-modifiers.md),
  [ADR-008](ADR-008-process-executor-capability-model.md)
- Related: [ADR-016](ADR-016-bottled-beverage-snapshot.md)

## Context

ADR-022 scaled industrial machines with yield, speed, thermal stability,
and batch size. A first draft of three-scale balance wanted each
executor to write `alcoholic:quality`. That recreates a magic 0–100
score and lets time or speed farm a better drink.

The three scales must stay one process definition. Volume and throughput
are industrial levers. Complexity and purity must cap even if the
player waits.

## Decision

Drink quality is a derived `QualityProfile` (purity, complexity,
maturity, balance, defects). Ethanol is never an input. Processors
write chemistry and provenance only. They stamp
`alcoholic:complexity_cap` (merge `MIN`) and `alcoholic:purity_floor`
(merge `MAX`) so industrial ceilings and floors survive bottling and
blending under identity modifiers.

`ExecutorModifiers` add `processFidelity`, `complexityCap`, and
`purityFloor`. Shipped defaults:

- Artisanal: fidelity 1.00, cap 1.00, floor 0.00, yield/speed identity
- Craft: fidelity 0.94, cap 0.82, floor 0.04
- Industrial: fidelity 0.70, cap 0.55, floor 0.12–0.15

Volume and throughput stay on ADR-022 knobs. Industrial press yield
1.05 / speed 2.0; industrial vat speed 1.0 with thermal stability 4.0.

`speedModifier` applies once:

- FERMENT / AGE / CONDITION: `scaleDelta` on chemistry ticks
- PRESS / MILL / MALT / MASH / BOIL: shorter `processingTicks` only

CONDITION UI job length stays unscaled; progress follows maturity.

CO₂ from FERMENT is produced then vented. Dissolved carbonation is
`alcoholic:carbonation` (CONDITION). Room atmosphere and SO₂ stay out
of v1. Wine oxygen uses `OxygenCurve` on existing oxidation exposure.
Tannin and colour contribute to complexity when present. Balance
averages only axes that are actually on the batch.

The bottle snapshot is version 2. `Quality` is the derived summary, not
harvest `alcoholic:quality`. Version 1 snapshots remain readable.

## Consequences

- Identity modifiers still match artisanal chemistry besides the stamp
  of an explicit cap and purity floor.
- Datapack overlays that omit the new modifier keys default to
  fidelity 1, cap 1, floor 0.
- A warehouse AGE machine can reuse the same AGE process later with
  the industrial cap.
- Blending industrial and artisanal lots keeps the tighter cap and the
  higher purity floor.
