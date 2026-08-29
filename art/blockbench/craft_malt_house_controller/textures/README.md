# craft_malt_house_controller textures

`master-512` contains the approved 512 atlas and `SHA256SUMS.txt`.
It is the only source used to generate lower-resolution runtime textures.

Hull tiles are copied from locked `craft_malt_house` copper
(`craft_siding` + `craft_frame`). Desk / pictogram / lever tiles stay on
this controller's unique 128 tiles.

Do not write this atlas back onto `industrial_malt_house_controller`.

## Brief

- **Id** : `craft_malt_house_controller` — contrôleur unitaire 1×1 (non formé).
- **Kit hull** : cuivre `craft_casing` ; pupitre −Z.
- **Face utile** : −Z. Java Block/Item.
- **Datagen** : `CraftAssetData` blockstate + item only.
