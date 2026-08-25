# ADR-030: Native Mechanical Executors and Optional Create

- Status: Accepted
- Date: 2026-08-25
- Supersedes: [ADR-028](ADR-028-create-mill-executor-integration.md) on the
  "Alcoholic has no mill" decision

## Context

Create was intended as optional logistics and automation. Official
production started to look as if MILL (and industrial kinetic power)
required Create machinery. That violates the product rule: Alcoholic
must be fully playable without an optional integration.

## Decision

No production DAG shipped by Alcoholic may require an executor supplied
exclusively by an optional integration. Once a process such as `PRESS`,
`MILL`, `MALT`, `MASH`, `BOIL`, `FERMENT`, `AGE`, or `DISTILL` is part of
official progression, Alcoholic ships at least one native executor.

Mechanical machines depend on a small, loader-independent port:

```text
MechanicalDrivePort
MechanicalDriveState
MechanicalRequirement
```

Units are Alcoholic speed and capacity, not Create RPM or Stress Units.
Adapters (Create today, Crossroads later) translate their own model.

Alcoholic ships:

- a **Malt Mill** that executes generic `MILL`;
- a **Primitive Combustion Engine** that burns furnace fuel and feeds a
  machine's mechanical port directly.

Alcoholic does **not** add shafts, gearboxes, cogwheels, clutches, belts,
or pipes. Create remains the superior mechanical and fluid network when
present. The native mill stays enabled if Create is installed; multiple
executors coexist. Throughput differences use `ExecutorModifiers` or
Create recipe times, not duplicated process definitions.

## Consequences

- Official grain progression works without Create.
- `integration-create-forge-1.19.2` owns Create types (heat, millstone,
  crushing wheels, kinetic port, drive probe).
- Future rotary mods register a `MechanicalDrives.Probe` without editing
  the Malt Mill.
