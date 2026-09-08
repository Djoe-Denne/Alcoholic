---
title: Machine Controller Telemetry
category: concepts
tags: [minecraft, compatibility, type/concept, project/alcoholic]
aliases: [controller data overlay, MachineTelemetryOverlay]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/c1fe1ca7-b44b-4208-8155-4cb69e690435/c1fe1ca7-b44b-4208-8155-4cb69e690435.jsonl"
summary: >-
  Formed controllers show temperature, stage, and drive beside the inventory. The panel sits left of the GUI so JEI does not cover it.
provenance:
  extracted: 0.9
  inferred: 0.08
  ambiguous: 0.02
created: 2026-09-08T20:30:00+02:00
updated: 2026-09-08T20:30:00+02:00
---

# Machine Controller Telemetry

Right-clicking a formed controller opens the shared machine screen. When `FLAG_CONTROLLER_TELEMETRY` is set, a side panel lists structure formed/unformed, process profile, stage, progress, ambient and heat temperatures, preferred/operating bands, humidity, kiln minimum, and drive.

`imageWidth` stays the inventory panel (176). The overlay is extra geometry, not a wider container.

## JEI

JEI 11.6 places its ingredient list just right of `leftPos × imageWidth`. A first cut drew the panel on the right, so JEI covered half the lines. The panel now sits **left** (`OFFSET_X = -WIDTH - 4`, width 152). `MachineScreenGuiHandler.getGuiExtraAreas` returns that `Rect2i` so bookmarks (often left) also shift. Clicks on the panel are inside the GUI (`hasClickedOutside`).

Clickable recipe areas on the progress arrow are unchanged. See [[process-display-and-recipe-viewers]].

## Related

- [[craft-scale-machines]]
- [[industrial-multiblock]]
- [[process-display-and-recipe-viewers]]
- [[cursor-machine-gui-and-malting-session]]
- [[alcoholic]]
