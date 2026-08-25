# ADR-026: MASH Thermal Transformation

- Status: Accepted
- Date: 2026-08-25

## Context

Mashing converts grist and water into wort plus spent grain. Temperature
changes extraction quality. Per-enzyme simulation is out of scope.

## Decision

`alcoholic:mash` is a generic mixed thermal process. Config names a solid
selector, a liquid input (volume), an output liquid, optional spent-grain
byproduct, duration, and a `TemperatureProfile`.

`TemperatureProfile.extractionYield` maps:

- preferred band → full extraction;
- operating below preferred → incomplete (slow/cold);
- operating above preferred → degraded fermentability;
- outside operating → residual yield only.

Sugar, color, and temperature are typed liquid properties on the produced
batch. Spent grain is an extractable item output, not discarded.

The mash tun is a two-tank executor: fillable input tank, drainable output
tank. Heat is sampled from the block below through `HeatSources` (vanilla
sources plus optional Create probes registered by the Forge root).

## Consequences

Wort stays a generic liquid. Future wash / spirit graphs reuse MASH without
marking it grain-beer-only. Large batches stay one `LiquidBatch`.
