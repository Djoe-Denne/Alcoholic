---
title: Mash Tun Visual
category: concepts
tags: [minecraft, type/concept, project/alcoholic]
aliases: [mash_tun model, mid-res skip]
sources:
  - "C:/Users/djden/.codex/sessions/2026/08/27/rollout-2026-08-27T23-08-37-01a0450d-a5de-7331-8f2b-285decbc74a0.jsonl"
summary: >-
  Oak mash tun copied from the locked engine tile. The mid-res grey pass is skipped; paint the 512 master, then downsample. Open lid is a second model.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-08-28T19:15:00+02:00
updated: 2026-08-28T19:15:00+02:00
---

# Mash Tun Visual

`mash_tun` is a one-block [[artisanal-processing]] executor. The voxel campaign authored it with the project skill `alcoholic-java-machine-model` and `$blockbench-mcp-modeling`. Do not edit the engine, floor, mill, kettle, or fermenter in the same chat.

## Mid-res skip

The user accepted the grey silhouette and said the mid-res pass would not be used. Go from approved form to 512 paint, then downsample from that master. Default in the mod is 64×64.

## Locked oak

Wood colour and plank pattern must match the existing engine tile in `resourcepacks/Alcoholic-512x`. After approval, that pattern was written into the project skill for later machines.

## Open lid

The tun has two plate states. Looking inside when open requires `mash_tun_open` plus the `open` blockstate. Export both models and the 64 texture into the jar.

## Related

- [[artisanal-processing]]
- [[artisanal-machine-voxel-models]]
- [[resource-pack-resolution-chain]]
- [[blockbench-java-block-workflow]]
- [[grain-processing]]
- [[cursor-voxel-campaign-session]]
