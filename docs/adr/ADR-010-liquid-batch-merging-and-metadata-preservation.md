# ADR-010: Liquid Batch Merging and Metadata Preservation

- Status: Accepted
- Date: 2026-08-25

## Context

Forge 1.19.2 `FluidStack` carries an amount, a fluid type, and optional NBT.
Create 0.5.1.x fluid pipes, pumps, and tanks move `FluidStack` values and use
fluid equality that includes NBT. A rich Distillery `LiquidBatch` is larger
than a vanilla fluid id. We inspected that contract rather than assuming
arbitrary metadata would round-trip.

Verified behaviour:

- `FluidStack.copy()` copies NBT. Pipe transfer therefore keeps NBT when the
  network stores stacks rather than flattening to a fluid id.
- `FluidStack.isFluidEqual` compares fluid type **and** NBT, not amount. Two
  stacks of the same must with different property NBT are not mixed by Create
  tanks; they behave as incompatible fluids.
- `IFluidHandler.fill` on Create tanks therefore refuses to blend distinct
  harvest lots. That preserves difference at the cost of extra tank slots.
- Some third-party tanks ignore NBT and keep only the fluid id. In that case
  only `LiquidDefinition` survives; sugar, acidity, and quality fall back to
  definition defaults when the stack is read back.
- Create compacting outputs a fluid of the registered type. Recipe JSON does
  not carry harvest-lot NBT, so Create pressing produces default-property
  must. Agricultural fidelity stays on the artisanal press.

A virtual-fluid ledger (ghost tanks keyed by UUID) would preserve more data
through NBT-stripping mods. It is not required for Create 0.5.1.x, and it
would fight the existing pipe network.

## Decision

Store batch properties in `FluidStack` NBT (`Version`, `Definition`,
`Properties`). Do **not** store volume in that NBT; volume is the stack
amount. Otherwise two amounts of the same lot would fail `isFluidEqual`.

Alcoholic tanks merge compatible batches (same `LiquidDefinition`) with
per-property strategies from the public property registry:

- numeric properties: volume-weighted average (or max/min/first when
  registered)
- string properties such as variety: keep the value if equal, else
  `"blended"`

Different definitions do not merge. Blending remains a future process type.

Create Mechanical Press recipes emit must plus a solid byproduct so compacting
has a required item result. Those fluids have empty/default NBT.

## Consequences

- Distinct grape lots remain distinct in Alcoholic tanks and in Create tanks
  that honour NBT (they simply will not combine).
- Mixing two lots in an Alcoholic tank averages sugar and acidity by volume.
- Interacting with a foreign tank that strips NBT is a documented lossy edge.
- No separate Distillery pipe network is introduced.
