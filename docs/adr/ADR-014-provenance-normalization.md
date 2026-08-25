# ADR-014: Provenance Normalization

- Status: Accepted
- Date: 2026-08-25

## Context

Blend and merge need a compact origin story. A recursive tree of every
parent batch would grow without bound and is not required for gameplay or
debug inspect.

## Decision

`BatchProvenance` is a flattened snapshot:

- `originComposition`: ingredient / variety fractions
- `blendComposition`: liquid-definition fractions
- summaries: fermentation stress, total aging time, wood, oxidation

Each merge or blend flattens maps. At most 16 entries per map. Fractions
below 0.5% are dropped. Remaining weights renormalize to 1.0.

NBT v2 stores this snapshot. v1 migrates to empty provenance. Unknown or
malformed tags become `Optional.empty()` with a debug log.

## Consequences

- Inspect and bottle snapshots can show origins without a tick journal.
- Tiny contributions disappear; that is accepted precision loss.
- Addons must not rely on a parent pointer into a previous batch.
