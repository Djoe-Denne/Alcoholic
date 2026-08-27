---
title: Codex Ajouter Modèles 3D Minecraft
category: references
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [Ajouter modèles 3D Minecraft, Codex 3D model forks]
sources:
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-11-32-01a03f45-29de-7df3-a840-d62271c376d5.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-15-09-01a03f48-7af6-7513-a3c9-b297e81b7a96.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-28-31-01a03f54-b465-7cc3-9e92-2bcfc0c8e291.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-48-08-01a03f66-acf1-7931-95db-a9b0b9d8961f.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-56-07-01a03f6d-f9dd-71c2-8d26-3fc369bd905a.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T21-26-14-01a03f89-8bc5-7140-aa48-215bf4ba51ed.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T21-27-11-01a03f89-8bc5-7140-aa48-215bf4ba51ed_01a03f8a-6d29-7862-943b-e0e09c706834.jsonl"
summary: Codex thread family that replaced three artisanal cubes with Java voxel models, then split the work into three machine forks.
provenance:
  extracted: 0.9
  inferred: 0.08
  ambiguous: 0.02
created: 2026-08-27T13:30:00+02:00
updated: 2026-08-27T13:30:00+02:00
---

# Codex Ajouter Modèles 3D Minecraft

Codex Desktop conversations titled **Ajouter modèles 3D Minecraft** designed voxel visuals for three already-registered [[artisanal-processing]] blocks. A sibling thread installed [[blockbench]] and the modeling skills. The user then forked the parent plan so each machine stayed autonomous.

## Thread map

```text
01a03f45  ROOT   first ask (planche → 1.19.2 asset tree)
01a03f48  ROOT   parent plan “Modèles 3D animés des trois machines artisanales”
01a03f54  ROOT   Installer le MCP Blockbench + img2blockbench
01a03f66  FORK 2 from 01a03f48   CIBLE = malting_floor
01a03f6d  FORK 3 from 01a03f48   CIBLE = primitive_combustion_engine
01a03f89  FORK 4 from 01a03f66   CIBLE = malt_mill
```

Fork 4 is a fork of the malting-floor thread, not of the parent plan. It still stayed mill-only.

## Decisions locked across the family

- Replace cube-per-face placeholders with real Java Block/Item voxel JSON. Do not add GeckoLib or Bedrock as a shipped format.
- Keep one logical block and existing collisions, recipes, and process ids. Visual geometry may overflow the 16×16 footprint except where a later fork pulled a part back in.
- Design and review in Blockbench through `$blockbench-mcp-modeling`. Use `$img2blockbench` only if it helps the first silhouette and stays Java-compatible.
- One fork, one machine. A fork must not regenerate the other two machines.
- Later texture policy: paint a 512×512 BDcraft-like master, then downsample. See [[resource-pack-resolution-chain]].

## Distilled pages

- [[artisanal-machine-voxel-models]]
- [[resource-pack-resolution-chain]]
- [[malting-floor-visual]]
- [[primitive-combustion-engine-visual]]
- [[malt-mill-visual]]
- [[blockbench-java-block-workflow]]
- [[blockbench]]

## Related

- [[artisanal-processing]]
- [[grain-processing]]
- [[mechanical-drive-port]]
- [[alcoholic]]
