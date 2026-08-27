# Alcoholic texture packs

Optional texture resolutions for painted machine atlases, item icons, crops,
hop bines, and red/white grapevines.

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
still use their model-specific built-in resolution. All optional item and plant
textures are generated directly from the archived 512 x 512 masters, never
from another reduced version.

Regenerate every item/plant resolution and install the 16 x 16 defaults with:

```powershell
python tools/build_item_plant_texture_packs.py
```
