---
title: Forge 1.19.2 Phase 1 Verification
category: skills
tags: [minecraft, testing, type/procedure, project/alcoholic]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b9ff420b-fd71-4089-83bc-cc4766880b14/b9ff420b-fd71-4089-83bc-cc4766880b14.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/bff6f9b3-354c-46b7-8b5e-c5162dc38730/bff6f9b3-354c-46b7-8b5e-c5162dc38730.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b282f8bd-e540-4028-93d1-896905419dcd/b282f8bd-e540-4028-93d1-896905419dcd.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/a6b5797c-82f8-4021-9d63-10a82fed6899/a6b5797c-82f8-4021-9d63-10a82fed6899.jsonl"
summary: Phase 1 Forge checks for datagen, architecture, tests, and the JAR. Later slices add vine GameTests, purity, and processing gates.
provenance:
  extracted: 0.88
  inferred: 0.12
  ambiguous: 0.0
created: 2026-08-25T09:20:00+02:00
updated: 2026-08-25T14:10:00+02:00
---

# Forge 1.19.2 Phase 1 Verification

Use this sequence after changing the Phase 1 crop slice:

1. Run `.\gradlew runData`.
2. Hash generated JSON before and after a second run to confirm deterministic output.
3. Run `.\gradlew checkArchitecture`.
4. Run `.\gradlew clean build`.
5. Run `.\gradlew test`.
6. Run `.\gradlew runGameTestServer`.
7. Resolve the optional Create profile with `-PwithCreate=true`.
8. Inspect the final reobfuscated JAR for metadata, core classes, semantic tags, and the Forge biome modifier.

## Runtime checks

Phase 1 originally shipped three GameTests (bonemeal, immature rejection, mature harvest). Phase 2 replaced that crop-age suite with six viticulture GameTests. After later-phase work, continue with [[forge-1.19.2-phase-2-3-verification]] and [[forge-1.19.2-phase-4-verification]].

Resource-contract tests assert optional Vinery tag references and the `forge:not(forge:mod_loaded("vinery"))` worldgen condition.

Forge's GameTest task can return a successful Gradle exit even when an early server startup fails. Always inspect the server log for `All ... required tests passed`, not only the process exit code.

## Known setup details

- Gradle 8.8 runs on the host JDK while the Java 17 toolchain compiles and launches Minecraft.
- ForgeGradle only recognizes the standard `data` run type automatically. A custom-named datagen run needs inherited launcher configuration; the implemented solution uses one standard run that writes common and Forge-specific outputs.
- Create 0.5.1f is runtime-optional. Phase 1 had no operational processing port; Phase 4 added a conditional compacting adapter. See [[create-press-adapter]].

## Related

- [[alcoholic]]
- [[loader-independent-minecraft-architecture]]
- [[semantic-crop-compatibility]]
- [[forge-1.19.2-phase-2-3-verification]]
- [[forge-1.19.2-phase-4-verification]]
- [[cursor-phase-1-foundation-session]]
