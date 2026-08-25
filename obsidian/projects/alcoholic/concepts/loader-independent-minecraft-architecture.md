---
title: Loader-Independent Minecraft Architecture
category: concepts
tags: [minecraft, software-architecture, compatibility, type/concept, project/alcoholic]
sources:
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b9ff420b-fd71-4089-83bc-cc4766880b14/b9ff420b-fd71-4089-83bc-cc4766880b14.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b9ff420b-fd71-4089-83bc-cc4766880b14/subagents/bd9af1a2-530d-4463-a8dc-715f5c4a790f.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/b282f8bd-e540-4028-93d1-896905419dcd/b282f8bd-e540-4028-93d1-896905419dcd.jsonl"
  - "C:/Users/djden/.cursor/projects/c-Users-djden-source-repos-Alcoholic/agent-transcripts/a6b5797c-82f8-4021-9d63-10a82fed6899/a6b5797c-82f8-4021-9d63-10a82fed6899.jsonl"
summary: Alcoholic keeps a public Java API and pure domain, then vanilla-common behavior, with Forge fluids and Create behind adapters.
provenance:
  extracted: 0.86
  inferred: 0.14
  ambiguous: 0.0
created: 2026-08-25T09:20:00+02:00
updated: 2026-08-25T14:10:00+02:00
---

# Loader-Independent Minecraft Architecture

Alcoholic uses an inward-pointing multi-module architecture so loader ports are additive adapters rather than rewrites.

## Boundaries

- `alcoholic-api` is the only versioned addon surface. It must not depend on domain, application, Minecraft, or a loader. See [[public-extension-api]].
- `domain` contains pure Java viticulture and beverage models and may depend on the public API for `ResourceId`.
- `platform-api` contains narrow platform ports and now reuses the public `ResourceId`.
- `application` contains compatibility, crop-provider, semantic-resolution, and beverage-catalog policy.
- `minecraft-common` may use vanilla Minecraft classes but not Forge, Fabric, Create, or Vinery classes.
- integration modules own optional-mod boundaries without leaking external APIs inward. `integration-create` translates PRESS data to Create compacting recipes in pure Java; see [[create-press-adapter]].
- `integration-test-addon` compiles only against `alcoholic-api`.
- `platform-forge-1.19.2` owns Forge bootstrap, registries, events, datagen, client setup, fluid registration, and `IFluidHandler` adapters over domain [[liquid-batch]] storage.

Gradle source checks reject forbidden imports in core and common modules. This is intentionally narrower than a speculative universal abstraction framework.

## Registration

Common content receives typed `RegistryPort` instances. The Forge adapter translates these operations to `DeferredRegister` and `RegistryObject`, keeping loader registries out of gameplay classes.

Registry objects must be constructed inside deferred suppliers. Constructing an `Item` in the mod constructor caused Forge datagen to fail because vanilla registries were already frozen; delaying construction fixed the lifecycle violation.

## Future direction

A Fabric target should reuse the pure and vanilla-common modules while replacing registration, mod-presence, storage, and loader lifecycle adapters. Forge and Fabric Create adapters may differ when their APIs differ rather than hiding differences behind false equivalence. ^[inferred]

## Related

- [[alcoholic]]
- [[semantic-crop-compatibility]]
- [[perennial-viticulture]]
- [[beverage-framework]]
- [[liquid-batch]]
- [[public-extension-api]]
- [[forge-1.19.2-phase-1-verification]]
- [[forge-1.19.2-phase-2-3-verification]]
- [[forge-1.19.2-phase-4-verification]]
- [[cursor-phase-1-foundation-session]]
- [[cursor-phase-3-beverage-framework-session]]
- [[cursor-phase-4-processing-session]]
