---
title: Fermentation Physics
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [FermentProcessor, fermentation kinetics]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/a6b5797c-82f8-4021-9d63-10a82fed6899/a6b5797c-82f8-4021-9d63-10a82fed6899.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/c2ca3b27-ad63-4be9-af24-47c49c111f2f/c2ca3b27-ad63-4be9-af24-47c49c111f2f.jsonl"
summary: FERMENT is continuous kinetics on a LiquidBatch. Sugar falls, ethanol rises, CO2 vents. Temperature bands slow or stall the rate.
provenance:
  extracted: 0.88
  inferred: 0.1
  ambiguous: 0.02
created: 2026-08-25T14:10:00+02:00
updated: 2026-08-25T14:40:00+02:00
---

# Fermentation Physics

FERMENT is a registered process capability, not a wine script. A datapack may omit it entirely (fruit liqueur via INFUSE) or start with it (a rum-style wash). See [[process-capability-graph]].

## Continuous conversion

A ferment definition names an input liquid, an output liquid, a yeast selector, kinetics, and temperature bands. Wine content uses preferred 18–24 °C and operating 10–30 °C, with `ticks_to_complete` 80. The cider test pack uses preferred 16–22 °C, operating 8–28 °C, and 100 ticks.

Each tick, fermentable sugar falls and ethanol rises according to `sugar_to_ethanol`. CO2 is recorded from `co2_per_sugar` and vented by the artisanal vessel rather than stored as a gameplay gas. When sugar reaches `completion_threshold`, the batch identity becomes the configured output liquid.

After that conversion, later ticks must not reject the batch because the input liquid no longer matches. The processor short-circuits to success once the batch is already the output and sugar is at or below the threshold.

## Temperature

Outside the preferred band the rate slows. Outside the operating band it stalls. Transformation rules live in domain and application services, not in block-entity `if wine` branches. [[aging-process]] reuses the same preferred / operating / stall bands without sharing FERMENT kinetics.

## Yeast

Gameplay FERMENT requires an item matching `#alcoholic:yeast` when `require_yeast` is true. The shipped tag currently contains `alcoholic:yeast`. The same tag can later accept other mods' yeasts without a new process type.

## Related

- [[liquid-batch]]
- [[artisanal-processing]]
- [[aging-process]]
- [[beverage-framework]]
- [[public-extension-api]]
- [[harvest-lot-metadata]]
- [[cursor-phase-4-processing-session]]
- [[cursor-phase-5-aging-session]]
- [[forge-1.19.2-phase-4-verification]]
- [[forge-1.19.2-phase-5-verification]]
