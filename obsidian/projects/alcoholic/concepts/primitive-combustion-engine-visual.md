---
title: Primitive Combustion Engine Visual
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [primitive_combustion_engine model]
sources:
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-56-07-01a03f6d-f9dd-71c2-8d26-3fc369bd905a.jsonl"
summary: Fork 3 voxel engine with off/lit atlases, archived 512 master, and resource packs down to 16 after a BDcraft paint pass.
provenance:
  extracted: 0.88
  inferred: 0.1
  ambiguous: 0.02
created: 2026-08-27T13:30:00+02:00
updated: 2026-08-27T13:30:00+02:00
---

# Primitive Combustion Engine Visual

Codex fork **Ajouter modèles 3D Minecraft (3)** (`01a03f6d`, from parent `01a03f48`) owns only `primitive_combustion_engine`. It must not touch the floor or mill.

## Role

This block is a [[mechanical-drive-port]] supply. The parent visual plan: moving parts (shaft / flywheel) spin while the engine is on. Off and lit texture states stay paired.

## Texture history in the fork

1. First voxel model existed with a large-pixel look the user rejected.
2. Repaint in Blockbench at 512×512, BDcraft / comic painted, **stop before export** for approval.
3. User kept that paint. Produce 256, 128, 64, 32, and 16 from the master. 16 first became the mod default; optional packs held the rest. No redundant 16× pack.
4. Atlas math then moved the recommended default to **64×64** so each of four material columns gets 16×16 pixels. The user accepted that change.

Masters live in the `.bbmodel` and in `art/blockbench/primitive_combustion_engine/textures/master-512/` with `SHA256SUMS.txt`. Gradle checks in that session passed after the 16× default swap.

## Related

- [[artisanal-machine-voxel-models]]
- [[resource-pack-resolution-chain]]
- [[mechanical-drive-port]]
- [[codex-ajouter-modeles-3d-minecraft]]
- [[blockbench-java-block-workflow]]
