# ADR-019: Port and Capability Architecture

- Status: Accepted
- Date: 2026-08-25

## Context

Automation must not require touching the controller. Create already
provides pipes, pumps, belts, and shafts. Alcoholic must not ship a
second transport layer.

## Decision

Generic ports are configured by the formed machine, not by beverage:

- Fluid port → Forge `FLUID_HANDLER` over the controller tank, gated by
  `INPUT` / `OUTPUT` / `BOTH` and `IndustrialAccess`
- Item port → `WorldlyContainer` + Forge `ITEM_HANDLER`
- Kinetic port → `KineticSource` RPM probe

Players cycle port mode with sneak + empty hand or `create:wrench`.

When Create is present the kinetic port is a Create `KineticBlock` so
shafts and gearboxes connect. When Create is absent the vanilla port
still stores RPM for GameTests via `debugForceRpm`.

The controller may also expose capabilities, but routine factories use
ports.

## Consequences

```
Create belt → item port → industrial press → fluid port → Create pipe
```

works without Alcoholic pipes. Future machines reuse the same three
ports.
