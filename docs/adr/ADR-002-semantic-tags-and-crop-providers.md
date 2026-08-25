# ADR-002: Semantic Tags and Crop Providers

- Status: Accepted
- Date: 2026-08-25

## Context

Alcoholic recipes need grape semantics without depending on one mod's item or
crop classes. Vinery can already provide grapes, while Alcoholic may also own
native crops. Replacing or unregistering content when Vinery is selected would
break registry stability and existing worlds.

## Decision

Define grape meaning through Minecraft item tags:

- `#alcoholic:grapes` is the umbrella tag accepted by color-independent rules.
- `#alcoholic:grapes/red` identifies red grapes.
- `#alcoholic:grapes/white` identifies white grapes.

The red and white tags are included in the umbrella tag. Alcoholic processing
uses these semantic tags instead of hard-coded Vinery or Alcoholic item IDs.
Data packs and integration-generated tag entries map concrete items to the
semantic categories.

Separate crop ownership from ingredient meaning through a crop-provider
selection boundary. A provider describes which crop family supplies grapes and
which native content should be discoverable; domain rules continue to consume
semantic grape categories.

When the Vinery provider is selected:

- Vinery grapes are added to the appropriate semantic tags.
- Alcoholic does not unregister, replace, or remap Vinery or Alcoholic registry
  entries.
- new Alcoholic grape world generation is disabled;
- native Alcoholic grape crops, seeds, and related acquisition paths are
  removed from normal discoverability;
- already-saved Alcoholic crop blocks remain registered and functional, so old
  worlds can load and those blocks can continue their normal behavior.

Provider selection changes exposure and generation policy, not registry
identity. It must be decided without late registry mutation.

## Consequences

- Recipes interoperate with Vinery and data-pack additions through stable
  semantics.
- Existing saves remain valid when provider selection changes.
- Inactive native crops can still exist in old chunks and yield their existing
  content; they are not newly generated or normally advertised.
- Tag correctness and provider-selection behavior require data-generation and
  GameTest coverage.
- Crop-specific mechanics stay in integration or platform adapters rather than
  entering the domain model.
