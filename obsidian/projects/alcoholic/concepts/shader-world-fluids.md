---
title: >-
  Shader World Fluids
category: concepts
tags: [minecraft, compatibility, type/concept, project/alcoholic]
aliases: [Iris, Oculus, Complementary Reimagined, block.properties]
sources:
  - "C:/Users/djden/source/repos/Alcoholic/docs/guides/shaders.md"
  - "C:/Users/djden/source/repos/Alcoholic/resourcepacks/README.md"
summary: >-
  Iris does not merge block.properties from the JAR. Complementary r5.x uses block.32000. Alcoholic never patches shaders.
provenance:
  extracted: 0.9
  inferred: 0.08
  ambiguous: 0.02
created: 2026-08-28T22:30:00+02:00
updated: 2026-08-28T22:30:00+02:00
---

# Shader World Fluids

Iris / Oculus draw Alcoholic [[world-fluid-textures]] in the water pass. Complementary, BSL, and most packs apply waves and refraction only when those ids appear in the **shader pack's** `shaders/block.properties`.

Alcoholic **does not** patch that file at runtime. Iris does not merge a `block.properties` from the JAR or a resource pack: putting one in `Alcoholic-64x` would overwrite Complementary's leaf and glass mapping. An Oculus mixin that injects ids after load is fragile across `net.coderbot.iris` vs `net.irisshaders.iris` and across pack water ids. That path is rejected.

Do not ship `shaders/block.properties` in Alcoholic texture packs. Player steps: `docs/guides/shaders.md`.

## Complementary Reimagined r5.x

Water is **not** `block.8`. Edit `block.32000` in the shader zip and add the nine Alcoholic ids (`red_grape_must`, `white_grape_must`, `young_red_wine`, `young_white_wine`, `red_wine`, `white_wine`, `wort`, `hopped_wort`, `beer`). Do not put them on `layer.translucent` (glass, not water). The flowing block uses the same id as the source (`alcoholic:beer`, not `flowing_beer`).

A CurseForge update of Complementary rewrites the file; the line must be re-pasted. Reload shaders (Iris menu, key **O** on the typed instance) or restart the client.

## Other packs

Older Complementary / BSL often use `block.8=minecraft:water`. Add the same nine ids on **that** line, not on `32000`.

Iris 1.7+ accepts a tag: `block.8 = %alcoholic:world_fluids` (or `block.32000` depending on the pack). Oculus 1.6 / Minecraft 1.19.2 does not: list the ids.

## Related

- [[world-fluid-textures]]
- [[resource-pack-resolution-chain]]
- [[loader-independent-minecraft-architecture]]
- [[cursor-progression-and-fluids-session]]
- [[alcoholic]]
