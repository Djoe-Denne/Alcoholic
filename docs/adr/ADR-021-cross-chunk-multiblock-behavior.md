# ADR-021: Cross-Chunk Multiblock Behavior

- Status: Accepted
- Date: 2026-08-25

## Context

A 9x16x9 tank almost always spans chunk borders. Forbidding that would
be an arbitrary player-facing restriction.

## Decision

Multiblocks may span chunks.

`WorldStructureSampler` reports `UNLOADED` when `hasChunkAt` is false.
The validator treats that as `INCOMPLETE` and leaves existing logical
state untouched.

When the missing chunk loads, the next dirty or periodic refresh
completes formation or confirms it.

There is no requirement that a machine stay inside one chunk or a
plus-shaped chunk neighbourhood.

## Consequences

Players can build along chunk seams. Tests cover unloaded cells as
incomplete rather than invalid.
