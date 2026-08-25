---
title: Industrial Ports
category: concepts
tags: [minecraft, compatibility, type/concept, project/alcoholic]
aliases: [fluid port, item port, kinetic port]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/dc49ea79-b9e5-4902-ad7f-795f762f8f52/dc49ea79-b9e5-4902-ad7f-795f762f8f52.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/14805ad4-dec0-4781-92a1-c718c5113dde/14805ad4-dec0-4781-92a1-c718c5113dde.jsonl"
summary: Fluid, item, and kinetic ports are views on the controller. Debug kinetic RPM NBT is compile-stripped outside debug.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-08-25T15:53:00+02:00
updated: 2026-08-25T19:21:00+02:00
---

# Industrial Ports

Ports belong to the [[industrial-multiblock]] shell. They do not own liquid, inventory, or process state; they expose selected controller capabilities.

## Modes

Fluid and item ports support input, output, or bidirectional modes. Structure state may further restrict access; a downsized machine remains drain-only until its contents fit.

## Capabilities

Ports expose standard Forge capability surfaces so Create pipes, pumps, belts, funnels, and chutes can automate the machine. Alcoholic does not duplicate Create transport infrastructure.

An unbound fluid port must reject fill. A review found isolated ports accepting liquid and swallowing it; that path was closed so a disconnected port cannot eat a batch.

## Kinetic port

The kinetic port is a [[mechanical-drive-port]] relay. A primitive combustion engine can sit against it; Create shafts connect when Create is present. Machines depend on Alcoholic speed/capacity, not Create RPM.

Vanilla and Create kinetic ports persist debug RPM only when `AlcoholicDebug.ENABLED` is compiled true. Published jars neither write nor read `Rpm` / `DebugRpm`. See [[mechanical-drive-port#Debug RPM is compile-time]].

## Related

- [[industrial-multiblock]]
- [[industrial-processing]]
- [[mechanical-drive-port]]
- [[native-executor-invariant]]
- [[create-press-adapter]]
- [[liquid-batch]]
- [[cursor-phase-6-industrial-session]]
- [[cursor-create-independence-session]]
- [[forge-1.19.2-phase-6-verification]]
