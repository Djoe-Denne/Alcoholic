---
title: Alcoholic Debug Commands
category: skills
tags: [minecraft, type/procedure, project/alcoholic]
aliases: [debug ports, /alcoholic inspect]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/9c25457e-4350-4b33-9dc9-83e0eb1511d1/9c25457e-4350-4b33-9dc9-83e0eb1511d1.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/d120a5cc-91ff-47b3-99d0-392583b27834/d120a5cc-91ff-47b3-99d0-392583b27834.jsonl"
summary: >-
  OP-2 /alcoholic debug place, ports, and kits plus inspect. Use one flat pad to review fluid cosmetics, kinetic faces, and both beer lines.
provenance:
  extracted: 0.92
  inferred: 0.06
  ambiguous: 0.02
created: 2026-08-28T19:15:00+02:00
updated: 2026-08-28T19:15:00+02:00
---

# Alcoholic Debug Commands

Everything sits under `/alcoholic`. `debug` needs **permission 2**. `inspect` does not.

## Ports

```
/alcoholic debug ports fluid [pos]
/alcoholic debug ports energy [pos]
```

Each command places a broken row and an aligned row plus signs. Fluid pad ≈ 20×10; energy pad ≈ 14×9.

Artisanal taps facing each other is **cosmetic**. Fluid I/O is `IFluidHandler` on every face. See [[artisanal-processing]] and [[cursor-machine-port-audit-session]].

Kinetic on the primitive engine is **not** cosmetic: only the right-hand shaft face transmits. A mill glued to the front grille gets no torque.

## Lines and single machines

```
/alcoholic debug place beer artisanal <pos>
/alcoholic debug place beer industrial <pos>
/alcoholic debug place <alias> <pos> [w h d]
```

Aliases include `malting_floor`, `malt_mill`, `mash_tun`, `brewing_kettle`, `fermenter`, `malt_house`, `roller_mill`, `industrial_mash_tun`, `industrial_brewing_kettle`, `vat`, `conditioning`, `tank`. Industrial sizes are at least 3. Formed look: [[formed-multiblock-visual]].

## Kits

```
/alcoholic debug kit wine agriculture|artisanal|industrial
/alcoholic debug kit beer agriculture|artisanal|industrial
```

Create drive extras are added to artisanal/industrial kits when Create is loaded.

## Inspect

```
/alcoholic inspect
```

Looks at the targeted block (vessel, machine, port) or the held bottle/bucket. Prints fluid, drive, and energy.

## One-shot pad

```
/alcoholic debug kit beer artisanal
/alcoholic debug kit wine artisanal
/alcoholic debug ports fluid ~ ~ ~
/alcoholic debug ports energy ~22 ~ ~
/alcoholic debug place beer artisanal ~ ~ ~8
/alcoholic debug place beer industrial ~ ~ ~20
```

Then `/alcoholic inspect` on each demo machine. If chat has no `ports` subcommand, the jar is stale — relaunch the instance. See [[curseforge-create2-deploy]].

## Related

- [[cursor-artisanal-brewery-guide-session]]
- [[cursor-machine-port-audit-session]]
- [[formed-multiblock-visual]]
- [[mechanical-drive-port]]
- [[alcoholic]]
