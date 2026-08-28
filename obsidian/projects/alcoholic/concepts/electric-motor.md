---
title: Electric Motor
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [alcoholic:electric_motor, EnergyBuffer, Forge Energy motor]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/1ca21b6f-a077-4686-8f47-c41ee6e59fc7/1ca21b6f-a077-4686-8f47-c41ee6e59fc7.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/ede9eb58-09ac-4ccc-9fea-74a521453d14/ede9eb58-09ac-4ccc-9fea-74a521453d14.jsonl"
summary: Native Alcoholic FE consumer that emits MechanicalDrivePort. Idle machines do not drain; empty buffer emits no torque.
provenance:
  extracted: 0.88
  inferred: 0.10
  ambiguous: 0.02
created: 2026-08-25T20:05:00+02:00
updated: 2026-08-28T19:15:00+02:00
---

# Electric Motor

`alcoholic:electric_motor` is native Alcoholic content. It is not an Immersive Engineering machine. It stores Forge Energy in an `EnergyBuffer` and implements [[mechanical-drive-port]]. Platform Forge exposes `ForgeCapabilities.ENERGY` (`canReceive=true`, `canExtract=false`) so any FE connector can charge it.

```text
Forge Energy
    |
    v
Electric Motor
    |
    v
MechanicalDrivePort
    |
    v
Alcoholic machine
```

## Defaults (`ElectricMotorSettings.DEFAULT`)

| Parameter | Value |
|---|---|
| Capacity | 8000 FE |
| Max input | 80 FE/t |
| Output speed | 32 (Alcoholic units) |
| Max load | 8 (medium) |
| Efficiency | 0.8 |
| FE per capacity unit | 20 at 100% → **25 FE** per 1.0 load tick |

First implementation uses a fixed output speed. Draw scales with the load actually billed this tick (`feForLoad`).

## Idle and empty

Neighbors call `MechanicalDrives.consumeWork` only when they perform work. An idle mill or press does not drain the motor at max FE/t. An empty buffer reports idle drive: no torque. Receive-only FE plus usage billing is the intended multiplayer contract. ^[inferred]

## Immersive Engineering

IE 1.19.2 already converts its wire network to FE at an Energy Connector. Supported path:

```text
IE Generator / Capacitor → IE wire → Energy Connector → Electric Motor → machine
```

Alcoholic does not implement `IImmersiveConnectable` or `EnergyTransferHandler`. Prefer one connector block over a second motor class. A vanilla iron/redstone/copper recipe always exists. `electric_motor_ie` unlocks when `immersiveengineering:coil_lv` is present. There is one motor. When Create is loaded, machine crafts follow [[vanilla-xor-create-crafts]] instead.

## Visual

The voxel shaft sits at **Y = 10**, matching [[primitive-combustion-engine-visual]]. The whole motor was raised 3.2 px and the four feet stretched to Y = 0. The shaft spins while the motor is on. See [[cursor-machine-port-audit-session]] and [[cursor-voxel-campaign-session]].

## Related

- [[mechanical-drive-port]]
- [[crossroads-rotary-adapter]]
- [[native-executor-invariant]]
- [[loader-independent-minecraft-architecture]]
- [[grain-processing]]
- [[industrial-processing]]
- [[vanilla-xor-create-crafts]]
- [[cursor-machine-port-audit-session]]
- [[cursor-crossroads-electric-motor-session]]
- [[forge-1.19.2-crossroads-fe-verification]]
