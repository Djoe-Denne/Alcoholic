# ADR-009: Liquid Batch vs Minecraft Fluid Representation

- Status: Accepted
- Date: 2026-08-25

## Context

ADR-004 forbade `FluidStack` in the domain. Phase 4 needs tanks, buckets, and
Create pipes to move must and young wine. The domain still has to distinguish
the *kind* of liquid (`LiquidDefinition`) from the *state* of a stored amount
(`LiquidBatch`).

## Decision

`LiquidDefinition` is datapack identity (`alcoholic:red_grape_must`).
`LiquidBatch` is that identity plus volume (millibuckets) plus a typed
property bag (temperature, fermentable sugar, ethanol fraction, acidity,
quality, variety, fermentation stress). There are no `RedWineBatch` subclasses.

Minecraft/Forge never enter domain or application:

- Block entities persist `LiquidBatch` with vanilla NBT (`AlcoholicLiquid`).
- Forge adapters convert to `FluidStack` for `IFluidHandler`.
- Each first-party `LiquidDefinition` that must travel in world pipes has a
  matching Forge fluid registry entry. The fluid type is a transport label,
  not the domain identity.

Create is not a pipe network we own. We expose standard Forge fluid handlers
so Create pipes, pumps, and tanks can attach.

Cider in the data-only acceptance pack does not register Minecraft fluids.
That pack proves the process engine, not Forge registration.

## Consequences

- Tests of pressing and fermentation run without a Minecraft runtime.
- Foreign tanks see a Forge fluid plus NBT; see ADR-010 for what survives.
- Adding apple must as gameplay content later means registering a fluid,
  not a new Java batch class.
