---
title: Cask Imprint
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [ADR-039, barrel imprint, seasoning stain]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/86cc18fc-6ee4-4cf0-8dbb-32844df14b88/86cc18fc-6ee4-4cf0-8dbb-32844df14b88.jsonl"
  - "C:/Users/djden/source/repos/Alcoholic/docs/adr/ADR-039-cask-imprint.md"
summary: >-
  Emptying a barrel snapshots transferable chemistry. The next fill leaks that stain upward; wood species stays out of scope.
provenance:
  extracted: 0.88
  inferred: 0.1
  ambiguous: 0.02
created: 2026-09-08T20:30:00+02:00
updated: 2026-09-08T20:30:00+02:00
---

# Cask Imprint

The shipped vessel is still one oak barrel ([[vessel-and-environment]]). A cherry cask would be a new Java block plus profile, not a texture swap. History used to store previous liquid ids without changing the next fill. ADR-039 makes the previous occupant stain chemistry.

## Snapshot on empty

On empty, the vessel snapshots a `PropertyBag` of transferable axes. The oak barrel uses the matching AGE recipe's `imprint_properties` when present; otherwise acidity, sugar, tannin, aroma, and `roast_intensity`. A sip is scaled by peak volume / capacity for that fill. A definition swap without an empty tick records the previous occupant first.

That imprint is faded × 0.55 and, per axis present on both sides, averaged with the new snapshot.

## Leak during AGE

`AgingPhysics` leaks imprint **upward only**, linearly in the unseasoned maturity step, capped at the imprint value. Leak continues after maturity until the batch reaches the imprint on each axis. Default `imprint_transfer` is 0.20 per unseasoned maturity cycle. The industrial aging vessel shares the tracker. AGE never reads a beverage id; [[quality-operator-dag]] interprets the axes.

Inspect and barrel status show the imprint.

## Related

- [[aging-process]]
- [[vessel-and-environment]]
- [[quality-operator-dag]]
- [[cursor-cask-hedging-stands-session]]
- [[alcoholic]]
