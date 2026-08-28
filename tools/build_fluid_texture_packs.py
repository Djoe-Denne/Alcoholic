"""Build still/flow fluid textures for every Alcoholic resource-pack resolution.

The 512x pack holds the painted still master (NxN) and flow master (2N x 2N).
Smaller packs are resampled from those masters. The 16x still (16x16) and
flow (32x32) results are also copied over the mod's built-in textures.

World fluid colors are painted into the PNGs. Java tints stay white so the
atlas is not multiplied a second time.
"""

from __future__ import annotations

import argparse
import gc
import time
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
PACKS = ROOT / "resourcepacks"
MASTER_PACK = PACKS / "Alcoholic-512x"
ASSET_ROOT = Path("assets/alcoholic/textures")
RESOLUTIONS = (512, 256, 128, 64, 32, 16)

FLUIDS = (
    "beer",
    "hopped_wort",
    "red_grape_must",
    "red_wine",
    "white_grape_must",
    "white_wine",
    "wort",
    "young_red_wine",
    "young_white_wine",
)


def save_png(image: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_name(f".{path.stem}.tmp.png")
    for attempt in range(4):
        try:
            image.save(temporary, optimize=True)
            temporary.replace(path)
            return
        except OSError:
            if attempt == 3:
                raise
            gc.collect()
            time.sleep(0.1 * (attempt + 1))


def make_seamless(image: Image.Image, blend: int | None = None) -> Image.Image:
    """Cross-fade opposite edges so the tile does not show a hard wrap seam."""

    rgb = image.convert("RGB")
    width, height = rgb.size
    if blend is None:
        blend = max(8, width // 16)
    source = rgb.load()
    output = rgb.copy()
    dest = output.load()
    half_x = width // 2
    half_y = height // 2
    for y in range(height):
        for x in range(width):
            edge_x = 1.0
            edge_y = 1.0
            if x < blend:
                edge_x = x / blend
            elif x >= width - blend:
                edge_x = (width - 1 - x) / blend
            if y < blend:
                edge_y = y / blend
            elif y >= height - blend:
                edge_y = (height - 1 - y) / blend
            weight = min(edge_x, edge_y)
            if weight >= 1.0:
                continue
            other = source[(x + half_x) % width, (y + half_y) % height]
            current = source[x, y]
            mix = 0.4 * (1.0 - weight)
            dest[x, y] = (
                int(current[0] * (1.0 - mix) + other[0] * mix),
                int(current[1] * (1.0 - mix) + other[1] * mix),
                int(current[2] * (1.0 - mix) + other[2] * mix),
            )
    return output


def resize_opaque(image: Image.Image, size: int) -> Image.Image:
    return image.convert("RGBA").resize((size, size), Image.Resampling.LANCZOS)


def import_generated_masters(source_dir: Path) -> None:
    """Normalize ImageGen still/flow squares into the 512x pack masters."""

    for name in FLUIDS:
        still_source = source_dir / f"{name}_still.png"
        flow_source = source_dir / f"{name}_flow.png"
        if not still_source.exists() or not flow_source.exists():
            raise FileNotFoundError(f"Missing generated pair for {name} in {source_dir}")

        still = make_seamless(Image.open(still_source))
        flow = make_seamless(Image.open(flow_source))
        save_png(
            resize_opaque(still, 512),
            MASTER_PACK / ASSET_ROOT / "block" / f"{name}_still.png",
        )
        save_png(
            resize_opaque(flow, 1024),
            MASTER_PACK / ASSET_ROOT / "block" / f"{name}_flow.png",
        )


def build_fluid(name: str) -> None:
    still_master_path = MASTER_PACK / ASSET_ROOT / "block" / f"{name}_still.png"
    flow_master_path = MASTER_PACK / ASSET_ROOT / "block" / f"{name}_flow.png"
    if not still_master_path.exists() or not flow_master_path.exists():
        raise FileNotFoundError(f"Missing 512x fluid masters for {name}")

    still_master = Image.open(still_master_path).convert("RGBA")
    flow_master = Image.open(flow_master_path).convert("RGBA")
    if still_master.size != (512, 512):
        still_master = resize_opaque(still_master, 512)
    if flow_master.size != (1024, 1024):
        flow_master = resize_opaque(flow_master, 1024)

    for resolution in RESOLUTIONS:
        still = resize_opaque(still_master, resolution)
        flow = resize_opaque(flow_master, resolution * 2)
        still_relative = Path("block") / f"{name}_still.png"
        flow_relative = Path("block") / f"{name}_flow.png"
        pack_root = PACKS / f"Alcoholic-{resolution}x" / ASSET_ROOT
        save_png(still, pack_root / still_relative)
        save_png(flow, pack_root / flow_relative)
        if resolution == 16:
            builtin_root = ROOT / "minecraft-common/src/main/resources" / ASSET_ROOT
            save_png(still, builtin_root / still_relative)
            save_png(flow, builtin_root / flow_relative)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--import-from",
        type=Path,
        help="Directory of generated <fluid>_still.png / <fluid>_flow.png pairs to install as 512x masters",
    )
    args = parser.parse_args()
    if args.import_from is not None:
        import_generated_masters(args.import_from.resolve())
    for name in FLUIDS:
        build_fluid(name)
    print(
        f"Built {len(FLUIDS)} fluid still/flow pairs "
        f"at {', '.join(map(str, RESOLUTIONS))}px; installed 16px defaults."
    )


if __name__ == "__main__":
    main()
