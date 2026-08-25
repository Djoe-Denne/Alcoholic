# ADR-003: Create Integration Boundary

- Status: Accepted
- Date: 2026-08-25

## Context

Create is optional and exposes loader- and version-specific machinery,
registries, recipes, and fluid behavior. Phase 1 has no validated Alcoholic
use case that establishes which Create capabilities are actually required.
Designing a generic machinery port now would encode guesses and leak Create's
current API shape into the application.

## Decision

Do not invent an operational Create port during Phase 1.

`integration-create` reserves a loader-neutral integration boundary, but it
must not acquire speculative interfaces merely to make that boundary appear
implemented. The base application remains complete and runnable without Create.
The `-PwithCreate=true` Gradle property only places Create on the Forge runtime
classpath for compatibility work.

In Phase 2, start from concrete application use cases and tests. Introduce only
the smallest required ports after the needed operations, failure semantics, and
ownership of transactions are known.

Keep loader adapters distinct:

- `integration-create-forge-1.19.2` will translate the required ports to the
  Forge build of Create.
- a future `integration-create-fabric-*` module will translate the same
  use-case requirements to the Fabric build of Create.

Neither adapter may expose Create or loader types to `integration-create`,
`application`, or `domain`. Forge and Fabric lifecycle wiring belongs in their
respective platform composition roots.

## Consequences

- Phase 1 contains no false abstraction presented as working Create support.
- The Phase 2 port surface is driven by tested behavior rather than Create API
  convenience.
- Forge and Fabric adapters can evolve independently when their Create APIs
  differ.
- Some adapter code may be duplicated to preserve loader isolation.
- Create remains an optional integration and cannot become a prerequisite for
  core brewing behavior.
- Operational Create adapters (heat, millstone/crushing, kinetic drive)
  live in `integration-create-forge-1.19.2`. They implement Alcoholic
  ports such as `HeatSources.Probe` and `MechanicalDrivePort`. See
  [ADR-030](ADR-030-native-mechanical-executors.md).
