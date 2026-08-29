# Craft malt house (formed) textures

`master-512` is the only source for the formed mega-mesh atlas.
Do not upscale a reduced atlas or overwrite the master.

Authored in **3×3×3 block space** (16 units per block). The BER scales
that unit cube by 3 at the art size. Other legal cubes (4³, 5³) keep the
9-slice `craft_casing` hull and real 1×1 fittings — the mesh is not
stretched, so windows and ports stay world-aligned.

## Brief

- **Id** : `alcoholic:craft_malt_house` — mega-mesh formé, pas un nouveau bloc.
- **Art size** : 3×3×3 (`FormedArtSize.CRAFT_MALT_HOUSE`), même taille que
  `/alcoholic debug place beer craft`.
- **Face utile** : −Z. Fittings alignés sur `IndustrialHullPattern` 3×3
  (fenêtres haut G/D, contrôleur centre, fluide milieu droit, item bas centre).
- **Intérieur** : plateau + grain + touraille, visibles dans les hublots.
- **Toit** : évent / cheminée (art only).
