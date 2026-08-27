---
title: Malt Mill Visual
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [malt_mill model]
sources:
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T21-27-11-01a03f89-8bc5-7140-aa48-215bf4ba51ed_01a03f8a-6d29-7862-943b-e0e09c706834.jsonl"
summary: Fork 4 mill: hopper, rollers, chute, no hand crank, two-block height, bottom bin inside 16×16, 512 paint then 32 default.
provenance:
  extracted: 0.9
  inferred: 0.08
  ambiguous: 0.02
created: 2026-08-27T13:30:00+02:00
updated: 2026-08-27T13:30:00+02:00
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

512×512 BDcraft-like atlas, user-validated in Blockbench, then other sizes exported. Last instruction in the fork: **32×32 as the mod default** (enough pixels for this mill atlas). That differs from the 64×64 default chosen for the engine and floor. ^[ambiguous]

Review stills were written under `art/blockbench/malt_mill/review/`.

## Related

- [[artisanal-machine-voxel-models]]
- [[resource-pack-resolution-chain]]
- [[grain-processing]]
- [[mechanical-drive-port]]
- [[codex-ajouter-modeles-3d-minecraft]]
- [[blockbench-java-block-workflow]]
