---
title: CurseForge Create 2 Mekanism Deploy
category: skills
tags: [minecraft, type/procedure, project/alcoholic]
aliases: [Alcoholic-128x zip, deploy jar]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/9271e372-01d0-4dab-ba2b-c0c28d969534/9271e372-01d0-4dab-ba2b-c0c28d969534.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/5a877b5d-04a2-4caf-bd44-f9114c295ec6/5a877b5d-04a2-4caf-bd44-f9114c295ec6.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/48cf13a3-9ec8-4348-b31e-37330cedae92/48cf13a3-9ec8-4348-b31e-37330cedae92.jsonl"
summary: >-
  Rebuild the remapped Forge jar and the 128 pack into Create 2 Mekanism. Zip paths must use slashes; pack.mcmeta must sit at the zip root.
provenance:
  extracted: 0.90
  inferred: 0.08
  ambiguous: 0.02
created: 2026-08-28T19:15:00+02:00
updated: 2026-08-28T19:15:00+02:00
---

# CurseForge Create 2 Mekanism Deploy

The usual in-world check instance is:

`C:\Users\djden\curseforge\minecraft\Instances\Create 2 Mekanism`

## Jar

Build `:platform-forge-1.19.2:reobfJar` (or `:jar` when that is the remapped artifact in use) and copy:

`alcoholic-forge-1.19.2-0.1.0-SNAPSHOT.jar` → `…\mods\`

F3+T does **not** reload a new jar. Quit the client if it is running; a locked file means the copy failed silently.

## 128 pack

Source folder: `resourcepacks/Alcoholic-128x`. Deploy either that folder or a zip next to it.

Minecraft 1.19.2 rules that failed in this repo:

1. `pack.mcmeta` must be at the **root** of the zip, not under `Alcoholic-128x/`.
2. Zip entry paths must use `/`. Windows `\` is stored, then the game registers the texture id and cannot open the PNG — magenta/black, no fallback to the mod atlas.
3. Enable **Alcoholic-128x** above PureBDcraft and above the mod resources.

If the zip is locked by a running client, close Minecraft, then switch to the folder pack or a rebuilt zip.

Gameplay textures that the model names (for example `wild_hops`) must also exist as 16× inside the **mod jar**. See [[wild-hops]] and [[resource-pack-resolution-chain]].

## Related

- [[resource-pack-resolution-chain]]
- [[alcoholic-debug-commands]]
- [[wild-hops]]
- [[alcoholic]]
