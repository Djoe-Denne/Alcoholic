---
title: Cursor Formed Hull Session
category: references
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [beer industrial place session]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/7709dbb4-6adc-48f3-ad6f-7de969fd9878/7709dbb4-6adc-48f3-ad6f-7de969fd9878.jsonl"
summary: >-
  Debug place beer industrial spawned loose cubes. The fix is a 9-slice welded hull at every size plus mega-mesh at art size.
provenance:
  extracted: 0.88
  inferred: 0.10
  ambiguous: 0.02
created: 2026-08-28T19:15:00+02:00
updated: 2026-08-28T19:15:00+02:00
---

# Cursor Formed Hull Session

`/alcoholic debug place beer industrial ~ ~ ~` laid independent blocks that did not match the formed multiblock recipe. Formed `.bbmodel` mega-meshes already existed under `art/` but Java still showed the hollow cuboid parts.

The plan **Hull soudé + mega-mesh à la taille d’art** implemented [[formed-multiblock-visual]] without changing the validator or capacity. The jar was rebuilt into [[curseforge-create2-deploy]].

## Distilled pages

- [[formed-multiblock-visual]]
- [[alcoholic-debug-commands]]

## Related

- [[industrial-multiblock]]
- [[cursor-artisanal-brewery-guide-session]]
- [[cursor-voxel-campaign-session]]
- [[alcoholic]]
