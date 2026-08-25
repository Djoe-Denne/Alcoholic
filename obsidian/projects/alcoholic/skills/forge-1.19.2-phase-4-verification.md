---
title: Forge 1.19.2 Phase 4 Verification
category: skills
tags: [minecraft, testing, type/procedure, project/alcoholic]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/a6b5797c-82f8-4021-9d63-10a82fed6899/a6b5797c-82f8-4021-9d63-10a82fed6899.jsonl"
summary: After PRESS, FERMENT, or fluid-adapter changes, rerun architecture purity, domain and Forge tests, datagen, and the twelve processing GameTests.
provenance:
  extracted: 0.88
  inferred: 0.12
  ambiguous: 0.0
created: 2026-08-25T14:10:00+02:00
updated: 2026-08-25T14:40:00+02:00
---

# Forge 1.19.2 Phase 4 Verification

Start from [[forge-1.19.2-phase-2-3-verification]], then add the processing gates.

1. `.\gradlew checkArchitecture`
2. `.\gradlew checkBeverageFrameworkPurity`
3. `.\gradlew :alcoholic-api:test :domain:test :application:test :integration-create:test :integration-test-addon:test`
4. `.\gradlew :minecraft-common:test :platform-forge-1.19.2:test`
5. `.\gradlew runData` if liquid, process, or Create recipe JSON changed
6. `.\gradlew runGameTestServer`
7. Optional `.\gradlew runGameTestServer -PwithCreate=true` to exercise Create tank GameTests

## What the extra gates protect

Purity still fails if generic engine sources name `Wine`, `Beer`, `Whisky`, or `Rum`. Catalog tests must accept omitted PRESS or FERMENT, reject cycles, and keep cider as `testpack:` data only.

Domain and application tests cover volume-weighted [[liquid-batch]] merge, [[fermentation-physics]], agricultural transfer into must, and DAG fixtures including a press→ferment+infuse branch.

Forge GameTests cover pressing, distinct lots, press-to-fermenter transfer, and progressive fermentation. Create tank movement tests are no-ops unless Create is on the classpath. Generated Create recipes must remain `forge:conditional`.

Textures for new processing items still come from `python tools/generate_textures.py` when assets change.

## Related

- [[forge-1.19.2-phase-1-verification]]
- [[forge-1.19.2-phase-2-3-verification]]
- [[artisanal-processing]]
- [[create-press-adapter]]
- [[public-extension-api]]
- [[cursor-phase-4-processing-session]]
- [[forge-1.19.2-phase-5-verification]]
