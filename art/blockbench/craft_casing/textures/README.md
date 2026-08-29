# Craft casing textures

`master-512` contains the approved 512 atlas and `SHA256SUMS.txt`.
It is the only source used to generate lower-resolution runtime textures.

Locked copper is copied from `craft_malt_house` (`craft_siding` +
`craft_frame`). Geometry stays the industrial 1×1 hull. Do not write this
atlas back onto `industrial_casing`.

## Brief modelage (agent Blockbench)

- **Id / type** : `craft_casing` — cube 1×1 hull partagé (revêtement).
- **Kit hull** : master cuivre à recopier pour les contrôleurs craft.
- **Face utile** : −Z. Java Block/Item.
