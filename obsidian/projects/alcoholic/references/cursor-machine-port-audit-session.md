---
title: Cursor Machine Port Audit Session
category: references
tags: [minecraft, software-architecture, compatibility, type/concept, project/alcoholic]
aliases: [debug ports session, decorative taps]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/9c25457e-4350-4b33-9dc9-83e0eb1511d1/9c25457e-4350-4b33-9dc9-83e0eb1511d1.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/28/rollout-2026-08-28T18-13-41-01a04925-fc7d-7b00-a62c-418b9a0d4ce5.jsonl"
summary: >-
  Artisanal taps are decoration; fluid I/O is every face. Primitive-engine kinetic is only the right shaft. Electric-motor shaft rose to Y=10 with feet on the floor.
provenance:
  extracted: 0.88
  inferred: 0.10
  ambiguous: 0.02
created: 2026-08-28T19:15:00+02:00
updated: 2026-08-28T19:15:00+02:00
---

# Cursor Machine Port Audit Session

The user asked whether machines that should couple can face their ports, and whether FE motors have wire posts in both Java and the 3D model. Two layout commands were added: `/alcoholic debug ports fluid` and `energy`. The full operator list is on [[alcoholic-debug-commands]].

## Taps

Fermenter, oak barrel, mash tun, and kettle models have visible taps. `BlockHitResult` is ignored. There is no open-tap state and no flow animation. Buckets, bottles, and Create pipes use `IFluidHandler` on **all faces**. Misaligned artisanal taps are cosmetic. Directional fluid is the industrial `fluid_port`.

## Kinetic

The primitive engine grille (front) must not emit torque. Only the right-hand shaft face does. Java now checks that face. A north-facing engine drives **east**.

The electric motor shaft sat at Y ≈ 6.8 while the combustion shaft is Y = 10. The whole motor was raised 3.2 and the four feet extended to Y = 0 so it does not hover. The junction box may peek above 16 like the combustion shaft peeks sideways. Shafts spin while the motor is on.

## Distilled pages

- [[alcoholic-debug-commands]]
- [[artisanal-processing]]
- [[electric-motor]]
- [[primitive-combustion-engine-visual]]

## Related

- [[industrial-ports]]
- [[mechanical-drive-port]]
- [[curseforge-create2-deploy]]
- [[alcoholic]]
