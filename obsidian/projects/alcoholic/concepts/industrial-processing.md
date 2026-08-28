---
title: Industrial Processing
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [industrial press, industrial vat, industrial tank, industrial malt house, industrial roller mill, industrial mash tun, industrial brewing kettle]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/dc49ea79-b9e5-4902-ad7f-795f762f8f52/dc49ea79-b9e5-4902-ad7f-795f762f8f52.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/1ca21b6f-a077-4686-8f47-c41ee6e59fc7/1ca21b6f-a077-4686-8f47-c41ee6e59fc7.jsonl"
summary: Extra executors for existing process types plus a passive tank. Kinetic power is any MechanicalDrivePort supply, not Create-only.
provenance:
  extracted: 0.86
  inferred: 0.12
  ambiguous: 0.02
created: 2026-08-25T15:30:00+02:00
updated: 2026-08-28T22:30:00+02:00
---

# Industrial Processing

Industrial machines are additional executors for existing process types. They do not add industrial wine recipes.

```
PRESS     artisanal / Create / industrial
FERMENT   artisanal / industrial vat
MALT      malting floor / industrial malt house
MILL      malt mill / industrial roller mill / Create millstone / crushing wheels
MASH      mash tun / industrial mash tun
BOIL      brewing kettle / industrial brewing kettle
CONDITION optional industrial conditioning vessel
STORAGE   passive tank (never a process executor)
```

Wine and beer DAGs are unchanged. Industrial machines change capacity, throughput, automation, and stability. They do not add industrial recipes. Artisanal executors stay valid.

## Industrial press

The press consumes [[mechanical-drive-port]] power (primitive engine or [[electric-motor]] adjacent to the kinetic port, Create shafts, or a [[crossroads-rotary-adapter]] axle), aggregates large homogeneous batches, and reuses generic PRESS outputs and byproducts. Executor modifiers improve throughput and yield without changing recipe semantics. Tick dispatch is by `ProcessType` (`IndustrialRuntime.strategy`). `MachineKind` names process families (MALT, MILL, MASH, BOIL, CONDITION) and never a drink identity.

Its crush-zone easter egg is active only during the compression stroke; incidental edge contact is safe.

## Industrial fermentation vat

The vat executes the same FERMENT process as the artisanal fermenter, with larger volume and greater thermal stability. One vat holds one [[liquid-batch]], and progress scales with elapsed time rather than per-unit simulation.

## Industrial storage tank

The tank is a passive vessel, not a process executor. It preserves batch metadata and exposes the same [[industrial-ports]]. Storage never implies MASH, BOIL, FERMENT, CONDITION, AGE, or BLEND.

## Industrial brewery

The malt house executes generic `MALT` with internal steeping / germination / kilning stages. The roller mill executes generic `MILL` through [[mechanical-drive-port]] (higher load than the small mill). The mash tun and brewing kettle reuse generic `MASH` and `BOIL`. The existing industrial vat ferments hopped wort through generic `FERMENT`. Optional `CONDITION` can mature a young liquid without becoming a global DAG stage.

## Create

Alcoholic does not add pipes, pumps, belts, or shafts. The intended factory is vineyard or grain intake → belts → industrial executors → pipes → tank → pipes → vat. Without Create, the primitive engine or [[electric-motor]] can still drive kinetic machines. See [[create-press-adapter]] for the separate Mechanical Press + Basin compacting path.

## Related

- [[industrial-multiblock]]
- [[industrial-ports]]
- [[mechanical-drive-port]]
- [[electric-motor]]
- [[crossroads-rotary-adapter]]
- [[native-executor-invariant]]
- [[artisanal-processing]]
- [[create-press-adapter]]
- [[fermentation-physics]]
- [[cursor-phase-6-industrial-session]]
- [[cursor-create-independence-session]]
- [[cursor-crossroads-electric-motor-session]]
- [[grain-processing]]
- [[industrial-progression-and-jei-formation]]
- [[forge-1.19.2-phase-6-verification]]
