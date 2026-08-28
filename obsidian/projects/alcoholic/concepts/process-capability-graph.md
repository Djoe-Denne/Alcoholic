---
title: Process Capability Graph
category: concepts
tags: [minecraft, software-architecture, type/concept, project/alcoholic]
aliases: [ProcessGraph, ProductionPipeline]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b282f8bd-e540-4028-93d1-896905419dcd/b282f8bd-e540-4028-93d1-896905419dcd.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b282f8bd-e540-4028-93d1-896905419dcd/subagents/5362e3cf-53fc-4eff-a34f-7cf2342088a9.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b282f8bd-e540-4028-93d1-896905419dcd/subagents/76fb1a5f-75ec-4a6a-9436-6a55a7cd0256.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/a6b5797c-82f8-4021-9d63-10a82fed6899/a6b5797c-82f8-4021-9d63-10a82fed6899.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/c2ca3b27-ad63-4be9-af24-47c49c111f2f/c2ca3b27-ad63-4be9-af24-47c49c111f2f.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/dc49ea79-b9e5-4902-ad7f-795f762f8f52/dc49ea79-b9e5-4902-ad7f-795f762f8f52.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/95c81b7c-fa88-4055-9741-14cb948964c9/95c81b7c-fa88-4055-9741-14cb948964c9.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06/416b9587-3d6b-43c7-ae7d-cfe21d2c2e06.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/1ca21b6f-a077-4686-8f47-c41ee6e59fc7/1ca21b6f-a077-4686-8f47-c41ee6e59fc7.jsonl"
summary: Production is an acyclic graph of named nodes. No process is mandatory. Official nodes need a native executor.
provenance:
  extracted: 0.88
  inferred: 0.1
  ambiguous: 0.02
created: 2026-08-25T12:50:00+02:00
updated: 2026-08-28T20:54:00+02:00
---

# Process Capability Graph

A beverage is a directed acyclic graph, not a single linear recipe and not a cultural script such as “crop then ferment.”

## Graph shape

Nodes have stable ids. Each node names a Java-registered process type and may reuse a datapack process definition. Inputs are a map of port name to selector: item, tag, defined ingredient, beverage identity, or another node's output port. Multiple inputs on one node express joins such as grist plus water.

The validator rejects cycles, unknown nodes or ports, unknown process types or properties, duplicate ids, and broken cross-file references. Diagnostics include a JSON path.

## No mandatory process

The graph is the only authority over which nodes exist. The engine never injects PRESS, FERMENT, DISTILL, AGE, BLEND, BOTTLE, MALT, MILL, MASH, or BOIL because a drink “should” have them. Wine and cider use PRESS then FERMENT. The shipped wine DAG is PRESS → FERMENT → AGE. BLEND is an optional crock capability, not a graph node. BOTTLE is a vessel action, not a DAG node. Shipped beer uses MALT → MILL → MASH → BOIL → FERMENT. There is no official beer AGE definition. CONDITION is industrial-only and off-DAG. A rum-style fixture may FERMENT only, then optionally AGE in data. Fruit liqueur may INFUSE only. Tests cover omitted nodes, cycles named by remaining node IDs, and a press→ferment+infuse branch.

Once a process type is part of official progression, the [[native-executor-invariant]] requires a native Alcoholic machine. Optional integrations may add extra executors for the same node type.

## Capability versus identity

[[artisanal-processing]] and the [[create-press-adapter]] execute `alcoholic:press`. The oak barrel advertises `alcoholic:age`. The crock advertises `alcoholic:blend`. They must not ask whether a recipe is wine. The same capability juices apples or ages a fixture liquid once a datapack says so. Beverage JSON never names `artisanal_press` or `oak_barrel`. That continues ADR-003 and ADR-008.

Runtime asks two questions: can this executor run process type T with these inputs, and what does `ProcessType.apply` produce? Application `ExecuteProcessUseCase` does not depend on the default `ProcessExecutor.execute()` unsupported stub.

## Why the fixtures exist

Cider in `testpack:` proves the engine is not a wine framework and needs no cider Java types. Fruit liqueur proves a beverage can consume another beverage and omit fermentation. Whisky `testpack:age_new_make` plus cider/rum AGE nodes prove [[aging-process]] without drink-family Java. Those graphs remain validation fixtures. Wine PRESS/FERMENT/AGE and grain [[grain-processing]] through FERMENT are shipped gameplay. BLEND is a crock capability, not a wine graph node. BOTTLE is a vessel action, not a DAG node. Extra wheat-beer and non-beer mash graphs stay fixtures.

## Related

- [[alcoholic]]
- [[beverage-framework]]
- [[public-extension-api]]
- [[liquid-batch]]
- [[artisanal-processing]]
- [[create-press-adapter]]
- [[industrial-processing]]
- [[grain-processing]]
- [[native-executor-invariant]]
- [[mechanical-drive-port]]
- [[electric-motor]]
- [[aging-process]]
- [[blend-versus-tank-merge]]
- [[loader-independent-minecraft-architecture]]
- [[cursor-phase-3-beverage-framework-session]]
- [[cursor-phase-4-processing-session]]
- [[cursor-phase-5-aging-session]]
- [[cursor-phase-6-industrial-session]]
- [[cursor-phase-7a-grain-session]]
