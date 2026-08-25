# ADR-017: Generic Variable-Size Multiblock Architecture

- Status: Accepted
- Date: 2026-08-25

## Context

Phase 6 needs an industrial press, fermentation vat, and storage tank.
A one-block-entity-equals-one-machine model cannot express interior
volume or ports. Fixed blueprints such as "must be exactly 5x5x7" would
force a rewrite for every later machine family.

## Decision

Alcoholic uses one hollow-cuboid family for all Phase 6 machines.

- `MultiblockDefinition` is a data-shaped machine family: kind, process
  capability, dimension bounds, casing/window/port tags, capacity per
  interior block, executor modifiers, and kinetic requirement.
- `HollowCuboidValidator` is the only geometry engine. It walks a
  connected shell, measures the bounding box, and treats strictly interior
  empty cells as process volume.
- Capacity is `interiorVolumeBlocks * capacityPerInternalBlock`. Casing,
  controllers, ports, and obstructions never count as fluid volume.
- Domain types use `CellCoord` / `StructureQuery`. They do not mention
  `BlockPos`, `Level`, or `BlockEntity`.
- Minecraft adapters (`WorldStructureSampler`, controllers, ports) map
  blocks and tags into that query.

Beverage names never appear in generic multiblock classes. Beer, whisky,
or rum machinery later adds a definition and an executor, not a second
validator.

## Consequences

- Players can grow a vat or tank within configured min/max exteriors.
- Third-party casings join through block tags, not validator patches.
- A press, vat, and tank share the same formation rules.
