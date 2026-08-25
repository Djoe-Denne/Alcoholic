---
title: Cursor Phase 7A Grain Session
category: references
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/95c81b7c-fa88-4055-9741-14cb948964c9/95c81b7c-fa88-4055-9741-14cb948964c9.jsonl"
summary: Session that implemented beer as generic MALT, MILL, MASH, and BOIL plus barley and hops agriculture.
provenance:
  extracted: 0.88
  inferred: 0.1
  ambiguous: 0.02
created: 2026-08-25T18:55:00+02:00
updated: 2026-08-25T18:55:00+02:00
---

# Cursor Phase 7A Grain Session

The user required a second beverage family that stress-tests the generic engine. Scope stayed alcohol production: no drinking, taverns, or economy. Wine PRESS/FERMENT/AGE and industrial infrastructure already existed.

## Decisions locked in the session

- Implement beer through ingredients, crops, process types, definitions, executors, and data. Stop if generic processing needs drink-family branches.
- Authoritative DAG: barley → malt → mill → mash → boil → ferment. No assumption these steps apply to other beverages.
- ADR-024 through ADR-029: malt kiln profiles as data; mixed solid/liquid ports; mash thermal yield; boil hop extraction; Create mill recipes; reusable crop providers for barley/hops (Brewery preferred, ids stay registered).
- Native executors for MALT, MASH, BOIL: malting floor, mash tun, brewing kettle. MILL initially had no Alcoholic mill (ADR-028); Create millstone/crushing plus a temporary MILL path on the malting floor. [[native-executor-invariant]] later superseded that mill decision.
- Shipped `alcoholic:beer` ends after FERMENT. AGE is not injected. Pale malt is the default overlapping `MALT` binding.
- A Create runtime bridge copies malt NBT onto millstone/crushing output so MILL does not drop kiln properties.

## Session outcome

`.\gradlew check` passed. `runGameTestServer -PwithCreate=true` reported **42/42** in this session (crops, malting, mash tun plus Create tank, kettle, ferment). That run predates the Malt Mill and primitive engine.

## Distilled pages

- [[grain-processing]]
- [[semantic-crop-compatibility]]
- [[native-executor-invariant]]
- [[forge-1.19.2-phase-7a-verification]]
- [[alcoholic]]

## Related

- [[cursor-phase-6-industrial-session]]
- [[cursor-create-independence-session]]
- [[process-capability-graph]]
- [[beverage-framework]]
