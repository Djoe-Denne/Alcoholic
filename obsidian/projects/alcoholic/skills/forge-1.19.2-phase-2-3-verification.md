---
title: Forge 1.19.2 Phase 2 and 3 Verification
category: skills
tags: [minecraft, testing, type/procedure, project/alcoholic]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/bff6f9b3-354c-46b7-8b5e-c5162dc38730/bff6f9b3-354c-46b7-8b5e-c5162dc38730.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b282f8bd-e540-4028-93d1-896905419dcd/b282f8bd-e540-4028-93d1-896905419dcd.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/a6b5797c-82f8-4021-9d63-10a82fed6899/a6b5797c-82f8-4021-9d63-10a82fed6899.jsonl"
summary: Extra checks after viticulture or beverage-catalog changes: purity, atomic catalog reload, and the original six vine GameTests.
provenance:
  extracted: 0.84
  inferred: 0.16
  ambiguous: 0.0
created: 2026-08-25T12:50:00+02:00
updated: 2026-08-25T14:10:00+02:00
---

# Forge 1.19.2 Phase 2 and 3 Verification

Start from [[forge-1.19.2-phase-1-verification]], then add:

1. `.\gradlew checkArchitecture`
2. `.\gradlew checkBeverageFrameworkPurity`
3. `.\gradlew :alcoholic-api:test :domain:test :application:test :integration-test-addon:test`
4. `.\gradlew :minecraft-common:test :platform-forge-1.19.2:test`
5. `.\gradlew clean build`
6. Two `.\gradlew runData` passes; compare generated JSON, ignoring `.cache`
7. `.\gradlew runGameTestServer` and read the log for the six viticulture tests

## What the extra gates protect

Purity fails if generic engine sources name a drink family. Catalog tests must reject cycles and unknown process types without replacing a valid snapshot. The fake addon must compile against `alcoholic-api` only.

The original six GameTests remain viticulture-only: mature harvest, immature rejection, dormancy, a second harvest without replanting, wire/training, and provider behavior. Phase 3 added no GameTest machinery. Phase 4 added processing GameTests; use [[forge-1.19.2-phase-4-verification]] after PRESS, FERMENT, or fluid-adapter changes.

Optional `-PwithVinery=true` still exercises seed planting into a trained row. `-PwithCreate=true` now also runs Create tank movement tests from Phase 4; without Create those tests no-op.

## Related

- [[forge-1.19.2-phase-1-verification]]
- [[forge-1.19.2-phase-4-verification]]
- [[perennial-viticulture]]
- [[beverage-framework]]
- [[public-extension-api]]
- [[cursor-phase-2-viticulture-session]]
- [[cursor-phase-3-beverage-framework-session]]
- [[cursor-phase-4-processing-session]]
