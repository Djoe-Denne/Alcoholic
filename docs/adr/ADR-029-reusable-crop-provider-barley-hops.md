# ADR-029: Reusable Crop-Provider Model for Barley and Hops

- Status: Accepted
- Date: 2026-08-25
- Supersedes in part: [ADR-002](ADR-002-semantic-tags-and-crop-providers.md)

## Context

Phase 2 used crop providers for grapes / Vinery. Barley and hops need the
same policy without coupling hop bines to grapevine classes.

## Decision

`CropProviderSelectionPolicy` already maps `CropKind.BARLEY` and `HOPS` to
Brewery when that mod is present. Alcoholic still registers its items and
blocks. When Brewery is preferred:

- creative acquisition for native barley/hops is disabled;
- wild barley worldgen is gated with `forge:not` / `alcoholic:item_present` on `brewery:barley`;
- wild hops worldgen (`alcoholic:wild_hops`) is gated the same way on `brewery:hops`;
- native blocks remain in the registry for save compatibility.

Semantic tags are the ingredient identity:

- `#alcoholic:barley`, `#alcoholic:barley/seeds`
- `#alcoholic:malted_barley`, `#alcoholic:malted_grain`
- `#alcoholic:grist`, `#alcoholic:hops`, `#alcoholic:spent_grain`

Optional `brewery:*` item ids are tag members with `required: false`. NBT is
not ingredient identity; solid property NBT only carries process state.

Hops grow as a vertical bine on generic `CropSupportPost` + trellis wire,
not as reskinned wheat and not as a grapevine subclass. Barley is an annual
`CerealCropBlock` (seedling / growing / mature).

## Consequences

External agriculture mods contribute ingredients, not their brewing
machines. Whisky can later reuse malted grain tags without a beer provider.
