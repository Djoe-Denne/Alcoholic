# ADR-020: Structure Validation and Cache Invalidation

- Status: Accepted
- Date: 2026-08-25

## Context

Scanning a 9x16x9 volume every tick, or on every capability request,
does not scale to a factory of tanks.

## Decision

Validation is event-driven with a slow safety refresh:

1. Place/break of a multiblock part calls `MultiblockNotifier`, which
   dirties controllers in the max bounding box (9x16x9). That scan runs
   only on block changes.
2. A dirty or periodic tick (every 20 ticks unformed, every 200 ticks
   formed) runs `HollowCuboidValidator` once.
3. Capability requests use the cached controller pointer only.

A formed machine becomes invalid when casing breaks, the interior is
obstructed, controller count is wrong, dimensions leave the allowed
range, or an unsupported block appears on the shell.

An unloaded chunk intersecting the connected shell returns
`INCOMPLETE`, not `INVALID`. A formed machine is not torn down because
a neighbour chunk is momentarily missing.

## Consequences

Passive tanks do no whole-structure work on most ticks. Datapack reload
replaces `MachineCatalog`; the next validation reads the new definition.
