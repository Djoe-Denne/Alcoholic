---
title: Grain Processing
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [MALT, MILL, MASH, BOIL, beer DAG, malting floor, mash tun, brewing kettle]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/95c81b7c-fa88-4055-9741-14cb948964c9/95c81b7c-fa88-4055-9741-14cb948964c9.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/1ca21b6f-a077-4686-8f47-c41ee6e59fc7/1ca21b6f-a077-4686-8f47-c41ee6e59fc7.jsonl"
summary: Phase 7A ships a beer DAG through generic MALT, MILL, MASH, and BOIL. No drink-family Java branches.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-08-25T18:55:00+02:00
updated: 2026-08-25T20:05:00+02:00
---

# Grain Processing

Phase 7A adds the second shipped beverage family by stressing the [[beverage-framework]] with a different DAG. Beer is data plus generic process types. There is no `BeerFermentationProcess` and no `if (isBeer)` in the engine.

## Authoritative DAG

```text
BARLEY → MALT → MALTED BARLEY → MILL → GRIST
GRIST + WATER → MASH → WORT
WORT + HOPS → BOIL → HOPPED WORT
HOPPED WORT + YEAST → FERMENT → BEER
```

The shipped graph is `alcoholic:beer`. It ends after generic `FERMENT`. AGE is not injected. Whisky remains a structural fixture (`DISTILL` is still a stub). Wheat-beer and non-beer mash graphs stay validation fixtures.

## Process types

`alcoholic:malt` is solid-to-solid (ADR-024). A definition carries duration, moisture, temperature, and a kiln profile (`colorPotential`, `fermentablePotential`, `roastIntensity`). Pale / amber / dark are data, not Java subclasses. When several `MALT` definitions share an input, executors bind an explicit id (default `alcoholic:malt_pale`). Shift-using an empty malting floor cycles definitions.

`alcoholic:mill` is a generic solid transform (malted grain → grist, property copy). Official execution is the [[native-executor-invariant|Malt Mill]], powered by any [[mechanical-drive-port]] supply. Create millstone and crushing wheels remain optional extra executors.

`alcoholic:mash` is a mixed thermal process (ADR-026): grist plus `minecraft:water` → wort plus spent grain. `TemperatureProfile.extractionYield` maps preferred / cold / hot / out-of-band heat to extraction quality. Sugar, color, and temperature are typed liquid properties. The mash tun is a two-tank executor; heat comes from the block below through `HeatSources`.

`alcoholic:boil` heats a liquid and consumes hop additions (ADR-027). Extracted `alcoholic:bitterness` and `alcoholic:aroma` are typed properties. Phase 7A uses one addition at progress `0.0`; `BoilConfig.additions` is the later timeline hook. The brewing kettle executes `BOIL`. Existing fermenters then run generic `FERMENT`.

Mixed solid/liquid ports reuse `ProcessInputs` (ADR-025). There is no brewing-water fluid.

## Agriculture

Barley is an annual cereal (`CerealCropBlock`). Hops grow as a vertical bine on generic `CropSupportPost` plus trellis wire, not as a grapevine subclass. Ingredient identity is tags: `#alcoholic:barley`, `#alcoholic:malted_barley`, `#alcoholic:malted_grain`, `#alcoholic:grist`, `#alcoholic:hops`, `#alcoholic:spent_grain`. Brewery is a preferred crop provider when present; Alcoholic ids stay registered. See [[semantic-crop-compatibility]].

## Native machines

The malting floor executes `MALT` only. An earlier Phase 7A cut also ran `MILL` on that floor because Alcoholic had no mill (ADR-028). [[native-executor-invariant|ADR-030]] added the Malt Mill and returned the floor to malt-only.

The mash tun and brewing kettle are artisanal mixed-input executors. There is no industrial malt house, mash tun, or kettle yet.

## Related

- [[alcoholic]]
- [[process-capability-graph]]
- [[beverage-framework]]
- [[artisanal-processing]]
- [[mechanical-drive-port]]
- [[electric-motor]]
- [[crossroads-rotary-adapter]]
- [[native-executor-invariant]]
- [[create-press-adapter]]
- [[semantic-crop-compatibility]]
- [[fermentation-physics]]
- [[cursor-phase-7a-grain-session]]
- [[cursor-create-independence-session]]
- [[forge-1.19.2-phase-7a-verification]]
