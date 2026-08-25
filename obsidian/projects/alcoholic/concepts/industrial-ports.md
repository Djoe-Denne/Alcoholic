---
title: Industrial Ports
category: concepts
tags: [minecraft, compatibility, type/concept, project/alcoholic]
aliases: [fluid port, item port, kinetic port]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/dc49ea79-b9e5-4902-ad7f-795f762f8f52/dc49ea79-b9e5-4902-ad7f-795f762f8f52.jsonl"
summary: Fluid, item, and kinetic ports are views on the controller. Create supplies shafts; Alcoholic does not add pipes.
provenance:
  extracted: 0.88
  inferred: 0.12
  ambiguous: 0.0
created: 2026-08-25T15:53:00+02:00
updated: 2026-08-25T16:01:00+02:00
---

# Industrial Ports

Ports belong to the [[industrial-multiblock]] shell. They do not own liquid, inventory, or process state; they expose selected controller capabilities.

## Modes

Fluid and item ports support input, output, or bidirectional modes. Structure state may further restrict access; a downsized machine remains drain-only until its contents fit.

## Capabilities

Ports expose standard Forge capability surfaces so Create pipes, pumps, belts, funnels, and chutes can automate the machine. Alcoholic does not duplicate Create transport infrastructure.

## Kinetic port

The kinetic port adapts Create rotational power to the industrial press while keeping Create optional. The common multiblock framework depends on a kinetic capability, not on Create classes.

## Related

- [[industrial-multiblock]]
- [[industrial-processing]]
- [[create-press-adapter]]
- [[liquid-batch]]
- [[cursor-phase-6-industrial-session]]
- [[forge-1.19.2-phase-6-verification]]
