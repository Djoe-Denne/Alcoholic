---
title: Grain Processing
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [MALT, MILL, MASH, BOIL, CONDITION, beer DAG, malting floor, mash tun, brewing kettle]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/95c81b7c-fa88-4055-9741-14cb948964c9/95c81b7c-fa88-4055-9741-14cb948964c9.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/1ca21b6f-a077-4686-8f47-c41ee6e59fc7/1ca21b6f-a077-4686-8f47-c41ee6e59fc7.jsonl"
  - "C:/Users/djden/.codex/sessions/2026/08/26/rollout-2026-08-26T20-48-08-01a03f66-acf1-7931-95db-a9b0b9d8961f.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/38cdf6ba-d500-47a2-87ec-22f346732c8c/38cdf6ba-d500-47a2-87ec-22f346732c8c.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/d120a5cc-91ff-47b3-99d0-392583b27834/d120a5cc-91ff-47b3-99d0-392583b27834.jsonl"
summary: Phase 7A ships a beer DAG through generic MALT, MILL, MASH, and BOIL. No drink-family Java branches.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-08-25T18:55:00+02:00
updated: 2026-08-27T16:20:00+02:00
---

# Grain Processing

Phase 7A adds the second shipped beverage family by stressing the [[beverage-framework]] with a different DAG. Beer is data plus generic process types. There is no `BeerFermentationProcess` and no `if (isBeer)` in the engine.

## Authoritative DAG

```text
BARLEY → MALT → MALTED BARLEY → MILL → GRIST
GRIST + WATER → MASH → WORT
WORT + HOPS → BOIL → HOPPED WORT
HOPPED WORT + YEAST → FERMENT → BEER
YOUNG BEER → optional CONDITION → BEER
```

The shipped graph is `alcoholic:beer`. It ends after generic `FERMENT`. AGE is not injected. `CONDITION` exists as a generic optional process and is not a node on `alcoholic:beer`. Whisky remains a structural fixture (`DISTILL` is still a stub). Wheat-beer and non-beer mash graphs stay validation fixtures.

## Process types

`alcoholic:malt` is solid-to-solid (ADR-024). A definition carries duration, moisture, temperature, and a kiln profile (`colorPotential`, `fermentablePotential`, `roastIntensity`). Pale / amber / dark are data, not Java subclasses. When several `MALT` definitions share an input, executors bind an explicit id (default `alcoholic:malt_pale`). Shift-using an empty malting floor cycles definitions.

`alcoholic:mill` is a generic solid transform (malted grain → grist, property copy). Official execution is the [[native-executor-invariant|Malt Mill]], powered by any [[mechanical-drive-port]] supply. Create millstone and crushing wheels remain optional extra executors.

`alcoholic:mash` is a mixed thermal process (ADR-026): grist plus `minecraft:water` → wort plus spent grain. `TemperatureProfile.extractionYield` maps preferred / cold / hot / out-of-band heat to extraction quality. Sugar, color, and temperature are typed liquid properties. The mash tun is a two-tank executor; heat comes from the block below through `HeatSources`.

`alcoholic:boil` heats a liquid and consumes hop additions (ADR-027). Extracted `alcoholic:bitterness` and `alcoholic:aroma` are typed properties. Additions may carry `at_progress` and a lightweight `role` (`bittering`, `aroma`, `dual`). The brewing kettle and industrial kettle execute `BOIL`. Existing fermenters then run generic `FERMENT`.

`alcoholic:condition` is optional post-fermentation maturation that is not wood `AGE`. It may raise `alcoholic:maturity` and, with yeast plus residual sugar, `alcoholic:carbonation`.

Mixed solid/liquid ports reuse `ProcessInputs` (ADR-025). There is no brewing-water fluid.

## Agriculture

Barley is an annual cereal (`CerealCropBlock`). Hops grow as a vertical bine on generic `CropSupportPost` plus trellis wire, not as a grapevine subclass. Ingredient identity is tags: `#alcoholic:barley`, `#alcoholic:malted_barley`, `#alcoholic:malted_grain`, `#alcoholic:grist`, `#alcoholic:hops`, `#alcoholic:spent_grain`. Brewery is a preferred crop provider when present; Alcoholic ids stay registered. See [[semantic-crop-compatibility]].

## Native machines

The malting floor executes `MALT` only. An earlier Phase 7A cut also ran `MILL` on that floor because Alcoholic had no mill (ADR-028). [[native-executor-invariant|ADR-030]] added the Malt Mill and returned the floor to malt-only.

The mash tun and brewing kettle are artisanal mixed-input executors. Phase 7B adds industrial executors for the same process types: malt house, roller mill, mash tun, brewing kettle, plus an optional conditioning vessel. See [[industrial-processing]].

Floor and mill cube placeholders are being replaced by [[malting-floor-visual]] and [[malt-mill-visual]]. Process types stay generic.

Player walkthrough: `docs/guides/brasserie-artisanale.md` from [[cursor-artisanal-brewery-guide-session]]. Recipe viewers show the same generic types via [[process-display-and-recipe-viewers]].

## Related

- [[alcoholic]]
- [[process-capability-graph]]
- [[beverage-framework]]
- [[artisanal-processing]]
- [[mechanical-drive-port]]
- [[electric-motor]]
- [[crossroads-rotary-adapter]]
- [[native-executor-invariant]]
- [[artisanal-machine-voxel-models]]
- [[create-press-adapter]]
- [[semantic-crop-compatibility]]
- [[fermentation-physics]]
- [[process-display-and-recipe-viewers]]
- [[cursor-artisanal-brewery-guide-session]]
- [[cursor-phase-7a-grain-session]]
- [[cursor-create-independence-session]]
- [[industrial-processing]]
- [[forge-1.19.2-phase-7a-verification]]
