---
title: Crossroads Rotary Adapter
category: concepts
tags: [minecraft, compatibility, type/concept, project/alcoholic]
aliases: [IAxleHandler, AXLE_CAPABILITY, CrossroadsRotaryMapping, ADR-031]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/1ca21b6f-a077-4686-8f47-c41ee6e59fc7/1ca21b6f-a077-4686-8f47-c41ee6e59fc7.jsonl"
summary: Optional Crossroads 2.9.5 axle joins the rotary network on Alcoholic inputs. Units and joule billing stay in the adapter.
provenance:
  extracted: 0.90
  inferred: 0.08
  ambiguous: 0.02
created: 2026-08-25T20:05:00+02:00
updated: 2026-08-25T20:05:00+02:00
---

# Crossroads Rotary Adapter

Crossroads 2.9.5 (Minecraft 1.19.2, Forge 43.2.x) exposes rotary power as `Capabilities.AXLE_CAPABILITY` / `IAxleHandler`. Alcoholic core and `minecraft-common` must not import `com.Da_Technomancer`. Mapping lives in `integration-crossroads`; the Forge attach lives in `integration-crossroads-1.19.2`.

```text
Crossroads axle network
        |
        v
Crossroads adapter
        |
        v
MechanicalDrivePort
        |
        v
Alcoholic machine
```

## Network participation

The adapter does **not** poll the neighboring Crossroads block each tick. It attaches `IAxleHandler` on the Malt Mill and on [[industrial-ports|kinetic ports]], implements `propagate`, and joins the axis list the way Crossroads `ModuleTE` machines do. Speed is `axis.getBaseSpeed() * rotRatio`, not a neighbor sample.

`MechanicalDrives.LocalAdapter` is the hook for a capability on the **machine itself**. Create still uses an adjacent `Probe`. Machines call `forMachine`, not a Crossroads `if`.

## Unit mapping (adapter only)

Alcoholic speed is RPM-equivalent. Crossroads speed is rad/s.

```text
RPM = rad/s × 60 / (2π)

1 Alcoholic capacity unit = 20 J
availableCapacity         = axleEnergy / 20   (capped)
work this tick            = requiredCapacity × 20 J  (removed from the axle)
MoI presented to the axis = 1.25
```

The core mechanical model is unchanged. A spinning axle with zero remaining energy is **stalled**: no free power from motion alone.

## Runtime

Crossroads requires Essentials (`modId=essentials`). Gradle `-PwithCrossroads=true` adds both Curse jars (Crossroads project **250231** file **4633319**; Essentials **293752** file **4633275**). Removing Crossroads must not prevent Alcoholic from loading.

`IAxleHandler.addEnergy` on this version takes two arguments. `getAngle` returns `float`.

## Related

- [[mechanical-drive-port]]
- [[electric-motor]]
- [[native-executor-invariant]]
- [[loader-independent-minecraft-architecture]]
- [[industrial-ports]]
- [[cursor-crossroads-electric-motor-session]]
- [[forge-1.19.2-crossroads-fe-verification]]
