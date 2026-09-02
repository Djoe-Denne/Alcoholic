# ADR-039: Cask Imprint

- Status: Accepted
- Date: 2026-09-02
- Extends: [ADR-012](ADR-012-process-vessel-and-material-interaction.md),
  [ADR-011](ADR-011-aging-process-model.md),
  [ADR-038](ADR-038-quality-operator-dag.md)

## Context

Barrel history stored previous liquid ids but did not change the next fill.
Wood species is out of scope. Whisky (and later other families) need the
previous occupant to stain acidity, sugar, tannin, aroma, and peat-like
roast without a drink-family switch.

## Decision

On empty, the vessel snapshots a `PropertyBag` of transferable axes.
The oak barrel uses the matching AGE recipe's `imprint_properties` when
one exists; otherwise the v1 set (`alcoholic:acidity`, `sugar`, `tannin`,
`aroma`, `roast_intensity`). A sip is scaled by peak volume / capacity
for that fill. A definition swap without an empty tick records the
previous occupant first.

That imprint is faded × 0.55 and, **per axis present on both sides**,
averaged with the new snapshot. An axis only on the old bag keeps the
faded value; an axis only on the new bag is kept in full.

`AgingPhysics` leaks imprint **upward only**, linearly in the unseasoned
maturity step: `current += imprint * imprint_transfer * (Δmaturity / threshold)`,
capped at the imprint value. Leak keeps running after maturity completes
until the batch reaches the imprint on each axis (a forgotten cask can
copy the stain in full). Default `imprint_transfer` is 0.20 per
unseasoned maturity cycle. Seasoning does not apply to the leak; fade
models refill weakness. The industrial aging vessel uses the same
tracker and leak, with industrial thermal stability and executor
modifiers.

AGE never reads a beverage id. Quality datapacks interpret the same axes:
wine treats `roast_intensity` as a defect; spirit, beer, and generic fold
roast (and, for spirit/beer, acid and sugar) into complexity. An omitted
`imprint_properties` key keeps the v1 axes; an explicit empty list
disables leak for that recipe.

## Consequences

- A used oak barrel is no longer only 15% faster.
- Addons change transfer or axes in AGE JSON; they do not patch Java.
- Distillation must later preserve `roast_intensity` when DISTILL ships.
