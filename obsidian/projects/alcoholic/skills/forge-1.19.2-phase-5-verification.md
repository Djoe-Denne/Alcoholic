---
title: Forge 1.19.2 Phase 5 Verification
category: skills
tags: [minecraft, testing, type/procedure, project/alcoholic]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/c2ca3b27-ad63-4be9-af24-47c49c111f2f/c2ca3b27-ad63-4be9-af24-47c49c111f2f.jsonl"
summary: After AGE, vessel, blend, or bottle changes, rerun purity, architecture, unit tests, datagen, and the nineteen GameTests.
provenance:
  extracted: 0.88
  inferred: 0.12
  ambiguous: 0.0
created: 2026-08-25T14:40:00+02:00
updated: 2026-08-25T14:40:00+02:00
---

# Forge 1.19.2 Phase 5 Verification

Start from [[forge-1.19.2-phase-4-verification]], then add the storage gates.

1. `.\gradlew checkArchitecture`
2. `.\gradlew checkBeverageFrameworkPurity`
3. `.\gradlew :alcoholic-api:test :domain:test :application:test :minecraft-common:test`
4. `.\gradlew :platform-forge-1.19.2:test`
5. `python tools/generate_textures.py` if barrel, crock, or bottle assets changed
6. `.\gradlew :platform-forge-1.19.2:runDataCommon` if wine AGE, finished liquids, or fluids changed
7. `.\gradlew runGameTestServer`
8. Optional `.\gradlew runGameTestServer -PwithCreate=true` for Create pipe paths into the oak barrel

## What the extra gates protect

Purity now also covers `domain/vessel`, Minecraft environment sampling, and inspect helpers. Generic engine sources still must not name `Wine`, `Beer`, `Whisky`, or `Rum`.

Domain tests cover [[liquid-batch]] split, merge, [[batch-provenance]], and [[aging-process]] steps. The Phase 5 Cursor session recorded 19/19 GameTests without Create on the classpath. Create transfer tests stay no-ops unless `-PwithCreate=true`.

Wine AGE content uses a wide operating band (0–36 °C) so overworld plains can age. That is content, not engine policy.

## Related

- [[forge-1.19.2-phase-4-verification]]
- [[artisanal-processing]]
- [[liquid-batch]]
- [[aging-process]]
- [[vessel-and-environment]]
- [[blend-versus-tank-merge]]
- [[bottled-beverage-snapshot]]
- [[cursor-phase-5-aging-session]]
