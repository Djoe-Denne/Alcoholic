---
title: Forge 1.19.2 Phase 6 Verification
category: skills
tags: [minecraft, testing, type/procedure, project/alcoholic]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/dc49ea79-b9e5-4902-ad7f-795f762f8f52/dc49ea79-b9e5-4902-ad7f-795f762f8f52.jsonl"
summary: After multiblock or industrial-executor changes, rerun purity, architecture, unit tests, industrial resources, and GameTests including industrial_pad.
provenance:
  extracted: 0.88
  inferred: 0.12
  ambiguous: 0.0
created: 2026-08-25T15:30:00+02:00
updated: 2026-08-25T16:01:00+02:00
---

# Forge 1.19.2 Phase 6 Verification

Start from [[forge-1.19.2-phase-5-verification]], then add the industrial gates.

1. `.\gradlew checkArchitecture`
2. `.\gradlew checkBeverageFrameworkPurity`
3. `.\gradlew :alcoholic-api:test :domain:test :application:test :minecraft-common:test`
4. `python tools/generate_industrial_resources.py` if industrial blocks, tags, or machines changed
5. `.\gradlew :platform-forge-1.19.2:test`
6. `.\gradlew :platform-forge-1.19.2:runDataCommon` if you prefer datagen over the Python writer
7. `.\gradlew runGameTestServer`
8. Optional `.\gradlew runGameTestServer -PwithCreate=true` for Create pipe and kinetic paths

## What the extra gates protect

Purity covers the multiblock domain, machine application layer, and Minecraft adapters. Generic engine sources remain beverage-agnostic.

Tests cover formation, resizing, persistence, port access, executor equivalence, performance, kinetic gating, and the crush-zone rules.

The Phase 6 session closed with `.\gradlew check --offline` and **32/32** `runGameTestServer` tests.

## Related

- [[industrial-multiblock]]
- [[industrial-processing]]
- [[industrial-ports]]
- [[forge-1.19.2-phase-5-verification]]
- [[cursor-phase-6-industrial-session]]
