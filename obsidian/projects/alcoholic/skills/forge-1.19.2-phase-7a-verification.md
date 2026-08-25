---
title: Forge 1.19.2 Phase 7A Verification
category: skills
tags: [minecraft, testing, type/procedure, project/alcoholic]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/95c81b7c-fa88-4055-9741-14cb948964c9/95c81b7c-fa88-4055-9741-14cb948964c9.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/14805ad4-dec0-4781-92a1-c718c5113dde/14805ad4-dec0-4781-92a1-c718c5113dde.jsonl"
summary: After grain or mechanical-executor changes, rerun purity, architecture, unit tests, GrainGameTests, and the debug-NBT compile gate.
provenance:
  extracted: 0.88
  inferred: 0.08
  ambiguous: 0.04
created: 2026-08-25T18:55:00+02:00
updated: 2026-08-25T19:21:00+02:00
---

# Forge 1.19.2 Phase 7A Verification

Start from [[forge-1.19.2-phase-6-verification]], then add grain and mechanical gates.

1. `.\gradlew checkArchitecture` — must fail if `platform-forge-1.19.2` imports `com.simibubi.create.`
2. `.\gradlew checkBeverageFrameworkPurity`
3. `.\gradlew :alcoholic-api:test :domain:test :application:test :minecraft-common:test`
4. `.\gradlew :integration-create:test` when mill/press translators or Create probes changed
5. `.\gradlew :platform-forge-1.19.2:test`
6. `.\gradlew :platform-forge-1.19.2:runDataCommon` if grain blocks, tags, or recipes changed
7. `.\gradlew runGameTestServer` — native path: barley, hops, malting floor, malt mill stall/power, mash tun, kettle, ferment
8. `.\gradlew runGameTestServer -PwithCreate=true` — millstone/crushing recipes, mash tun → Create tank, kinetic adapters
9. After changing `AlcoholicDebug` generation, compile with `"-Palcoholic.debug=false"` and confirm `javap` no longer shows `Rpm` / `DebugRpm` on kinetic-port save/load. See [[mechanical-drive-port#Debug RPM is compile-time]]. PowerShell must quote `-Palcoholic.debug=…`.

## What the extra gates protect

Purity still forbids drink-family branches in generic processing. Architecture keeps Create types inside `integration-create-forge-1.19.2`.

`GrainGameTests` covers crop stages, hops-on-trellis, malting floor, mill without power, mill with properties, mash tun wort/spent grain and reload, optional Create tank move, kettle hopped wort, kettle → artisanal fermenter, hopped-wort FERMENT, and kettle reload.

The Phase 7A grain session closed Create GameTests at **42/42**. After the Malt Mill and primitive engine, compile and unit tests passed; re-run the GameTest server before treating the mechanical cut as in-game verified. ^[ambiguous]

## Related

- [[grain-processing]]
- [[mechanical-drive-port]]
- [[native-executor-invariant]]
- [[forge-1.19.2-phase-6-verification]]
- [[cursor-phase-7a-grain-session]]
- [[cursor-create-independence-session]]
