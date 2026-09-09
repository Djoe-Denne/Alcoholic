---
title: Playability Audit
category: concepts
tags: [minecraft, testing, type/concept, project/alcoholic]
aliases: [C1-C26, current-playability-audit]
sources:
  - "C:/Users/djden/source/repos/Alcoholic/docs/audits/current-playability-audit.md"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/112827ce-4901-4cfd-8dad-1e626367ee8f/112827ce-4901-4cfd-8dad-1e626367ee8f.jsonl"
summary: >-
  2026-09-09 audit of wine and beer through bottling. C1–C26 remediations: mash tanks, young beer, craft FORMED, bottle-stand crafts, no stand-network crash.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-09-09T16:00:00+02:00
updated: 2026-09-09T16:00:00+02:00
---

# Playability Audit

`docs/audits/current-playability-audit.md` maps what a survival player can do through bottling, then lists gaps. A Cursor session on 2026-09-09 remediates **C1–C26**. Distill the corrected loop here, not the obsolete bug list. Characterization lives in `PlayabilityAuditCharacterizationTest`.

The motivating example was craft vs industrial malt houses sharing a single barley slot and processing lots one-by-one, which made the upgrade pointless. Formed malt houses now keep datapack duration and accept many lots in parallel ([[grain-processing]], [[craft-scale-machines]]).

## Shipped loops after C1–C26

Wine: grapes → PRESS → FERMENT (`young_*_wine`) → bottle 250 mB, optional AGE, optional BLEND of young+young of the same colour back to young. Finished wine still needs AGE (oak barrel or [[industrial-processing|industrial aging vessel]]). Blend no longer emits finished wine without a cask ([[blend-versus-tank-merge]]).

Beer: barley → MALT pale → MILL → MASH (two tanks) → BOIL + hops → FERMENT → `young` beer → bottle. Optional industrial `CONDITION` is a node on `alcoholic:beer` (`condition_beer`) with no artisanal executor. Amber/dark malt stays a sneak cycle on the floor, off the official graph.

`DISTILL` and `INFUSE` stay stubs. Cider, whisky, rum, and drinking stay out of this pass. Marc and spent grain already compost (0.3).

## GUI and heat

`MachineLayout.forMultiblock` matches process IO: MASH two slots and two tanks at every scale; FERMENT/BOIL one slot and one tank; AGE/CONDITION one tank; MILL two slots; MALT dedicated layouts (craft malt house: twelve slots, no tanks). Heat is sampled under the controller **and** the interior floor. Magma (~65 °C) can mash and stall boil; campfire/lava (~100 °C) can boil.

## Progression

[[wine-beer-progression-graph]] adds five craft `form_craft_*` nodes after malt / mill / mash / boil / `ferment_beverage`. Vanilla industrial AGE parents `form_industrial_vat`. FTB still accepts vat **or** conditioning. Process quest `condition_beer` sits under conditioning.

## Display furniture

[[bottle-stands]] have vanilla recipes (`bottle_rack`: planks + stick + bottle; `bottle_shelf`: slabs + planks) without Create XOR. Adjacent tiling used to crash (`BottleStandNetwork`); that path is guarded.

## Still later

Create circular harvesters for vines/hops remain unwired ([[column-harvest]]). `spirit.json` is a testpack fixture, not a shipped drink. Wiki pages that still say industrial AGE is “next” are stale relative to HEAD `5a6fdad`. ^[inferred]

## Related

- [[wine-beer-progression-graph]]
- [[grain-processing]]
- [[craft-scale-machines]]
- [[industrial-processing]]
- [[bottle-stands]]
- [[blend-versus-tank-merge]]
- [[machine-controller-telemetry]]
- [[cursor-playability-audit-session]]
- [[alcoholic]]
