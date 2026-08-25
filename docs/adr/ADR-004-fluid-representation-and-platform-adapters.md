# ADR-004: Fluid Representation and Platform Adapters

- Status: Accepted
- Date: 2026-08-25

## Context

Brewing and distillation will eventually move liquids between containers and
machines. Forge represents such storage with types including `FluidStack`,
while Fabric and Create expose different storage and transfer contracts.
Using a platform stack as the domain representation would couple composition,
processing rules, persistence, and tests to one loader.

The required operations and fidelity are not yet established in Phase 1.

## Decision

`FluidStack` is forbidden in the domain model. Forge, Fabric, Create, vanilla
registry, capability, and transfer types must not appear in domain values or
application-facing contracts.

When a liquid model is required, represent the liquid as a platform-independent
composition of domain components and quantities. The model must preserve the
meaning of a mixture rather than treating a platform fluid registry entry as
the authoritative domain identity. Exact units, precision, metadata, and
persistence format will be decided from the first concrete use cases.

Introduce small ports only when a use case needs them. Each port will expose
the minimum operation required by that use case, such as observing available
composition or requesting a bounded transfer, with explicit transaction and
failure semantics. Do not create a universal fluid-storage abstraction in
advance.

Platform adapters own conversion:

- Forge adapters may use `FluidStack`, capabilities, and Forge transfer rules.
- Future Fabric adapters may use Fabric storage and transfer types.
- Create-specific Forge and Fabric adapters remain separate and translate
  through the use-case ports rather than exporting Create fluid types inward.

No liquid representation, fluid port, registration, container, or adapter is
implemented during Phase 1.

## Consequences

- Domain processing can model mixtures without a Forge dependency.
- Platform serialization or transfer can be lossy; adapters must make any loss
  or unsupported composition explicit.
- Ports stay narrow and can match real transactional requirements.
- Forge and Fabric can use native storage APIs without forcing a lowest-common-
  denominator platform type into the domain.
- Fluid implementation is intentionally deferred until Phase 2 supplies a
  tested use case.
