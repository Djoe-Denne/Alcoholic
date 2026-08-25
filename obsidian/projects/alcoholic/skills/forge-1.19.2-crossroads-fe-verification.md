---
title: Forge 1.19.2 Crossroads and FE Verification
category: skills
tags: [minecraft, testing, type/procedure, project/alcoholic]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/1ca21b6f-a077-4686-8f47-c41ee6e59fc7/1ca21b6f-a077-4686-8f47-c41ee6e59fc7.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/ede9eb58-09ac-4ccc-9fea-74a521453d14/ede9eb58-09ac-4ccc-9fea-74a521453d14.jsonl"
summary: After mechanical-supply changes, rerun architecture, mapping tests, FE GameTests, then optional Crossroads and IE client paths.
provenance:
  extracted: 0.84
  inferred: 0.10
  ambiguous: 0.06
created: 2026-08-25T20:05:00+02:00
updated: 2026-08-25T20:05:00+02:00
---

# Forge 1.19.2 Crossroads and FE Verification

Start from [[forge-1.19.2-phase-7a-verification]], then add supply-adapter gates.

1. `.\gradlew checkArchitecture` — domain / application / `minecraft-common` must not import Crossroads, Create, or IE. `platform-forge-1.19.2` must not import `com.Da_Technomancer` or Create types.
2. `.\gradlew :integration-crossroads:test` — rad/s → RPM, joules → capacity, stall when energy is zero.
3. `.\gradlew :minecraft-common:test` — `EnergyBuffer`, load-proportional FE.
4. `.\gradlew :platform-forge-1.19.2:test` and `runGameTestServer` — motor + Malt Mill, idle motor does not full-drain, empty motor emits no drive, FE capability present, industrial press from motor, Alcoholic loads without Crossroads.
5. `.\gradlew runClient -PwithImmersiveEngineering=true` — IE generator/capacitor → wire → Energy Connector → [[electric-motor]] → mill. No custom IE transport.
6. `.\gradlew runClient -PwithCrossroads=true` — also pulls Essentials. Master axis → axle → mill face: speed, direction, energy loss on work, stall when joules run out.

GameTests that attach `AXLE_CAPABILITY` use reflection so `platform-forge` does not import Crossroads. They only assert the capability when Crossroads is loaded.

Industrial press FE drain after the winning-port fix is not covered by `debugForceRpm` GameTests. ^[ambiguous]

## Related

- [[electric-motor]]
- [[crossroads-rotary-adapter]]
- [[mechanical-drive-port]]
- [[industrial-ports]]
- [[cursor-crossroads-electric-motor-session]]
- [[forge-1.19.2-phase-7a-verification]]
