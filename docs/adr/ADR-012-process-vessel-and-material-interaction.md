# ADR-012: Process Vessel and Material Interaction

- Status: Accepted
- Date: 2026-08-25

## Context

Long-running AGE needs material and cellar effects. Encoding those on a
BlockEntity would couple domain physics to Minecraft and make addons write
a new BE for every wood or clay vessel.

## Decision

Vessel and environment are views, not block entities:

- `VesselProfile` / `VesselProfileView`: material, capacity, process
  capabilities, permeability, wood and oxidation multipliers, optional
  opaque `BarrelHistory`.
- `EnvironmentProfile` / `EnvironmentProfileView`: temperature, stability,
  sheltered. No biome IDs.

Profiles are registered on `AlcoholicApi.vessels()` during bootstrap and
frozen with the rest of the API. A datapack can still describe AGE without
a new Java vessel; a new *block* remains Java.

Minecraft adapters sample a compact cellar profile (`canSeeSky`, Y vs sea
level, vanilla biome temperature, six solid neighbours) and cache it.
`ProcessContext` exposes `vessel()`, `environment()`, and `gameTime()`.

## Consequences

- `AgingPhysics` stays beverage-agnostic and testable without a world.
- An addon registers a vessel profile for a new material; the oak barrel
  is one content adapter of that profile.
- History IDs are semantic liquid ids, not drink-family classes.
