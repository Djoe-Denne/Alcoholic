---
title: Trellis Training
category: concepts
tags: [minecraft, type/concept, project/alcoholic]
aliases: [palissage, trellis]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/bff6f9b3-354c-46b7-8b5e-c5162dc38730/bff6f9b3-354c-46b7-8b5e-c5162dc38730.jsonl"
summary: Vineyard posts, spool-placed wire, and derived training improve yield and quality without making untrained vines illegal.
provenance:
  extracted: 0.9
  inferred: 0.1
  ambiguous: 0.0
created: 2026-08-25T12:50:00+02:00
updated: 2026-08-25T12:50:00+02:00
---

# Trellis Training

Phase 2 adds vineyard infrastructure so a player can plant rows, not just isolated crop blocks.

## Blocks and tools

Inventory stays small: vineyard post, end post, trellis wire, trellis spool, and pruning shears. Visual canopy variation comes from blockstates and models, not extra items.

The spool connects two posts if the distance is valid. Wire is stored as axis-oriented segments. The spool spends one durability point per segment. Wire has no direct drop, which prevents duplicating a run by breaking segments; posts remain recoverable.

## Derived training

A vine can grow untrained, but worse. The Phase 2 brief used untrained yield 70% and quality 85% versus 100% when trellised. Training is derived at runtime only when a wire run reaches valid posts on both sides within the configured span.

When Vinery is present, its seed items may plant Alcoholic vines only in a valid trained row. Other Vinery interactions stay with Vinery. See [[semantic-crop-compatibility]].

## Why it matters later

A trained row is agricultural context for a future press, not a wine-specific machine. [[perennial-viticulture]] consumes training multipliers at harvest time and writes them into [[harvest-lot-metadata]].

## Related

- [[perennial-viticulture]]
- [[alcoholic]]
- [[cursor-phase-2-viticulture-session]]
- [[loader-independent-minecraft-architecture]]
- [[forge-1.19.2-phase-2-3-verification]]
