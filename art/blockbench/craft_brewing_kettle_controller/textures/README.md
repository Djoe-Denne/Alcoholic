# craft_brewing_kettle_controller textures

`master-512` contains the approved 512 atlas and `SHA256SUMS.txt`.
It is the only source used to generate lower-resolution runtime textures.

Hull cubes are a copper recolor of `industrial_brewing_kettle_controller`. Desk / pictogram /
fitting cubes stay on the industrial paint (steel pupitre, lamps off).

Do not write this atlas back onto `industrial_brewing_kettle_controller`.

## Brief

- **Id** : `craft_brewing_kettle_controller` — contrôleur unitaire 1×1 (non formé).
- **Refs** : `art/blockbench/craft_brewing_kettle_controller/reference.png`.
- **Kit hull** : cuivre `craft_casing` ; pupitre −Z ; extras = slot_well, slot_lip_u, slot_lip_d, slot_lip_w, slot_lip_e, accent_line, lever_base, lever_shaft…
- **Face utile** : −Z. Java Block/Item.
- **Datagen** : `CraftAssetData` blockstate + item only.
