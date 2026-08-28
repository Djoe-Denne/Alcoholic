---
title: Cursor Voxel Campaign Session
category: references
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [oak barrel session, remaining machine models]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/76150f12-2be0-4b0e-b613-5c8b2a34469d/76150f12-2be0-4b0e-b613-5c8b2a34469d.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/37519451-d242-4e5f-895c-c406da512e6b/37519451-d242-4e5f-895c-c406da512e6b.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/9d867687-dd51-479d-8fb3-dcc65bf01789/9d867687-dd51-479d-8fb3-dcc65bf01789.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/dcc16384-7d96-4214-bfd1-a48100462d4a/dcc16384-7d96-4214-bfd1-a48100462d4a.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/f1fcf432-2298-4c57-b7c8-573f8bf487ed/f1fcf432-2298-4c57-b7c8-573f8bf487ed.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/27/rollout-2026-08-27T23-08-36-01a0450d-a2aa-7822-aa7e-9924e1f0c3e5.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/27/rollout-2026-08-27T20-23-24-01a04476-63b6-7753-9dbc-0aef51083da8.jsonl"
summary: >-
  Cube inventory, ChatGPT prompts, then Java voxels for barrel, crock, press, kettle, fermenter, motor, hatch, casing, and window. One Blockbench MCP at a time.
provenance:
  extracted: 0.80
  inferred: 0.18
  ambiguous: 0.02
created: 2026-08-28T19:15:00+02:00
updated: 2026-08-28T19:15:00+02:00
---

# Cursor Voxel Campaign Session

After the first three grain machines, nineteen blocks were still `cube_all` (plus a two-box fermenter and a four-box press). Prompts landed in `art/prompts/chatgpt-machines-restantes.md` (kettle and fermenter excluded once authored). A later Codex pass started 512 BDcraft masters for items and vineyard plants.

## What shipped in this cluster

- [[mash-tun-visual]] — mid-res skipped; locked oak; `open` lid
- [[brewing-kettle-visual]] — gauges after the first copper pass
- Oak barrel — lying cask, locked oak, iron hoops
- Blending crock — stoneware jar; lid opens in Java when the player clicks to use the inventory
- Artisanal press — screw press 1×1
- Electric motor — form accepted, then spin while on; later shaft height matched the combustion engine (see [[electric-motor]])
- Access hatch, industrial casing, machine window — cuboids must touch so the block does not look hollow
- Artisanal fermenter — 50-cube barrel; later `FACING`, `OPEN`, hitbox, airlock particles (red-team of uncommitted Java)

Art commits staged **only treated** `art/` trees so unfinished boards stayed uncounted.

## Formed overviews (art only)

Eight formed boards (kettle, vat, conditioning, tank, malt house, mash tun, roller mill, press) were locked with SHA `2c51ee93…`. They are overlays at art size, not gameplay block ids. See [[formed-multiblock-visual]].

## Parallel Blockbench

`user-blockbench` owns one open project. Two agents calling `new_project` / `save_project` clobber each other. Parallel work is allowed for README/refs; MCP modeling is serial. A pool of eight agents was used to align formed **references**, one agent per machine.

Each remaining `art/blockbench/<id>/textures/README.md` now carries a Blockbench brief, the real Java classes, and what to update if an animation appears. Gaps called out: crock `reference_open` without `OPEN`; ports still `cube_all` in-game while art shows useful faces; formed interiors without a BER except the press platen.

## Distilled pages

- [[artisanal-machine-voxel-models]]
- [[mash-tun-visual]]
- [[brewing-kettle-visual]]
- [[blockbench-java-block-workflow]]

## Related

- [[codex-ajouter-modeles-3d-minecraft]]
- [[resource-pack-resolution-chain]]
- [[alcoholic]]
