---
title: Mechanical Drive Port
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [MechanicalDrivePort, MechanicalDriveState, MechanicalRequirement, Primitive Combustion Engine, AlcoholicDebug, LocalAdapter]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/14805ad4-dec0-4781-92a1-c718c5113dde/14805ad4-dec0-4781-92a1-c718c5113dde.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/1ca21b6f-a077-4686-8f47-c41ee6e59fc7/1ca21b6f-a077-4686-8f47-c41ee6e59fc7.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/ede9eb58-09ac-4ccc-9fea-74a521453d14/ede9eb58-09ac-4ccc-9fea-74a521453d14.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-56-07-01a03f6d-f9dd-71c2-8d26-3fc369bd905a.jsonl"
summary: Loader-independent rotary port. Four supplies; consumeWork bills the winning source. Debug RPM NBT is compile-stripped.
provenance:
  extracted: 0.84
  inferred: 0.14
  ambiguous: 0.02
created: 2026-08-25T18:55:00+02:00
updated: 2026-08-27T13:30:00+02:00
---

# Mechanical Drive Port

Machines that need rotary power speak a small domain port. Domain code does not import Create RPM, Stress Units, or kinetic classes.

## Port model

```text
MechanicalDrivePort
MechanicalDriveState
MechanicalRequirement
```

A state carries speed, available capacity, direction, running, and stalled. A requirement carries min/max speed and required capacity. Units are Alcoholic speed and capacity. `KineticRequirement` is a compatibility view that maps `min_rpm` onto speed.

`minecraft-common` `MechanicalDrives` samples the same way `HeatSources` samples heat. Machines call `forMachine`: local adapters on the consumer plus adjacent sources. A native block entity that implements `MechanicalDrivePort` and `isSource()` is read directly. Optional mods register either:

- `Probe` — sample an **adjacent** foreign block (Create kinetic).
- `LocalAdapter` — sample a capability **on the machine** (Crossroads `IAxleHandler` attached to the mill or kinetic port).

`consumeWork(load)` bills the winning source. Idle machines must not call it. Crossroads joule drain and FE draw live in those adapters, not in mill Java.

```text
                    MechanicalDrivePort
                           ^
                           |
      +--------------------+--------------------+
      |                    |                    |
Primitive Engine       Electric Motor      external adapters
      |                    |                 /        \
 furnace fuel          Forge Energy       Create   Crossroads
```

## Primitive combustion engine

`alcoholic:primitive_combustion_engine` burns furnace-compatible fuel (coal, charcoal, wood). It provides a low fixed speed and limited capacity, and sits **adjacent** to a machine or an [[industrial-ports|industrial kinetic port]]. It is a standalone fallback so official mechanical processes stay playable without Create. The visible voxel and off/lit atlases are documented on [[primitive-combustion-engine-visual]]; they do not change the port contract.

Alcoholic does not add shafts, gearboxes, cogwheels, clutches, transmissions, belts, or pipes. That would duplicate Create. The intended factory still uses Create logistics when the mod is present.

## Consumers

The [[native-executor-invariant|Malt Mill]] reads `forMachine` and stalls when the requirement is unmet. The industrial press can run from an engine, [[electric-motor]], Create shaft, or Crossroads axle against its kinetic port. The port is a `MechanicalDrivePort` relay.

A multiblock with several `kinetic_port` cells must bill **once**, on the same winning port `collectDrive()` used. Billing every shell port drained a shared adjacent motor N times. See [[industrial-ports]].

## Debug RPM is compile-time

A red-team review of local changes found persisted `Rpm` / `DebugRpm` on kinetic ports as a medium gameplay-integrity cheat. A schematic or NBT inject could run the press or mill with infinite capacity and no engine. ^[inferred]

Java has no `#if DEBUG`. Gradle generates `AlcoholicDebug.ENABLED` as a `static final boolean` literal. `javac` dead-strips the save and load branches when it is false, so those keys do not exist in published bytecode.

- `true` for IDE / `run*`
- `false` for `build` / `jar` / `publish`
- Override: `-Palcoholic.debug=true|false`

Both write and read are gated. Gating only save still loads injected NBT. `debugForceRpm` stays in-memory so GameTests work without persistence. Controller `LastRpm` is last observed gameplay speed, not this debug hook.

## Related

- [[native-executor-invariant]]
- [[electric-motor]]
- [[crossroads-rotary-adapter]]
- [[industrial-ports]]
- [[industrial-processing]]
- [[grain-processing]]
- [[primitive-combustion-engine-visual]]
- [[create-press-adapter]]
- [[loader-independent-minecraft-architecture]]
- [[cursor-create-independence-session]]
- [[cursor-crossroads-electric-motor-session]]
- [[forge-1.19.2-phase-7a-verification]]
- [[forge-1.19.2-crossroads-fe-verification]]
