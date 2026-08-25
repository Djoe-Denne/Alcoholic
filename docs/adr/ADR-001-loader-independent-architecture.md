# ADR-001: Loader-Independent Architecture

- Status: Accepted
- Date: 2026-08-25

## Context

Alcoholic initially targets Forge for Minecraft 1.19.2, but its gameplay rules
must remain reusable by later platform distributions, including Fabric. Loader
APIs are pervasive when registry objects, events, capabilities, or networking
types cross module boundaries. Allowing those types into core logic would make
a future loader port a rewrite rather than an adapter.

## Decision

Use an inward-pointing, multi-module architecture:

- `domain` contains pure Java domain concepts and rules.
- `platform-api` contains pure Java identifiers and narrow platform ports. It
  may depend on `domain`, but not on Minecraft or a loader.
- `application` contains pure Java use cases and orchestration. It depends only
  on `domain` and `platform-api`.
- `minecraft-common` may use vanilla Minecraft APIs only. It must not import
  Forge, Fabric, or third-party mod APIs.
- Third-party integrations are isolated from core behavior. Loader-neutral
  policy belongs in integration modules; loader-specific access belongs in
  dedicated adapter modules.
- `platform-forge-1.19.2` is the only Forge-aware platform module and the Forge
  composition root.
- A future Fabric target will provide Fabric-specific platform and integration
  adapters while reusing the pure and vanilla-common modules unchanged.

Build-time architecture checks enforce forbidden imports. Platform registry
objects, event types, capabilities, and networking types must be translated at
adapter boundaries rather than passed inward.

## Consequences

- Domain and application tests run without Minecraft or a loader.
- Loader migrations are localized to adapters and composition roots.
- Forge and Fabric adapters may contain deliberate duplication where their
  lifecycle or API semantics differ.
- Features that cannot be expressed without a loader dependency stay outside
  the pure modules.
- The initial Forge delivery requires more explicit mapping and wiring than a
  single-module mod.
