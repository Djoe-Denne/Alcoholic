---
title: Craft Scale Machines
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [MachineScale.CRAFT, craft malt house, craft vat]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/311c7aac-40ca-404b-b8c5-4d789891da7c/311c7aac-40ca-404b-b8c5-4d789891da7c.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/0071757b-137d-401f-bb1e-dcfded953f4b/0071757b-137d-401f-bb1e-dcfded953f4b.jsonl"
  - "C:/Users/djden/source/repos/Alcoholic/docs/audits/current-playability-audit.md"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/112827ce-4901-4cfd-8dad-1e626367ee8f/112827ce-4901-4cfd-8dad-1e626367ee8f.jsonl"
summary: >-
  A third executor scale between artisanal and industrial. Same process types, formed hollow cuboids, mid quality caps.
provenance:
  extracted: 0.84
  inferred: 0.14
  ambiguous: 0.02
created: 2026-09-08T20:30:00+02:00
updated: 2026-09-09T16:00:00+02:00
---

# Craft Scale Machines

Alcoholic already had artisanal blocks and [[industrial-multiblock]] plants. Sessions added a third `MachineScale.CRAFT`: formed like industrials (replace the look when the shell is valid) with mid [[emergent-quality-profile]] defaults.

## Shipped beer craft set

`BuiltinCraftMachines` registers malt house, mill, mash tun, brewing kettle, and vat. Casing is `alcoholic:craft_casing`. They execute the same generic MALT / MILL / MASH / BOIL / FERMENT types as [[grain-processing]], not a second recipe system. Wine PRESS / AGE / BLEND have no craft family.

Size is variable (hull 3³–5³). Mega-mesh overlays only the art size; other legal sizes keep the 9-slice hull ([[formed-multiblock-visual]]). Ports stay on real blocks at the faces the recipe requires, independent of mega-mesh size.

Craft malt house: 12 item slots, batch 384, no GUI tanks. Mash tun: two tanks. Vat and kettle: one tank. `MachineLayout.forMultiblock` is shared with industrial. See [[playability-audit]].

## Balance intent

Artisanal stays highest fidelity and lowest throughput. Craft sits between. Industrial wins volume and speed and is clamped by complexity cap / purity floor. One process definition per type.

## Related

- [[industrial-multiblock]]
- [[industrial-processing]]
- [[formed-multiblock-visual]]
- [[emergent-quality-profile]]
- [[machine-controller-telemetry]]
- [[cursor-quality-and-craft-session]]
- [[playability-audit]]
- [[alcoholic]]
