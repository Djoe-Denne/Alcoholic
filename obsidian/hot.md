---
title: Hot Cache
updated: 2026-08-25T19:21:00+02:00
---

## Recent Activity

- Synced Alcoholic — compile-time `AlcoholicDebug.ENABLED` so kinetic `Rpm` / `DebugRpm` NBT is dead-stripped from published jars.
- Earlier: Create independence (ADR-030, Malt Mill, primitive engine) and Phase 7A grain DAG.
- Earlier: industrial multiblocks and optional Create ports.

## Active Threads

- Alcoholic mechanical power: native engine first, Create optional. Debug kinetic NBT must not survive in production bytecode.
- Full `runGameTestServer` after the mechanical cut is still unmarked in-wiki. ^[ambiguous]

## Key Takeaways

Java has no `#if DEBUG`. A generated `static final boolean` is the production gate: `javac` removes save and load, so injected schematic NBT cannot restore infinite-capacity drive. Both directions must be gated. GameTests keep in-memory `debugForceRpm`. Controller `LastRpm` is gameplay state, not the debug hook.

## Flagged Contradictions
