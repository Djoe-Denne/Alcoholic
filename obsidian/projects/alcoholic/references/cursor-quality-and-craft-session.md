---
title: Cursor Quality and Craft Session
category: references
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [ADR-037 session, craft machines session]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/e7dd3c64-d28a-4c16-9d2a-d2dc3be2a3d2/e7dd3c64-d28a-4c16-9d2a-d2dc3be2a3d2.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/9d899962-c13d-474a-9cb7-59e0ef74ca71/9d899962-c13d-474a-9cb7-59e0ef74ca71.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/311c7aac-40ca-404b-b8c5-4d789891da7c/311c7aac-40ca-404b-b8c5-4d789891da7c.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/70bd14d5-9768-41dd-8ccf-8b3463491a05/70bd14d5-9768-41dd-8ccf-8b3463491a05.jsonl"
summary: >-
  Quality must be a reusable composition DAG. Craft is a third formed scale. Yeast stays an ingredient tag, not a drink-family class.
provenance:
  extracted: 0.8
  inferred: 0.16
  ambiguous: 0.04
created: 2026-09-08T20:30:00+02:00
updated: 2026-09-08T20:30:00+02:00
---

# Cursor Quality and Craft Session

Late August sessions asked whether Alcoholic was viable harvest-to-bottle, how to balance artisanal vs industrial, and whether yeast existed as a concept. Codex rollouts on 2026-08-29 mirror the same threads.

## Decisions

Drink quality is not a persisted 0–100 written by each machine. See [[emergent-quality-profile]] and [[quality-operator-dag]]. Artisanal stays high fidelity / low throughput; industrial wins volume and is capped; [[craft-scale-machines]] sit in between and form like industrials.

`#alcoholic:yeast` is already required on gameplay FERMENT ([[fermentation-physics]]). There is no yeast species Java type.

CO₂ as a drink-quality input was discussed for beer. Dissolved carbonation remains the CONDITION property `alcoholic:carbonation`; vented FERMENT CO₂ is not a quality axis. ^[inferred]

## Distilled pages

- [[emergent-quality-profile]]
- [[quality-operator-dag]]
- [[craft-scale-machines]]
- [[fermentation-physics]]

## Related

- [[industrial-processing]]
- [[grain-processing]]
- [[alcoholic]]
