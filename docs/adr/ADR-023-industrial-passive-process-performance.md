# ADR-023: Industrial Passive Process Performance

- Status: Accepted
- Date: 2026-08-25

## Context

A 2000 B vat must not be a thousand times more expensive than a 2 B
fermenter. Fifty tanks in one base must not rescan themselves every
tick.

## Decision

- FERMENT on the industrial vat uses `ElapsedProcessClock` and steps the
  whole `LiquidBatch` from elapsed time, volume, and damped temperature.
  There is no per-mB loop.
- STORAGE performs no process. It only holds a tank.
- Structure work is dirty + periodic (ADR-020), never per-capability.
- Tag membership is sampled into `StructureCell` strings at validation
  time, not resolved inside a hot item loop.
- `MultiblockProfiler` records validation and tick nanoseconds.

Instrumentation on a 5x8x5 hollow tank shows validation in the
microsecond-to-low-millisecond range for fifty sequential checks on the
same in-memory query. That is not a world benchmark; it is a guard
against accidental O(volume) Java in the validator.

## Consequences

Large batches stay one `LiquidBatch`. Future mash or boil executors
must evolve batch state the same way, not simulate each millibucket.
