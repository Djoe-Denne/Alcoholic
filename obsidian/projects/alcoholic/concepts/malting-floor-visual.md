---
title: Malting Floor Visual
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [malting_floor model]
sources:
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-48-08-01a03f66-acf1-7931-95db-a9b0b9d8961f.jsonl"
summary: Fork 2 voxel model for the malting floor: oak frame, hardware, mat, and barley, with a validated 512 master then 64 default.
provenance:
  extracted: 0.88
  inferred: 0.1
  ambiguous: 0.02
created: 2026-08-27T13:30:00+02:00
updated: 2026-08-27T13:30:00+02:00
---

# Malting Floor Visual

Codex fork **Ajouter modèles 3D Minecraft (2)** (`01a03f66`, from parent `01a03f48`) owns only `malting_floor`. It must not touch the engine or mill.

## Silhouette

Low oak frame and feet, readable iron hardware, an interior mat, and barley set *into* the bed rather than painted on top. Eighteen cuboids in the 512 master `.bbmodel`. The parent plan described this as a tray-like floor, not a full cube.

Gameplay stays [[artisanal-processing]]: `alcoholic:malt` only. This page is visual.

## Texture

The fork reused the engine's [[resource-pack-resolution-chain]] after that language existed: inspect `resourcepacks/`, paint a new 512×512 master, wait for approval, then `512 → 256 → 128 → 64`. No 32 or 16 generated in that pass. The 64×64 atlas became the texture shipped in the mod.

Source files called out in the session:

- `art/blockbench/malting_floor/malting_floor_512_master.bbmodel`
- `resourcepacks/Alcoholic-512x/assets/alcoholic/textures/block/malting_floor.png`

## Related

- [[artisanal-machine-voxel-models]]
- [[resource-pack-resolution-chain]]
- [[grain-processing]]
- [[codex-ajouter-modeles-3d-minecraft]]
- [[blockbench-java-block-workflow]]
