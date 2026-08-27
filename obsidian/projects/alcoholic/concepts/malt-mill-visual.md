---
title: Malt Mill Visual
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [malt_mill model]
sources:
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T21-27-11-01a03f89-8bc5-7140-aa48-215bf4ba51ed_01a03f8a-6d29-7862-943b-e0e09c706834.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/fa499a53-567a-462e-aa1e-cc2c5df0a700/fa499a53-567a-462e-aa1e-cc2c5df0a700.jsonl"
summary: Fork 4 mill: hopper, rollers, chute, no crank; locked engine oak; rollers spin; a raised interior plate is still open.
provenance:
  extracted: 0.86
  inferred: 0.1
  ambiguous: 0.04
created: 2026-08-27T13:30:00+02:00
updated: 2026-08-27T16:20:00+02:00
---

# Malt Mill Visual

Codex fork **Ajouter modèles 3D Minecraft (4)** (`01a03f89`, forked from the malting-floor thread `01a03f66`) owns only `malt_mill`. Other machines already in the dirty tree stay untouched.

## Silhouette

Wide hopper, two rollers, chute, and a short axle meant to couple to the [[primitive-combustion-engine-visual|engine]] — not a hand crank. Review after the first approved paint:

- Remove the crank entirely.
- Keep height at two blocks.
- Pull the lower receptacle fully inside the 16×16 footprint.

First Blockbench pass had coplanar faces in the frame; those were fixed before UVs. Later counts: on the order of 70 cuboids, static versus moving groups. Functional face `-Z`.

Gameplay stays the [[native-executor-invariant]] mill on [[mechanical-drive-port]]. This page is visual.

## Texture

512×512 BDcraft-like atlas, user-validated in Blockbench. Oak must copy the locked engine tile from [[resource-pack-resolution-chain]], not a darker chocolate wood. The unused middle animation pass was dropped; the approved silhouette stayed.

The Codex fork last asked for **32×32 as the mod default**. The later Java skill locks **64×64** as the shared machine default until the mill finishes the same pack export as the engine and mash tun. Treat the mill as painted-and-reviewed, not a finished example. ^[ambiguous]

Review stills live under `art/blockbench/malt_mill/review/`.

## Motion

`MaltMillRenderer` spins the front/rear rollers and drive axle while the mill is working. The user then asked for two hopper-plate poses: idle plate down, raised plate when looking into the hopper. That second pose is not in the renderer yet. ^[inferred]

## Related

- [[artisanal-machine-voxel-models]]
- [[resource-pack-resolution-chain]]
- [[grain-processing]]
- [[mechanical-drive-port]]
- [[codex-ajouter-modeles-3d-minecraft]]
- [[blockbench-java-block-workflow]]
