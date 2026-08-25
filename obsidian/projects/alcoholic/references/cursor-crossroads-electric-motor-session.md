---
title: Cursor Crossroads Electric Motor Session
category: references
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [ADR-031 session]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/1ca21b6f-a077-4686-8f47-c41ee6e59fc7/1ca21b6f-a077-4686-8f47-c41ee6e59fc7.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/ede9eb58-09ac-4ccc-9fea-74a521453d14/ede9eb58-09ac-4ccc-9fea-74a521453d14.jsonl"
summary: Session that added optional Crossroads axle bridging and a generic FE electric motor, then fixed multi-port mechanical billing.
provenance:
  extracted: 0.86
  inferred: 0.10
  ambiguous: 0.04
created: 2026-08-25T20:05:00+02:00
updated: 2026-08-25T20:05:00+02:00
---

# Cursor Crossroads Electric Motor Session

After [[cursor-create-independence-session]], the next cut added two more supplies onto the same [[mechanical-drive-port]]: Crossroads rotary and a generic Forge Energy motor. ADR-031 records the mapping.

## Mandate

Machines must keep depending only on `MechanicalDrivePort`. No `if (crossroadsInstalled)` / `if (immersiveEngineeringInstalled)` in mill or press code. Create stays an optional extra.

## What shipped

- `consumeWork` on the port; `MechanicalDrives.forMachine` plus `LocalAdapter` for capabilities on the consumer.
- Native [[electric-motor]] with `EnergyBuffer` and Forge `IEnergyStorage`.
- [[crossroads-rotary-adapter]] attaching `AXLE_CAPABILITY` and joining the axis network.
- Optional IE-shaped recipe; no `IImmersiveConnectable`; IE Energy Connector is the supported wire path.
- Gradle `-PwithCrossroads` / `-PwithImmersiveEngineering`; Essentials is a Crossroads runtime dependency.

Wrong Curse project **247931** is MineWorldTech, not Crossroads. The real Crossroads MC project is **250231**.

## Red-team follow-up

A design review of uncommitted code found no critical or high issues. The remaining medium finding: `consumeMechanicalWork` billed every kinetic shell port, so one adjacent motor could be drained N times per press tick. The fix selects the same winning port as `collectDrive()` and bills once. Existing GameTests use `debugForceRpm` and do not cover that FE drain. ^[ambiguous]

## Not closed in these chats

Manual IE wire → connector → motor in the client, and a live Crossroads master axis → axle → mill, were not run here. Unit and architecture gates passed. ^[ambiguous]

## Distilled pages

- [[electric-motor]]
- [[crossroads-rotary-adapter]]
- [[mechanical-drive-port]]
- [[forge-1.19.2-crossroads-fe-verification]]

## Related

- [[cursor-create-independence-session]]
- [[native-executor-invariant]]
- [[industrial-ports]]
- [[loader-independent-minecraft-architecture]]
