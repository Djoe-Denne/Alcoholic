# Malt mill texture exports

`master-512/malt_mill.png` is the approved Blockbench source atlas.

The in-mod default is the 32 x 32 atlas. Optional 64 x 64, 128 x 128,
256 x 256 and 512 x 512 variants live in the matching standalone resource
packs. Every reduced atlas is derived directly from the 512 x 512 master with
nearest-neighbour sampling; reduced atlases must never be used as an upscale
source.

After exporting the open Blockbench project to
`export/malt_mill.blockbench.json`, run `tools/export-malt-mill.ps1` from the
repository. The script strips newer Blockbench-only metadata, validates the
approved static geometry, installs the 32 x 32 default and copies the optional
resolution variants to their resource packs.
