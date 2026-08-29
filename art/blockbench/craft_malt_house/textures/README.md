# Craft malt house (formed) textures

**LOCKED 2026-08-29.** Do not repaint, remesh, or remap UVs without an
explicit unlock. `master-512` is the only source.

The approved mega-mesh lives in `../craft_malt_house.bbmodel`.
1×1 runtime cubes (`craft_casing`, `craft_malt_house_controller`) copy
tiles from these masters — they do not write back here.

Authored in **3×3×3 block space** (16 units per block). The BER scales
that unit cube by 3 at the art size. Other legal cubes (4³, 5³) keep the
9-slice `craft_casing` hull and real 1×1 fittings.

## Masters

- `craft_fluid_face.png` — `c24ed06c178b075bbe63972752b151e227986ac7bf74ffe4ddfc0c9a7f810d5b`
- `craft_frame.png` — `b1bd62a0f06f2142dd563ffc1c84b0b87ec1e3e171ab12c88f5df5154ab96505`
- `craft_item_face.png` — `12565009d32bb1850f5a169dac95f246fb986c4f7bbdd2d78f1ca955b2a9a94a`
- `craft_malt_house.png` — `99e906e290967a4225371a448c00e56890482db97f398dc4bf893a5ca94a9b1c`
- `craft_malt_house_desk.png` — `05817380451e66af6dc7f4d4cc9c6fde979e3c624810a67a9ff70c5599f551e8`
- `craft_siding.png` — `b7251a798663a51a4c4e90db50ebeecc446f860b227d366a823c75b2121baeef`
- `craft_window_frame.png` — `1cb4c2e62ad25022da3e898b728748e1220b4e0d28b7c5981e0cdcff0bbefdbc`
- `craft_window_glass.png` — `2d10ce18fec0e7acebc0e7dfc72069b03e0b49398e5e6b58c1f365838c182ced`

## Brief

- **Id** : `alcoholic:craft_malt_house` — mega-mesh formé, pas un nouveau bloc.
- **Art size** : 3×3×3. Face utile −Z.
- **Kit** : `craft_siding` (un panneau / face), `craft_frame` (H/V + coins),
  vitre / cadre, pupitre, ports fluide / item.
- **Intérieur** : plateau + grain + touraille (`brewing_kettle` tiles).
