# Alcoholic FTB Quests chapter

Optional presentation of the [ADR-033](../../docs/adr/ADR-033-advancements-as-progression-source.md)
advancement path. Alcoholic does not load these files. There is no FTB
compile or runtime dependency.

Target: **FTB Quests 1902.5.10**, **FTB Library 1902.3.14+**, quest file
`version: 13`.

## Install

New quest book (empty `config/ftbquests/quests`):

1. Copy this `quests/` folder to `config/ftbquests/quests/`.
2. Keep Alcoholic on the client so the flipbook sprites resolve.
3. Run `/ftbquests reload` (or restart the world).

Existing quest book: copy only
`quests/chapters/alcoholic.snbt` into
`config/ftbquests/quests/chapters/`. Leave the pack's `data.snbt`
alone.

Without FTB the player still has the Alcoholic advancement tab.

## What this chapter does

Each quest is one `AdvancementTask` on a stable Alcoholic advancement
ID. Empty `criterion` means the whole advancement. FTB does not
re-check harvest or process logic.

There are no FTB rewards. Toasts and XP come from the advancements.

Animated chapter images use item-atlas sprites
`alcoholic:item/ftbquests/<id>` plus `.mcmeta`. They stay visible
before the quest is completed.

## Stable hex IDs

Do not regenerate these in the FTB editor if you can avoid it. New
IDs break progress for worlds that already used this chapter.

| Object | Hex id |
|---|---|
| Chapter | `A1C0A01C00000001` |
| Quest `root` | `A1C0A01C00000010` |
| Quest `harvest_grapes` | `A1C0A01C00000011` |
| Quest `harvest_hops` | `A1C0A01C00000012` |
| Quest `produce_must` | `A1C0A01C00000013` |
| Quest `ferment_beverage` | `A1C0A01C00000014` |
| Quest `age_wine` | `A1C0A01C00000015` |
| Quest `blend` | `A1C0A01C00000016` |
| Quest `bottle` | `A1C0A01C00000017` |
| Task `root` | `A1C0A01C10000010` |
| Task `harvest_grapes` | `A1C0A01C10000011` |
| Task `harvest_hops` | `A1C0A01C10000012` |
| Task `produce_must` | `A1C0A01C10000013` |
| Task `ferment_beverage` | `A1C0A01C10000014` |
| Task `age_wine` | `A1C0A01C10000015` |
| Task `blend` | `A1C0A01C10000016` |
| Task `bottle` | `A1C0A01C10000017` |

Advancements watched:

- `alcoholic:root`
- `alcoholic:harvest_grapes`
- `alcoholic:harvest_hops`
- `alcoholic:produce_must`
- `alcoholic:ferment_beverage`
- `alcoholic:age_wine`
- `alcoholic:blend`
- `alcoholic:bottle`

See [ADR-034](../../docs/adr/ADR-034-ftb-quests-optional-chapter.md).
