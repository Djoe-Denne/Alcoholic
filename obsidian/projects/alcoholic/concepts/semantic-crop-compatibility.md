---
title: Semantic Crop Compatibility
category: concepts
tags: [minecraft, compatibility, software-architecture, type/concept, project/alcoholic]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b9ff420b-fd71-4089-83bc-cc4766880b14/b9ff420b-fd71-4089-83bc-cc4766880b14.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/bff6f9b3-354c-46b7-8b5e-c5162dc38730/bff6f9b3-354c-46b7-8b5e-c5162dc38730.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b282f8bd-e540-4028-93d1-896905419dcd/b282f8bd-e540-4028-93d1-896905419dcd.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/a6b5797c-82f8-4021-9d63-10a82fed6899/a6b5797c-82f8-4021-9d63-10a82fed6899.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/95c81b7c-fa88-4055-9741-14cb948964c9/95c81b7c-fa88-4055-9741-14cb948964c9.jsonl"
summary: Ingredient semantics use open tag bindings. Grapes, yeast, barley, malt, grist, and hops resolve through tags, not item classes.
provenance:
  extracted: 0.9
  inferred: 0.08
  ambiguous: 0.02
created: 2026-08-25T09:20:00+02:00
updated: 2026-08-25T18:55:00+02:00
---

# Semantic Crop Compatibility

Processing logic classifies ingredients through semantic tags instead of NBT or direct external registry IDs.

## Grape tags

- `#alcoholic:grapes`
- `#alcoholic:grapes/red`
- `#alcoholic:grapes/white`

Alcoholic's red and white grape items populate the color tags. Optional references to `#vinery:red_grape` and `#vinery:white_grape` make Vinery's normal and biome-specific grapes valid without importing Vinery classes.

## Provider policy

When Vinery is absent, Alcoholic's grape world generation and creative discoverability are active. When Vinery is present:

- Alcoholic registry IDs remain registered for save compatibility;
- existing Alcoholic vines remain functional;
- new Alcoholic wild-vine generation is disabled through a conditional Forge biome modifier;
- redundant grape items and cuttings are omitted from normal creative discoverability;
- Vinery grapes remain accepted through semantic tags.

Provider selection therefore changes acquisition policy, not registry identity. This avoids corrupting worlds when optional mods are added or removed.

When Vinery is present, its seed items may plant Alcoholic vines only in a valid [[trellis-training]] row. Other Vinery interactions stay with Vinery.

Phase 3 keeps the closed `IngredientType` enum as a viticulture compatibility adapter and adds `IngredientSemanticRegistry` so beverage pipelines can bind new tags without editing a finite Java list. Pipeline inputs prefer tags and definition ids over that enum.

Phase 4 PRESS definitions consume `#alcoholic:grapes/red` and `#alcoholic:grapes/white`. FERMENT consumes `#alcoholic:yeast` (currently `alcoholic:yeast`). Vinery grapes enter the same PRESS nodes through those grape tags. Machines still must not branch on concrete item classes.

## Barley and hops

ADR-029 reuses `CropProviderSelectionPolicy` for `CropKind.BARLEY` and `HOPS`. When Brewery is present, creative acquisition for native barley/hops is disabled and wild barley / wild hops worldgen is gated; Alcoholic registry ids stay registered. Optional `brewery:*` items join tags with `required: false`. NBT is not ingredient identity.

Grain tags:

- `#alcoholic:barley`, `#alcoholic:barley/seeds`
- `#alcoholic:malted_barley`, `#alcoholic:malted_grain`
- `#alcoholic:grist`, `#alcoholic:hops`, `#alcoholic:spent_grain`

Barley is an annual cereal. Hops grow as a vertical bine on generic trellis posts and wire, not as a grapevine subclass. `alcoholic:wild_hops` is a ground bush for survival bootstrap (rhizome + hops), gated like wild barley when Brewery is present. External agriculture mods contribute ingredients, not their brewing machines. See [[grain-processing]].

## Related

- [[alcoholic]]
- [[loader-independent-minecraft-architecture]]
- [[beverage-framework]]
- [[trellis-training]]
- [[harvest-lot-metadata]]
- [[artisanal-processing]]
- [[fermentation-physics]]
- [[forge-1.19.2-phase-1-verification]]
- [[cursor-phase-1-foundation-session]]
- [[cursor-phase-2-viticulture-session]]
- [[grain-processing]]
- [[cursor-phase-4-processing-session]]
- [[cursor-phase-7a-grain-session]]
