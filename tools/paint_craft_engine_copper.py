"""Rebuild craft copper 512 from the primitive combustion engine stave tile.

Keeps industrial_casing rivet/inset layout so existing UVs still work.
Does not downsample. Writes master-512 + Alcoholic-512x only.
"""

from __future__ import annotations

import hashlib
import shutil
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
PACK512 = ROOT / "resourcepacks" / "Alcoholic-512x" / "assets" / "alcoholic" / "textures" / "block"
ART = ROOT / "art" / "blockbench"
ENGINE = (
    ART
    / "primitive_combustion_engine"
    / "textures"
    / "master-512"
    / "primitive_combustion_engine.png"
)


def luma(rgb: np.ndarray) -> np.ndarray:
    return 0.2126 * rgb[..., 0] + 0.7152 * rgb[..., 1] + 0.0722 * rgb[..., 2]


def tile(im: Image.Image, x: int, y: int, s: int = 128) -> np.ndarray:
    return np.array(im.crop((x, y, x + s, y + s)).convert("RGBA"), dtype=np.float32)


def engine_copper_lut(engine: Image.Image) -> np.ndarray:
    stave = tile(engine, 256, 0)[:, :, :3].reshape(-1, 3)
    return stave[np.argsort(luma(stave))]


def recolor_structure(src_rgba: np.ndarray, lut: np.ndarray, darker: float = 1.18) -> np.ndarray:
    rgb = src_rgba[:, :, :3]
    a = src_rgba[:, :, 3]
    L = luma(rgb)
    opaque = a > 0
    p2, p98 = np.percentile(L[opaque], [2, 98]) if opaque.any() else (0.0, 255.0)
    t = np.clip((L - p2) / (p98 - p2 + 1e-6), 0, 1) ** darker
    idx = np.clip((t * (len(lut) - 1)).astype(np.int32), 0, len(lut) - 1)
    out = lut[idx]
    return np.dstack([out, a])


def blend_staves(recolored: np.ndarray, staves: np.ndarray, structure: np.ndarray, amount: float) -> np.ndarray:
    L = luma(structure[:, :, :3])
    field = L > np.percentile(L, 22)
    mix = recolored.copy()
    mix[field, :3] = (1.0 - amount) * recolored[field, :3] + amount * staves[field, :3]
    return mix


def paint_rivet(draw: ImageDraw.ImageDraw, cx: int, cy: int, r: int = 7) -> None:
    draw.ellipse((cx - r, cy - r + 1, cx + r, cy + r + 1), fill=(62, 22, 10, 255))
    draw.ellipse((cx - r, cy - r, cx + r, cy + r), fill=(118, 52, 24, 255))
    draw.ellipse((cx - r + 2, cy - r + 2, cx + r - 3, cy + r - 3), fill=(176, 86, 38, 255))
    draw.ellipse((cx - r + 3, cy - r + 2, cx - 1, cy - 1), fill=(228, 168, 92, 255))


def paint_bracket(base: np.ndarray) -> np.ndarray:
    im = Image.fromarray(np.clip(base, 0, 255).astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(im)
    bar = (132, 58, 26, 255)
    hi = (198, 108, 48, 255)
    sh = (72, 26, 12, 255)
    d.rectangle((10, 10, 34, 118), fill=bar)
    d.rectangle((10, 94, 118, 118), fill=bar)
    d.rectangle((12, 12, 18, 116), fill=hi)
    d.rectangle((12, 96, 116, 102), fill=hi)
    d.rectangle((30, 14, 34, 112), fill=sh)
    d.rectangle((14, 114, 116, 118), fill=sh)
    paint_rivet(d, 22, 22)
    paint_rivet(d, 22, 64)
    paint_rivet(d, 22, 106)
    paint_rivet(d, 64, 106)
    paint_rivet(d, 106, 106)
    return np.array(im, dtype=np.float32)


def paint_strap(base: np.ndarray) -> np.ndarray:
    im = Image.fromarray(np.clip(base, 0, 255).astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(im)
    d.rectangle((6, 44, 122, 84), fill=(128, 56, 24, 255))
    d.rectangle((8, 46, 120, 54), fill=(204, 118, 52, 255))
    d.rectangle((8, 74, 120, 82), fill=(78, 28, 12, 255))
    d.rectangle((8, 58, 120, 64), fill=(226, 162, 78, 255))
    paint_rivet(d, 24, 64, 8)
    paint_rivet(d, 64, 64, 8)
    paint_rivet(d, 104, 64, 8)
    return np.array(im, dtype=np.float32)


def paint_plaque(base: np.ndarray) -> np.ndarray:
    im = Image.fromarray(np.clip(base * 0.82, 0, 255).astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(im)
    d.rectangle((16, 36, 112, 92), fill=(86, 34, 14, 255))
    d.rectangle((20, 40, 108, 88), fill=(168, 82, 34, 255))
    d.rectangle((24, 44, 104, 84), fill=(58, 28, 14, 255))
    d.rectangle((28, 50, 100, 58), fill=(214, 142, 64, 255))
    d.rectangle((36, 64, 92, 70), fill=(196, 118, 48, 255))
    paint_rivet(d, 26, 42, 5)
    paint_rivet(d, 102, 42, 5)
    paint_rivet(d, 26, 86, 5)
    paint_rivet(d, 102, 86, 5)
    return np.array(im, dtype=np.float32)


def assemble_casing(engine: Image.Image, steel: Image.Image) -> Image.Image:
    lut = engine_copper_lut(engine)
    staves = tile(engine, 256, 0)
    out = Image.new("RGBA", (512, 512), (36, 14, 8, 255))
    variants = [
        ((0, 0), 0.42, 1.12),
        ((128, 0), 0.28, 1.08),
        ((256, 0), 0.22, 1.22),
        ((384, 0), 0.34, 1.05),
    ]
    for (x, y), blend, darker in variants:
        src = tile(steel, x, y)
        reco = recolor_structure(src, lut, darker=darker)
        mixed = blend_staves(reco, staves, src, blend)
        out.paste(Image.fromarray(np.clip(mixed, 0, 255).astype(np.uint8), "RGBA"), (x, y))

    out.paste(Image.fromarray(np.clip(staves, 0, 255).astype(np.uint8), "RGBA"), (0, 128))
    dark_staves = staves.copy()
    dark_staves[:, :, :3] *= 0.72
    out.paste(Image.fromarray(np.clip(dark_staves, 0, 255).astype(np.uint8), "RGBA"), (128, 128))
    out.paste(
        Image.fromarray(np.clip(paint_bracket(staves), 0, 255).astype(np.uint8), "RGBA"),
        (256, 128),
    )
    out.paste(
        Image.fromarray(np.clip(paint_strap(staves), 0, 255).astype(np.uint8), "RGBA"),
        (384, 128),
    )
    out.paste(
        Image.fromarray(np.clip(paint_plaque(staves), 0, 255).astype(np.uint8), "RGBA"),
        (0, 256),
    )
    return out.filter(ImageFilter.SMOOTH)


def write_master(craft_id: str, image: Image.Image) -> None:
    master = ART / craft_id / "textures" / "master-512" / f"{craft_id}.png"
    master.parent.mkdir(parents=True, exist_ok=True)
    image.save(master)
    shutil.copy2(master, PACK512 / f"{craft_id}.png")
    digest = hashlib.sha256(master.read_bytes()).hexdigest()
    (master.parent / "SHA256SUMS.txt").write_text(f"{digest}  {craft_id}.png\n", encoding="utf-8")
    uniq = len(np.unique(np.array(image.convert("RGB")).reshape(-1, 3), axis=0))
    print(f"{craft_id}: unique={uniq} sha={digest[:16]}")


def paint_controller(engine: Image.Image, steel: Image.Image) -> None:
    craft_id = "craft_malt_house_controller"
    current = Image.open(PACK512 / f"{craft_id}.png").convert("RGBA")
    casing = assemble_casing(engine, steel)
    # Hull tiles 0/2/3 + new vocabulary. Keep desk tile (128,0) and pictogram (0,128) from current.
    desk = current.crop((128, 0, 256, 128))
    icon = current.crop((0, 128, 128, 256))
    out = casing.copy()
    out.paste(desk, (128, 0))
    out.paste(icon, (0, 128))
    write_master(craft_id, out)


def main() -> None:
    engine = Image.open(ENGINE)
    steel = Image.open(PACK512 / "industrial_casing.png")
    write_master("craft_casing", assemble_casing(engine, steel))
    paint_controller(engine, steel)


if __name__ == "__main__":
    main()
