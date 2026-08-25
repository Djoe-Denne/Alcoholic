---
title: Beverage Framework
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [BeverageDefinition]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b282f8bd-e540-4028-93d1-896905419dcd/b282f8bd-e540-4028-93d1-896905419dcd.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b282f8bd-e540-4028-93d1-896905419dcd/subagents/5362e3cf-53fc-4eff-a34f-7cf2342088a9.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b282f8bd-e540-4028-93d1-896905419dcd/subagents/76fb1a5f-75ec-4a6a-9436-6a55a7cd0256.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/a6b5797c-82f8-4021-9d63-10a82fed6899/a6b5797c-82f8-4021-9d63-10a82fed6899.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/c2ca3b27-ad63-4be9-af24-47c49c111f2f/c2ca3b27-ad63-4be9-af24-47c49c111f2f.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/95c81b7c-fa88-4055-9741-14cb948964c9/95c81b7c-fa88-4055-9741-14cb948964c9.jsonl"
summary: Beverages are data graphs over registered process types and liquids. Wine and grain beer execute without a drink-family Java list.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-08-25T12:50:00+02:00
updated: 2026-08-25T18:55:00+02:00
---

# Beverage Framework

The core must not contain a finite list of drink families. Phase 3 built the catalogs. Phase 4 executes PRESS and FERMENT. Phase 5 executes AGE, BLEND, and BOTTLE. Phase 7A executes MALT, MILL, MASH, and BOIL on the same catalogs without a beer service.

## Definitions versus liquids

A `BeverageDefinition` is an identity plus optional category, a [[process-capability-graph]], and the properties that identity may carry. A [[liquid-batch]] is volume, a `LiquidDefinition`, and a typed property bag. Those are different objects. Category is free metadata, not a `switch`.

## Data load

Datapacks contribute files under `data/<namespace>/alcoholic/ingredients/`, `.../processes/`, `.../beverages/`, and `.../liquids/`. Gson is converted to public `DataNode` values, then decoded with pure Java codecs. The catalog is validated as one snapshot and published atomically. An invalid reload keeps the previous snapshot.

Java-registered process types include press, mill, malt, mash, boil, ferment, distill, age, blend, infuse, and bottle. Built-in properties include sugar, ethanol, acidity, tannin, bitterness, aroma, carbonation, and maturity. They are content registrations, not engine branches. PRESS, FERMENT, AGE, BLEND, BOTTLE, MALT, MILL, MASH, and BOIL have gameplay executors. DISTILL remains a registered type without a gameplay machine. Each official type follows the [[native-executor-invariant]].

Process-definition input validation must use the real catalog, not an empty one. Using `BeverageCatalog.empty()` made every configured ingredient look unknown. ^[inferred]

## Shipped content versus fixtures

Wine liquids and PRESS/FERMENT/AGE/BLEND processes are generated content, including finished `red_wine` and `white_wine`. Grain beer (`alcoholic:beer`) is shipped through [[grain-processing]] and ends after FERMENT. Cider stays a `testpack:` data-only acceptance pack: apple → PRESS → FERMENT → young cider, with no cider Java types and no Forge cider fluids. Whisky, rum, fruit liqueur, wheat beer, and extra mash graphs remain validation fixtures; those packs may include AGE nodes such as `testpack:age_new_make`. Distillation gameplay is still out of scope. See [[aging-process]].

## Related

- [[process-capability-graph]]
- [[liquid-batch]]
- [[fermentation-physics]]
- [[aging-process]]
- [[blend-versus-tank-merge]]
- [[public-extension-api]]
- [[alcoholic]]
- [[harvest-lot-metadata]]
- [[loader-independent-minecraft-architecture]]
- [[cursor-phase-3-beverage-framework-session]]
- [[cursor-phase-4-processing-session]]
- [[grain-processing]]
- [[native-executor-invariant]]
- [[cursor-phase-5-aging-session]]
- [[cursor-phase-7a-grain-session]]
- [[forge-1.19.2-phase-2-3-verification]]
- [[forge-1.19.2-phase-4-verification]]
- [[forge-1.19.2-phase-5-verification]]
- [[forge-1.19.2-phase-7a-verification]]
