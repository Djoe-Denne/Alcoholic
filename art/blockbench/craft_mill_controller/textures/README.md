# craft_mill_controller textures

`master-512` contains the approved 512 atlas and `SHA256SUMS.txt`.
It is the only source used to generate lower-resolution runtime textures.

Hull cubes are a copper recolor of `industrial_roller_mill_controller`. Desk / pictogram /
fitting cubes stay on the industrial paint (steel pupitre, lamps off).

Do not write this atlas back onto `industrial_roller_mill_controller`.

## Brief

- **Id** : `craft_mill_controller` — contrôleur unitaire 1×1 (non formé).
- **Refs** : `art/blockbench/craft_mill_controller/reference.png`.
- **Kit hull** : cuivre `craft_casing` ; pupitre −Z ; extras = hazard_stripe, tray, bay_w, bay_e, bay_u, bolt_tl_pad, bolt_tl_head, bolt_tr_pad…
- **Face utile** : −Z. Java Block/Item.
- **Datagen** : `CraftAssetData` blockstate + item only.
