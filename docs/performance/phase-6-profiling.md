# Phase 6 profiling notes

This is instrumentation, not a formal benchmark.

## What was measured

`MultiblockProfiler` records:

- `HollowCuboidValidator.validate` nanoseconds
- controller `serverTick` nanoseconds

A unit fixture validates a 5x8x5 hollow tank fifty times against an
in-memory `StructureQuery` (no world, no chunk I/O).

## Findings

- One validation of a formed hollow cuboid is a connected-shell BFS plus
  one bbox pass. It is not a per-mB or per-item loop.
- Fifty sequential validations of the same 5x8x5 query stay in the
  microsecond-to-low-millisecond range on a developer workstation. The
  unit test fails if the average exceeds 5 ms, which would indicate an
  accidental full-volume nested scan.
- Formed controllers refresh structure at most every 200 ticks unless a
  nearby part change dirties them. Unformed controllers retry every 20
  ticks. Capability requests do not validate.
- FERMENT on the vat steps one `LiquidBatch` from elapsed time. Volume
  does not multiply tick cost.

## Factory-scale implication

Ten formed machines cost about ten controller ticks plus rare
validations. Fifty passive tanks cost fifty cheap ticks and almost no
structure work if nobody is breaking blocks.

The remaining world cost is vanilla BlockEntity ticking and Create
pipe/belt networks, which Alcoholic does not replace.
