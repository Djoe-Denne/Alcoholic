# ADR-034: Optional FTB Quests Chapter

- Status: Accepted
- Date: 2026-08-28
- Extends: [ADR-033](ADR-033-advancements-as-progression-source.md),
  [ADR-001](ADR-001-loader-independent-architecture.md),
  [ADR-003](ADR-003-create-integration-boundary.md)

## Context

[ADR-033](ADR-033-advancements-as-progression-source.md) made vanilla
advancements the only progression source in the Alcoholic JAR. A modpack
may still want an FTB Quests chapter that shows the same path and how
the artisanal machines move, without pulling Architectury, FTB Library,
FTB Teams, or Item Filters into the core compile or runtime classpath.

FTB Quests 1902.5.10 loads quests from `config/ftbquests/quests`, not
from a mod JAR. Chapter Image objects animate only when the texture is
an atlas sprite (FTB Library 1902.3.14+). A GUI `SimpleTexture` ignores
`.mcmeta`.

## Decision

Ship an optional **template** under `modpack/ftbquests/`. Pack authors
copy it into `config/ftbquests/quests`. Alcoholic does not depend on
FTB, Create, Ponder, or GeckoLib.

- Each quest is one `AdvancementTask` (`type: "advancement"`, empty
  `criterion`) watching an ADR-033 ID. Detection stays in the
  advancement triggers.
- Quest titles and descriptions reuse `advancements.alcoholic.*`
  translation keys. There are no FTB item rewards.
- The chapter canvas is a production line. Six flipbook sprites
  (`press`, `mash_tun`, `fermenter`, `barrel`, `crock`, `bottle`) sit
  next to the process quests. Harvest quests use item icons only.
- Flipbooks live on the item atlas as
  `assets/alcoholic/textures/item/ftbquests/<id>.png` plus `.mcmeta`.
  Chapter images reference `alcoholic:item/ftbquests/<id>` (no `.png`)
  so FTB uses `AtlasSpriteIcon`. They are not inventory items.
- Images have no FTB `dependency`, so the diagrams stay visible before
  the quest is completed.
- Quest and task IDs are stable 16-digit hex values documented in
  `modpack/ftbquests/README.md`.

Target versions: FTB Quests **1902.5.10**, FTB Library **1902.3.14+**,
quest file `version: 13`.

## Consequences

- A player without FTB still has the Alcoholic advancement tab.
- Adding or renaming an advancement ID requires a matching SNBT edit
  in the template; the core JAR does not load that SNBT.
- Industrial machines, malt floor, mill, and kettle stay out of this
  chapter. They can be added later as extra images without new
  advancement IDs.
