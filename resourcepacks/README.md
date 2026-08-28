# Alcoholic texture packs

Optional texture resolutions for painted machine atlases, item icons, crops,
hop bines, red/white grapevines, and world fluid still/flow tiles.

Each directory is a standalone Minecraft Java 1.19.2 resource pack. Copy the
chosen directory into the instance's `resourcepacks` directory and enable it
above the base Alcoholic resources.

- `Alcoholic-16x`: 16 x 16 lightweight textures
- `Alcoholic-32x`: 32 x 32 lightweight textures
- `Alcoholic-64x`: 64 x 64 textures
- `Alcoholic-128x`: 128 x 128 textures
- `Alcoholic-256x`: 256 x 256 textures
- `Alcoholic-512x`: archived full-resolution artwork

The mod uses 16 x 16 item and plant textures by default. Machine atlases may
still use their model-specific built-in resolution.

Four world fluids are painted and animated like vanilla water (vertical frame
strips): `beer`, `hopped_wort`, `red_grape_must`, and `white_grape_must`.
16 / 32 / 64 packs use 32 frames (`frametime` 2). The 128 pack uses 24 frames
timed to the same 64-tick loop. 256 / 512 packs use 8 frames (`frametime` 8).
Still width is the pack resolution; flow width is
twice that. Young wines, aged wines, and wort use vanilla water tiles plus a
Java tint instead of Alcoholic world PNGs.

Shader packs only treat a block as water when it is listed in that pack's
`shaders/block.properties`. Do not ship that file in these texture packs.
Complementary Reimagined r5.x uses `block.32000`, not `block.8`. Player
steps: `docs/guides/shaders.md`.

All optional item, plant, and painted-fluid textures are generated directly
from the archived 512 x 512 masters, never from another reduced version.

Regenerate every item/plant resolution and install the 16 x 16 defaults with:

```powershell
python tools/build_item_plant_texture_packs.py
```

Regenerate painted animated fluid strips (or one fluid with `--fluid beer`)
and remove leftover tinted-water world tiles with:

```powershell
python tools/build_fluid_texture_packs.py
python tools/build_fluid_texture_packs.py --clean-tinted
```
