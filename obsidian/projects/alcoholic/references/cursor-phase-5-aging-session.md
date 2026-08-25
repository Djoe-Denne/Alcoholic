---
title: Cursor Phase 5 Aging Session
category: references
tags: [minecraft, software-architecture, testing, project/alcoholic]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/c2ca3b27-ad63-4be9-af24-47c49c111f2f/c2ca3b27-ad63-4be9-af24-47c49c111f2f.jsonl"
summary: Cursor session that implemented aging, vessels, split/merge/blend, oak barrel, cellar sampling, and bottling.
provenance:
  extracted: 0.9
  inferred: 0.08
  ambiguous: 0.02
created: 2026-08-25T14:40:00+02:00
updated: 2026-08-25T15:53:00+02:00
---

# Cursor Phase 5 Aging Session

The user required the Phase 4 [[liquid-batch]] model to survive long-term storage: aging, split, merge, controlled blend, oak barrel, cellar environment, and a consumer bottle. Wine is the gameplay example. The engine stays beverage-agnostic. Whisky, beer, cider, and rum AGE graphs are data-only fixtures.

The brief drafted ADR numbers that already belonged to Phase 4. Shipped records are ADR-011 ([[aging-process]]), ADR-012 ([[vessel-and-environment]]), ADR-013 ([[blend-versus-tank-merge]]), ADR-014 ([[batch-provenance]]), ADR-015 (loaded-only catch-up), and ADR-016 ([[bottled-beverage-snapshot]]).

## Decisions locked in the session

- AGE is optional. The engine never injects it. Empty config is valid. Optional `output.liquid` may rename on maturity ≥ threshold.
- No aging while unloaded. `skipUnloadGap` on load. Catch-up only while loaded, cap 24000 ticks.
- Tanks merge the same definition only. Distinct definitions blend only through an explicit crock action (sneak + empty hand).
- Provenance is flattened: 16 entries, 0.5% cutoff, no parent tree. NBT v2; v1 migrates to empty provenance.
- A bottle is a 250 mB consumer snapshot, not a portable clock.
- Oak barrel 8000 mB; blending crock two 4000 mB tanks; `/alcoholic inspect`; metadata-lost tooltip when a foreign tank strips `Version`.
- Distillation gameplay, industry, drunkenness, and many woods stay out of scope.

## Implementation outcome

Wine finished liquids (`red_wine`, `white_wine`) plus AGE/BLEND processes and matching fluids. Fixture AGE nodes for whisky (`testpack:age_new_make`), beer, cider, and rum. Purity, architecture, unit tests, and 19 GameTests passed. Create paths remain optional.

The same transcript later grew with two `/wiki-ingest` turns. No new Phase 5 product decisions were added; only the source hash needed a refresh. [[cursor-phase-6-industrial-session]] is a later chat.

## Distilled pages

- [[aging-process]]
- [[vessel-and-environment]]
- [[batch-provenance]]
- [[blend-versus-tank-merge]]
- [[bottled-beverage-snapshot]]
- [[liquid-batch]]
- [[artisanal-processing]]
- [[process-capability-graph]]
- [[alcoholic]]
- [[forge-1.19.2-phase-5-verification]]
