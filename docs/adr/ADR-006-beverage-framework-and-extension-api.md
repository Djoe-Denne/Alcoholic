# ADR-006: Beverage Framework and Extension API

- Status: Accepted
- Date: 2026-08-25

## Context

Phase 2 delivered viticulture without a processing engine. Implementing a
press or fermentation loop as wine-specific services would force later beer,
whisky, rum, and liqueur work to retrofit a closed hierarchy. Addons would
also have no stable surface for new process types or liquid properties.

The required gameplay machines are not part of Phase 3. The required
architecture is: beverages as data, processes as capabilities, and an
explicit public API that does not leak internal modules.

## Decision

- Publish `alcoholic-api` as the only versioned Java surface. Addons compile
  against `com.djden.alcoholic.api.*` and never against domain, application,
  or loader modules.
- Represent identifiers with `ResourceId` in that API. Domain models may
  depend on the API; the API must not depend on domain.
- Model production as a directed acyclic graph of named nodes with named
  ports. A node references a Java-registered process type and may reuse a
  datapack process definition.
- Keep `LiquidBatch` as domain composition plus typed properties. Beverage
  identity is metadata, not a Minecraft fluid type. This continues ADR-004:
  no `FluidStack` enters the core.
- Register process types and properties in Java during bootstrap, then freeze
  the API before the first datapack reload. Ingredient, process, and beverage
  definitions reload atomically from
  `data/<namespace>/alcoholic/{ingredients,processes,beverages}/`.
- Provide two extension levels: datapacks for new beverages that reuse known
  process types, and Java addons for new process types or properties.
- Machines and future integrations target process capabilities such as
  `alcoholic:press` or `alcoholic:mill`. They must not branch on a beverage
  identity. Create remains an optional later executor, consistent with
  ADR-003.
- Built-in process types and properties are content registrations, not engine
  switches. Generic engine packages must not name a finite drink family.

## Consequences

- Cider, beer, whisky, rum, and liqueur can be expressed as validation
  fixtures without gameplay machines.
- A third-party process such as rice polishing can be registered through
  `AlcoholicApi` and used from a datapack without modifying core classes.
- JSON codecs stay in pure Java `DataNode` / `DataCodec` types. Gson and
  Minecraft resource types remain adapters.
- Invalid datapack reloads keep the previous catalog snapshot.
- Phase 4 can implement `PRESS` and `FERMENT` execution against this graph
  without introducing wine-specific services.
