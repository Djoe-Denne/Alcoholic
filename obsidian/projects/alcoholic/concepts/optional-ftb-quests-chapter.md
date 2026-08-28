---
title: >-
  Optional FTB Quests Chapter
category: concepts
tags: [minecraft, compatibility, type/concept, project/alcoholic]
aliases: [ADR-034, FTB Quests template, AdvancementTask]
sources:
  - "C:/Users/djden/source/repos/Alcoholic/docs/adr/ADR-034-ftb-quests-optional-chapter.md"
  - "C:/Users/djden/source/repos/Alcoholic/modpack/ftbquests/README.md"
summary: >-
  Optional SNBT template under modpack/ftbquests. Each quest is one AdvancementTask. The core JAR never loads it.
provenance:
  extracted: 0.88
  inferred: 0.1
  ambiguous: 0.02
created: 2026-08-28T22:30:00+02:00
updated: 2026-08-28T22:30:00+02:00
---

# Optional FTB Quests Chapter

Ship an optional template under `modpack/ftbquests/`. Pack authors copy it into `config/ftbquests/quests`. Alcoholic does not depend on FTB, Create, Ponder, or GeckoLib. The core JAR does not load that SNBT.

FTB Quests 1902.5.10 loads quests from `config/ftbquests/quests`, not from a mod JAR. Quest file `version: 13`. Target library: FTB Library 1902.3.14+.

## Presentation only

Each quest is one `AdvancementTask` (`type: "advancement"`, empty `criterion`) watching a vanilla advancement ID from [[advancements-as-progression-source]]. Detection stays in the advancement triggers.

Quest titles and descriptions reuse `advancements.alcoholic.*` translation keys. There are no FTB item rewards. Toasts and XP come from the advancements.

SNBT is generated from [[wine-beer-progression-graph|ProgressionCatalog]]. Do not hand-edit chapter files.

## Canvas and flipbooks

The artisanal chapter canvas is a production line. Wine is left (`x < 0`), beer is right (`x > 0`), shared nodes sit on `x = 0`. Chapter Image objects animate only when the texture is an atlas sprite. A GUI `SimpleTexture` ignores `.mcmeta`.

Flipbooks live on the item atlas as `assets/alcoholic/textures/item/ftbquests/<id>.png` plus `.mcmeta`. Chapter images reference `alcoholic:item/ftbquests/<id>` (no `.png`) so FTB uses `AtlasSpriteIcon`. They are not inventory items. Images have no FTB `dependency`, so diagrams stay visible before the quest is completed.

Harvest quests and new beer process quests use item icons until flipbooks exist.

## Install

New book: copy `quests/` to `config/ftbquests/quests/`, keep Alcoholic on the client, then `/ftbquests reload`. Existing book: copy only `alcoholic.snbt` and `alcoholic_industrial.snbt`; leave the pack's `data.snbt` alone.

Stable hex IDs are documented in `modpack/ftbquests/README.md`. Do not regenerate them in the FTB editor. Industrial formation is a second template chapter: [[industrial-progression-and-jei-formation]].

## Related

- [[advancements-as-progression-source]]
- [[wine-beer-progression-graph]]
- [[industrial-progression-and-jei-formation]]
- [[create-press-adapter]]
- [[cursor-progression-and-fluids-session]]
- [[alcoholic]]
