---
title: Cursor Artisanal Brewery Guide Session
category: references
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [brasserie artisanale guide]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/d120a5cc-91ff-47b3-99d0-392583b27834/d120a5cc-91ff-47b3-99d0-392583b27834.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-44-02-01a03f62-e9dc-7663-b9a1-4d28c8337b60.jsonl"
summary: >-
  QMD player guide for alcoholic:beer plus operator /alcoholic debug place commands that spawn artisanal or industrial beer lines.
provenance:
  extracted: 0.88
  inferred: 0.1
  ambiguous: 0.02
created: 2026-08-27T16:20:00+02:00
updated: 2026-08-27T16:20:00+02:00
---

# Cursor Artisanal Brewery Guide Session

The user asked QMD (`alcoholic-wiki`) how to build an artisanal beer brewery, then wanted a markdown guide and commands that place the machines at exact coordinates.

## Player guide

`docs/guides/brasserie-artisanale.md` is the compiled walkthrough of [[grain-processing]] on [[artisanal-processing]] machines. Official `alcoholic:beer` ends after generic `FERMENT`. No press, oak barrel, crock, or industrial controller is required. The mill still needs an adjacent [[mechanical-drive-port]] supply. Mash wants magma underneath; boil wants a lit campfire or lava.

Creative kits already existed: `/alcoholic debug kit beer_agriculture` and `beer_artisanal`.

## Placement commands

`BeerLinePlacer` plus `/alcoholic debug place` (permission 2) spawn layouts from machine aliases, not drink-family branches:

- `/alcoholic debug place beer artisanal <pos>`
- `/alcoholic debug place beer industrial <pos>`
- `/alcoholic debug place <alias> <pos> [w h d]`

Artisanal aliases pad the mill with an engine and the thermal machines with the matching heat block. Industrial aliases go through `HollowCuboidPlacer` and `INDUSTRIAL_STRIDE`. The session also deployed a build into a local CurseForge instance for in-world checks. ^[inferred]

## Related

- [[grain-processing]]
- [[artisanal-processing]]
- [[industrial-multiblock]]
- [[industrial-processing]]
- [[fermentation-physics]]
- [[bottled-beverage-snapshot]]
- [[alcoholic]]
