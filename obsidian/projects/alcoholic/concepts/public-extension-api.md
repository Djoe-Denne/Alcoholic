---
title: Public Extension API
category: concepts
tags: [minecraft, software-architecture, compatibility, type/concept, project/alcoholic]
aliases: [alcoholic-api, DistilleryApi]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b282f8bd-e540-4028-93d1-896905419dcd/b282f8bd-e540-4028-93d1-896905419dcd.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b282f8bd-e540-4028-93d1-896905419dcd/subagents/5362e3cf-53fc-4eff-a34f-7cf2342088a9.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b282f8bd-e540-4028-93d1-896905419dcd/subagents/76fb1a5f-75ec-4a6a-9436-6a55a7cd0256.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/a6b5797c-82f8-4021-9d63-10a82fed6899/a6b5797c-82f8-4021-9d63-10a82fed6899.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/c2ca3b27-ad63-4be9-af24-47c49c111f2f/c2ca3b27-ad63-4be9-af24-47c49c111f2f.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/dc49ea79-b9e5-4902-ad7f-795f762f8f52/dc49ea79-b9e5-4902-ad7f-795f762f8f52.jsonl"
summary: Addons compile only against alcoholic-api. Executors advertise process types; vessels freeze with the rest of the API.
provenance:
  extracted: 0.88
  inferred: 0.12
  ambiguous: 0.0
created: 2026-08-25T12:50:00+02:00
updated: 2026-08-25T16:01:00+02:00
---

# Public Extension API

The published module is `alcoholic-api` under `com.djden.alcoholic.api`. The brief used the conceptual name `distillery-api`; the repository namespace stays `alcoholic`.

## Two extension levels

1. **Datapack** — a new beverage that only uses registered process types, properties, and selectors needs no Java.
2. **Java addon** — a new process type or liquid property is registered on `AlcoholicApi` during bootstrap, then referenced from data.

The API is frozen at Forge `FMLCommonSetupEvent` with lowest priority. Duplicate registration fails. After freeze, only read views remain.

## What is stable

`@PublicApi` types, `ApiVersion`, `ResourceId`, `DataNode` / `DataCodec`, process and property registrars, and `ProcessExecutor` are the promised surface. Domain, application, Minecraft-common, and loader modules are internal. Codecs are pure Java 17; Gson and `ResourceLocation` stay in adapters.

Phase 4 makes `ProcessExecutor` operational: `supportedProcesses()` advertises ids such as `alcoholic:press`. Phase 5 adds `AlcoholicApi.vessels()` for [[vessel-and-environment]] profiles. Those freeze with the rest of the API. A datapack can attach AGE to an existing vessel without a new Java profile. A new *block* remains Java.

Application code selects an executor by capability, not machine identity. Phase 6 lets the execution context carry throughput, yield, and stability modifiers so [[industrial-processing]] remains an alternate executor rather than a second recipe language.

A compile-only `integration-test-addon` registers `testaddon:rice_polishing` and `testaddon:polishing_ratio` to prove a third-party process needs no core edits.

## Purity

Generic engine Java must not name `Wine`, `Beer`, `Whisky`, or `Rum`. Those words belong in content, fixtures, tests, and compatibility. Gradle `checkBeverageFrameworkPurity` enforces that, including `domain/vessel`, `domain/multiblock`, `application/machine`, environment sampling, inspect helpers, and industrial adapters.

## Related

- [[alcoholic]]
- [[beverage-framework]]
- [[process-capability-graph]]
- [[loader-independent-minecraft-architecture]]
- [[artisanal-processing]]
- [[vessel-and-environment]]
- [[aging-process]]
- [[industrial-multiblock]]
- [[industrial-processing]]
- [[cursor-phase-3-beverage-framework-session]]
- [[cursor-phase-4-processing-session]]
- [[cursor-phase-5-aging-session]]
- [[cursor-phase-6-industrial-session]]
- [[forge-1.19.2-phase-2-3-verification]]
- [[forge-1.19.2-phase-4-verification]]
- [[forge-1.19.2-phase-5-verification]]
- [[forge-1.19.2-phase-6-verification]]
