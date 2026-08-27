# ADR-032: Process Display Port and Recipe Viewers

- Status: Accepted
- Date: 2026-08-27
- Extends: [ADR-001](ADR-001-loader-independent-architecture.md),
  [ADR-006](ADR-006-beverage-framework-and-extension-api.md),
  [ADR-008](ADR-008-process-executor-capability-model.md)

## Context

Recipe viewers (JEI today, later REI/EMI or Fabric) must show the same
process definitions the engine executes. A closed switch on builtin
process types, invented 1000 mB volumes, and GUI `MachineLayout` as a
category key would make an addon process or a second viewer a core edit.

## Decision

- Recipe viewers are loader adapters. They live in `platform-forge-1.19.2`
  (and later a Fabric adapter). They do not own process IO.
- The IO projection is a port of the registered `ProcessType`. A decoded
  config implements `ProcessDisplaying`, or the type supplies
  `ProcessType.of(..., display)`. Otherwise viewers use
  `SolidAccepting` plus declared item inputs.
- `ProcessDisplaySpec` is the public DTO. Missing volumes stay empty;
  viewers must not invent bucket amounts.
- A machine advertises `MachineAccess.displayedProcessTypes()`. A JEI
  click opens those types only, never every process that shares a GUI
  layout.
- JEI `RecipeType` UIDs are the full `ResourceId` (namespace + path).
- JEI rebinds recipes on `AlcoholicApi.notifyCatalogReloaded()`. A jar
  or client-resource snapshot is bootstrap only. World datapacks on a
  dedicated-server client stay invisible until a catalog sync exists.

## Consequences

- A third-party process such as rice polishing appears in JEI when its
  config implements `ProcessDisplaying` or `SolidAccepting`. Core Java
  is not edited.
- Artisanal press, mill, and industrial controllers no longer share a
  clickable JEI category set.
- Fabric or REI adapters reuse `ProcessDisplaySpec` and
  `displayedProcessTypes()` without copying a Forge switch.
