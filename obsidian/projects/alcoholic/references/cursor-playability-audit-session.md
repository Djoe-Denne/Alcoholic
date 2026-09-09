---
title: Cursor Playability Audit Session
category: references
tags: [minecraft, testing, type/concept, project/alcoholic]
aliases: [audit jouabilité, C1-C26 session]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/112827ce-4901-4cfd-8dad-1e626367ee8f/112827ce-4901-4cfd-8dad-1e626367ee8f.jsonl"
  - "C:/Users/djden/source/repos/Alcoholic/docs/audits/current-playability-audit.md"
summary: >-
  Functional map then remediations C1–C26. Wine/beer through bottle, craft FORMED nodes, young beer, bottle-stand crafts, stand-network crash fix.
provenance:
  extracted: 0.84
  inferred: 0.14
  ambiguous: 0.02
created: 2026-09-09T16:00:00+02:00
updated: 2026-09-09T16:00:00+02:00
---

# Cursor Playability Audit Session

The user asked for a full functional map and playability blind spots, then to fix every finding C1–C15 and C16–C26. Bottling was in scope for the audit; drinking was not.

The session wrote `docs/audits/current-playability-audit.md`, characterization tests, GUI layouts, beer `young` + optional `condition`, vanilla AGE parent, craft FORMED nodes, blend young→young, and bottle-stand recipes. Native GameTests (148) passed after a non-deterministic hull-order test fix. Create GameTests ended non-zero only while C26 (stands) was still open; leftover mash and the FE motor already passed.

The remapped jar was copied to Create 2 Mekanism. F3+T does not reload a jar; the client must restart. See [[curseforge-create2-deploy]].

## Distilled pages

- [[playability-audit]]
- [[wine-beer-progression-graph]]
- [[grain-processing]]
- [[craft-scale-machines]]
- [[bottle-stands]]
- [[blend-versus-tank-merge]]

## Related

- [[cursor-machine-gui-and-malting-session]]
- [[alcoholic]]
