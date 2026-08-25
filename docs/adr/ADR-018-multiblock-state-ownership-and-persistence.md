# ADR-018: Multiblock State Ownership and Persistence

- Status: Accepted
- Date: 2026-08-25

## Context

Duplicating `LiquidBatch`, inventory, and process progress onto every
part would duplicate fluids on rebuild and lose metadata on reload.

## Decision

The controller BlockEntity is the only authoritative logical machine.

It owns:

- inventory
- `LiquidTank` / `LiquidBatch`
- process and stroke state
- derived capacity
- cached geometry and bound part positions

Parts implement `ControllerBound` with a cached controller position.
Capability requests resolve that cache and validate `owns(part)`. They
never rescan the structure.

Persistence:

- Controller NBT stores tank, items, process clocks, and geometry.
- Breaking casing invalidates the machine (`CLOSED` or `DRAIN_ONLY`) but
  leaves contents on the controller.
- Breaking the controller drops inventory, then a controller item that
  carries tank NBT. Controller loot tables are empty to avoid a second
  copy.
- Resize that would overflow stored volume refuses to reform
  (`OVERCAPACITY` / `DRAIN_ONLY`). Liquid is never deleted.

## Consequences

- Save/reload and chunk load restore one tank, not N copies.
- A broken shell can be repaired without flooding the world.
- Ports are views, not storage.
