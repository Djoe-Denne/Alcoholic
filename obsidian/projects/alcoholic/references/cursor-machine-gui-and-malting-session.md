---
title: Cursor Machine GUI and Malting Session
category: references
tags: [minecraft, compatibility, type/concept, project/alcoholic]
aliases: [JEI telemetry session, craft malt house overlay]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/c1fe1ca7-b44b-4208-8155-4cb69e690435/c1fe1ca7-b44b-4208-8155-4cb69e690435.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/312a15fc-37d5-4e6c-bd14-00f96c8bcefb/312a15fc-37d5-4e6c-bd14-00f96c8bcefb.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/d649689c-f6f6-42e4-bef7-be193c16d93e/d649689c-f6f6-42e4-bef7-be193c16d93e.jsonl"
summary: >-
  Controller telemetry sits left of the inventory so JEI does not cover it. Malting keeps duration and accepts multiple lots. Mega-mesh is art-size only.
provenance:
  extracted: 0.88
  inferred: 0.1
  ambiguous: 0.02
created: 2026-09-08T20:30:00+02:00
updated: 2026-09-09T16:00:00+02:00
---

# Cursor Machine GUI and Malting Session

A formed controller overlay listed temperature, stage, and drive to the **right** of the inventory. JEI's ingredient list sat on top of it. The fix is [[machine-controller-telemetry]]: panel on the left plus `getGuiExtraAreas`.

Malting at 600 s felt too slow. The chosen path is **not** a shorter official duration: the malt house / floor should run multiple ingredient lots in parallel while `ticks_to_complete` stays datapack-authored. See [[grain-processing]].

A 3×3×3 craft malt house looked correct; larger formed sizes broke the overlay. That matches [[formed-multiblock-visual]]: mega-mesh only at art size, 9-slice hull otherwise.

The remapped jar was deployed to Create 2 Mekanism via [[curseforge-create2-deploy]]. F3+T does not reload a new jar.

## Distilled pages

- [[machine-controller-telemetry]]
- [[formed-multiblock-visual]]
- [[craft-scale-machines]]
- [[grain-processing]]
- [[playability-audit]]

## Related

- [[process-display-and-recipe-viewers]]
- [[alcoholic]]
