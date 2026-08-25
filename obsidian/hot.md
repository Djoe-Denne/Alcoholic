---
title: Hot Cache
updated: 2026-08-25T20:05:00+02:00
---

## Recent Activity

- Synced Alcoholic — ADR-031: generic FE electric motor plus optional Crossroads axle adapter on MechanicalDrivePort.
- Follow-up: industrial press bills one winning kinetic port, not every shell port.
- Earlier: compile-time `AlcoholicDebug.ENABLED` so kinetic `Rpm` / `DebugRpm` NBT is dead-stripped from published jars.
- Earlier: Create independence (ADR-030, Malt Mill, primitive engine) and Phase 7A grain DAG.

## Active Threads

- Four mechanical supplies: primitive engine, electric motor, Create, Crossroads. Machines depend only on MechanicalDrivePort.
- IE Energy Connector is the supported IE path; no IImmersiveConnectable on the motor.
- Manual IE wire and live Crossroads axis GameTests are still unmarked in-wiki. ^[ambiguous]

## Key Takeaways

Alcoholic defines required mechanical power. Native engines and optional mods define how it is supplied. Crossroads must join the axle network (no neighbor poll). FE draw scales with billed work; idle neighbors must not drain. A spinning axle with zero joules is stalled.

## Flagged Contradictions
