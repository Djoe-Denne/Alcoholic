"""Normalize item/plant masters and build every supported texture resolution.

The 512x resource pack is the source of truth.  Every smaller texture is
resampled directly from that master so repeated downsizing never compounds
blur.  The 16x result is also copied over the mod's matching built-in texture.
"""

from __future__ import annotations

from collections import deque
import gc
from pathlib import Path
import time

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
PACKS = ROOT / "resourcepacks"
MASTER_PACK = PACKS / "Alcoholic-512x"
ASSET_ROOT = Path("assets/alcoholic/textures")
RESOLUTIONS = (512, 256, 128, 64, 32, 16)

ITEM_TEXTURES = (
    "barley",
    "barley_seeds",
    "beer_bucket",
    "beverage_bottle",
    "empty_bottle",
    "grape_pomace",
    "grist",
    "hop_rhizome",
    "hopped_wort_bucket",
    "hops",
    "malted_barley",
    "pruning_shears",
    "red_grape_cutting",
    "red_grape_must_bucket",
    "red_grapes",
    "red_wine_bucket",
    "spent_grain",
    "trellis_spool",
    "white_grape_cutting",
    "white_grape_must_bucket",
    "white_grapes",
    "white_wine_bucket",
    "wort_bucket",
    "wild_hops",
    "yeast",
    "young_red_wine_bucket",
    "young_white_wine_bucket",
)

PLANT_TEXTURES = (
    "barley_crop_0",
    "barley_crop_1",
    "barley_crop_2",
    "hop_bine_0",
    "hop_bine_1",
    "hop_bine_2",
    "wild_hops",
    "red_grapevine_dormant",
    "red_grapevine_establishing",
    "red_grapevine_flowering",
    "red_grapevine_green_fruit",
    "red_grapevine_harvest_ready",
    "red_grapevine_planted",
    "red_grapevine_ripening",
    "red_grapevine_vegetative",
    "end_post",
    "trellis_wire",
    "vineyard_post",
    "white_grapevine_dormant",
    "white_grapevine_establishing",
    "white_grapevine_flowering",
    "white_grapevine_green_fruit",
    "white_grapevine_harvest_ready",
    "white_grapevine_planted",
    "white_grapevine_ripening",
    "white_grapevine_vegetative",
)

# Image generation occasionally renders a checkerboard instead of real alpha.
# These masters are known to require removal of every light-neutral checker cell,
# including enclosed gaps that are not connected to the canvas edge.
SYNTHETIC_BACKGROUND_TEXTURES = {
    ("block", "wild_hops"),
    ("item", "wild_hops"),
}


def is_light_neutral(pixel: tuple[int, int, int, int]) -> bool:
    """Identify white/gray backgrounds accidentally baked by image generation."""

    red, green, blue, alpha = pixel
    return alpha > 0 and min(red, green, blue) >= 214 and max(red, green, blue) - min(
        red, green, blue
    ) <= 28


def clear_connected_light_background(image: Image.Image) -> Image.Image:
    """Clear only edge-connected light neutral pixels, preserving pale subjects."""

    rgba = image.convert("RGBA")
    pixels = rgba.load()
    width, height = rgba.size

    # Already transparent masters need no synthetic-background cleanup.
    if rgba.getchannel("A").getextrema()[0] == 0:
        return rgba

    pending: deque[tuple[int, int]] = deque()
    visited = bytearray(width * height)

    def enqueue(x: int, y: int) -> None:
        index = y * width + x
        if not visited[index] and is_light_neutral(pixels[x, y]):
            visited[index] = 1
            pending.append((x, y))

    for x in range(width):
        enqueue(x, 0)
        enqueue(x, height - 1)
    for y in range(height):
        enqueue(0, y)
        enqueue(width - 1, y)

    while pending:
        x, y = pending.popleft()
        pixels[x, y] = (0, 0, 0, 0)
        if x:
            enqueue(x - 1, y)
        if x + 1 < width:
            enqueue(x + 1, y)
        if y:
            enqueue(x, y - 1)
        if y + 1 < height:
            enqueue(x, y + 1)

    return rgba


def clear_all_light_neutral_pixels(image: Image.Image) -> Image.Image:
    """Remove synthetic white/checkerboard pixels from a known generated master."""

    rgba = image.convert("RGBA")
    pixels = rgba.load()
    width, height = rgba.size
    for y in range(height):
        for x in range(width):
            if is_light_neutral(pixels[x, y]):
                pixels[x, y] = (0, 0, 0, 0)
    return rgba


def clear_edge_alpha_noise(image: Image.Image) -> Image.Image:
    """Remove low-alpha matte residue connected to the canvas boundary."""

    rgba = image.convert("RGBA")
    pixels = rgba.load()
    width, height = rgba.size
    pending: deque[tuple[int, int]] = deque()
    visited = bytearray(width * height)

    def enqueue(x: int, y: int) -> None:
        index = y * width + x
        if not visited[index] and pixels[x, y][3] < 128:
            visited[index] = 1
            pending.append((x, y))

    for x in range(width):
        enqueue(x, 0)
        enqueue(x, height - 1)
    for y in range(height):
        enqueue(0, y)
        enqueue(width - 1, y)

    while pending:
        x, y = pending.popleft()
        pixels[x, y] = (0, 0, 0, 0)
        if x:
            enqueue(x - 1, y)
        if x + 1 < width:
            enqueue(x + 1, y)
        if y:
            enqueue(x, y - 1)
        if y + 1 < height:
            enqueue(x, y + 1)

    return rgba


def premultiplied_resize(image: Image.Image, size: int) -> Image.Image:
    """Resize RGBA without introducing light/dark fringes around cutouts."""

    rgba = image.convert("RGBA")
    alpha = rgba.getchannel("A")
    red, green, blue, _ = rgba.split()
    premultiplied = Image.merge(
        "RGB",
        tuple(Image.eval(channel, lambda value: value) for channel in (red, green, blue)),
    )

    # Pillow's RGBA resize is alpha-aware in current bundled versions. Clearing
    # RGB under fully transparent pixels prevents hidden matte colors leaking.
    cleaned = Image.new("RGBA", rgba.size, (0, 0, 0, 0))
    cleaned.paste(premultiplied, mask=alpha)
    resized = cleaned.resize((size, size), Image.Resampling.LANCZOS)
    return clear_edge_alpha_noise(resized)


def align_to_bottom(image: Image.Image) -> Image.Image:
    """Anchor a cutout plant to the ground edge of its crossed model plane."""

    rgba = image.convert("RGBA")
    bounds = rgba.getchannel("A").getbbox()
    if bounds is None:
        return rgba

    bottom_gap = rgba.height - bounds[3]
    if bottom_gap <= 0:
        return rgba

    anchored = Image.new("RGBA", rgba.size, (0, 0, 0, 0))
    anchored.alpha_composite(rgba, (0, bottom_gap))
    return anchored


def find_builtin(relative_texture: Path) -> Path:
    candidates = (
        ROOT / "minecraft-common/src/main/resources" / ASSET_ROOT / relative_texture,
        ROOT / "minecraft-common/src/generated/resources" / ASSET_ROOT / relative_texture,
    )
    for candidate in candidates:
        if candidate.exists():
            return candidate
    return ROOT / "minecraft-common/src/generated/resources" / ASSET_ROOT / relative_texture


def save_png(image: Image.Image, path: Path) -> None:
    """Write through a sibling temporary file to tolerate transient Windows locks."""

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


def build_texture(kind: str, name: str) -> None:
    relative_texture = Path(kind) / f"{name}.png"
    master_path = MASTER_PACK / ASSET_ROOT / relative_texture
    if not master_path.exists():
        raise FileNotFoundError(f"Missing 512x master: {master_path}")

    master = clear_connected_light_background(Image.open(master_path))
    if (kind, name) in SYNTHETIC_BACKGROUND_TEXTURES:
        master = clear_all_light_neutral_pixels(master)
    master = premultiplied_resize(master, 512)
    if kind == "block":
        master = align_to_bottom(master)

    for resolution in RESOLUTIONS:
        output = premultiplied_resize(master, resolution)
        if kind == "block":
            output = align_to_bottom(output)
        pack_path = PACKS / f"Alcoholic-{resolution}x" / ASSET_ROOT / relative_texture
        save_png(output, pack_path)

        if resolution == 16:
            builtin_path = find_builtin(relative_texture)
            save_png(output, builtin_path)


def main() -> None:
    for item in ITEM_TEXTURES:
        build_texture("item", item)
    for plant in PLANT_TEXTURES:
        build_texture("block", plant)
    print(
        f"Built {len(ITEM_TEXTURES)} item and {len(PLANT_TEXTURES)} plant textures "
        f"at {', '.join(map(str, RESOLUTIONS))}px; installed 16px defaults."
    )


if __name__ == "__main__":
    main()
