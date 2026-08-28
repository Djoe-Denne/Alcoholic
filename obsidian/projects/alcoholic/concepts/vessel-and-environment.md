---
title: Vessel and Environment
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [VesselProfile, EnvironmentProfile, oak barrel]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/c2ca3b27-ad63-4be9-af24-47c49c111f2f/c2ca3b27-ad63-4be9-af24-47c49c111f2f.jsonl"
summary: Vessel and cellar profiles live outside BlockEntities. The oak barrel is one adapter. Aging runs only while the chunk is loaded.
provenance:
  extracted: 0.88
  inferred: 0.1
  ambiguous: 0.02
created: 2026-08-25T14:40:00+02:00
updated: 2026-08-28T19:15:00+02:00
---

# Vessel and Environment

Long-running [[aging-process]] needs material and cellar effects without coupling domain physics to a Minecraft block entity.

## Profiles, not machines

`VesselProfile` records material, capacity, process capabilities, permeability, wood and oxidation multipliers, and optional opaque barrel history. `EnvironmentProfile` records temperature, stability, and whether the site is sheltered. Neither stores a biome id.

Profiles register on `AlcoholicApi.vessels()` and freeze with the rest of the [[public-extension-api]]. A datapack can attach AGE to an existing vessel without a new Java profile. A new *block* remains Java.

The shipped oak barrel is one content adapter of `alcoholic:oak_barrel` (8000 mB, capability `alcoholic:age`). A used barrel (history usage or previous contents) applies a 1.15 seasoning multiplier. Emptying records the previous liquid id and does not wipe history. The cube placeholder is now a lying cask voxel (locked oak, iron hoops); the tap remains decorative. See [[artisanal-machine-voxel-models]] and [[cursor-voxel-campaign-session]].

## Cellar sampling

The Minecraft adapter samples `canSeeSky`, Y versus sea level, vanilla biome temperature, and the six solid neighbours. There is no cubic region scan. The sample is cached: on place, every 200 ticks, and when a neighbour changes.

## Loaded-only catch-up

No aging while the chunk is unloaded. The barrel persists `lastProcessedGameTime`. On load it skips the world-time gap and records *now*. While loaded, `delta` is capped at 24000 ticks (one Minecraft day). That blocks AFK-outside-chunk exploits and stays deterministic.

`ProcessContext` exposes optional `vessel()`, `environment()`, and `gameTime()`. AGE falls back to oak plus a temperate default when a machine omits them.

## Related

- [[aging-process]]
- [[artisanal-processing]]
- [[public-extension-api]]
- [[liquid-batch]]
- [[cursor-phase-5-aging-session]]
- [[forge-1.19.2-phase-5-verification]]
