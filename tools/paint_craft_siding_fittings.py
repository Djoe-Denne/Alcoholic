"""Single-panel copper siding + richer fitting tiles.

Craft casing: one rivet-free siding tile for dynamic faces.
Window/port: paint ONLY unused atlas cells so industrial UVs stay intact.
Controller: enrich desk + pictogram (craft-only).
Writes master-512 + Alcoholic-512x. No downsample.
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


def crop(im: Image.Image, x: int, y: int, s: int = 128) -> np.ndarray:
    return np.array(im.crop((x, y, x + s, y + s)).convert("RGBA"), dtype=np.float32)


def engine_lut(engine: Image.Image) -> np.ndarray:
    stave = crop(engine, 256, 0)[:, :, :3].reshape(-1, 3)
    return stave[np.argsort(luma(stave))]


def recolor(src: np.ndarray, lut: np.ndarray, darker: float = 1.08) -> np.ndarray:
    rgb, a = src[:, :, :3], src[:, :, 3]
    L = luma(rgb)
    opaque = a > 0
    p2, p98 = np.percentile(L[opaque], [2, 98]) if opaque.any() else (0.0, 255.0)
    t = np.clip((L - p2) / (p98 - p2 + 1e-6), 0, 1) ** darker
    idx = np.clip((t * (len(lut) - 1)).astype(np.int32), 0, len(lut) - 1)
    return np.dstack([lut[idx], a])


def flatten(arr: np.ndarray) -> np.ndarray:
    out = np.clip(arr, 0, 255).astype(np.float32)
    out[:, :, 3] = 255
    return out


def paste_tile(sheet: Image.Image, arr: np.ndarray, x: int, y: int) -> None:
    tile = Image.fromarray(flatten(arr).astype(np.uint8), "RGBA")
    sheet.paste(tile, (x, y))


def write_atlas(rel_id: str, image: Image.Image) -> None:
    master = ART / rel_id / "textures" / "master-512" / f"{rel_id}.png"
    master.parent.mkdir(parents=True, exist_ok=True)
    image.save(master)
    shutil.copy2(master, PACK512 / f"{rel_id}.png")
    digest = hashlib.sha256(master.read_bytes()).hexdigest()
    (master.parent / "SHA256SUMS.txt").write_text(f"{digest}  {rel_id}.png\n", encoding="utf-8")
    uniq = len(np.unique(np.array(image.convert("RGB")).reshape(-1, 3), axis=0))
    print(f"{rel_id}: unique={uniq} sha={digest[:16]}")


def paint_siding(steel: Image.Image, lut: np.ndarray) -> np.ndarray:
    """One even plate for dynamic siding: no rivets, no 3x3, no giant specular."""
    inset = recolor(crop(steel, 128, 0), lut, darker=1.02)
    rgb = inset[:, :, :3]
    # flatten the industrial inset's dark well so one face stays one tone
    mid = np.median(rgb.reshape(-1, 3), axis=0)
    field = luma(rgb)
    rgb = rgb * 0.35 + mid * 0.65
    yy = np.linspace(1.08, 0.90, rgb.shape[0])[:, None, None]
    rgb = rgb * yy
    inset[:, :, :3] = rgb
    im = Image.fromarray(np.clip(inset, 0, 255).astype(np.uint8), "RGBA")
    im = im.filter(ImageFilter.SMOOTH)
    d = ImageDraw.Draw(im, "RGBA")
    d.rectangle((6, 6, 121, 121), outline=(196, 112, 52, 255), width=4)
    d.rectangle((10, 10, 117, 117), outline=(118, 52, 22, 255), width=2)
    arr = np.array(im, dtype=np.float32)
    arr[:, :, 3] = 255
    return arr


def paint_rich_glass() -> np.ndarray:
    im = Image.new("RGBA", (128, 128), (18, 78, 118, 255))
    d = ImageDraw.Draw(im, "RGBA")
    d.rectangle((0, 0, 127, 127), fill=(22, 96, 142, 255))
    d.ellipse((8, 6, 70, 62), fill=(150, 214, 236, 90))
    d.ellipse((14, 10, 42, 36), fill=(240, 252, 255, 200))
    d.ellipse((78, 70, 122, 118), fill=(8, 36, 58, 90))
    # mullion ghosts
    d.rectangle((60, 8, 67, 120), fill=(10, 28, 40, 70))
    d.rectangle((8, 60, 120, 67), fill=(10, 28, 40, 70))
    # grain pile in the lower third
    d.ellipse((28, 78, 100, 124), fill=(168, 104, 36, 95))
    d.ellipse((40, 88, 88, 122), fill=(196, 132, 48, 110))
    for x in (22, 36, 54, 70, 88, 102):
        d.line((x, 18, x - 3, 78), fill=(190, 230, 245, 35), width=1)
    d.rectangle((0, 0, 127, 127), outline=(8, 22, 34, 255), width=4)
    arr = np.array(im.convert("RGBA"), dtype=np.float32)
    arr[:, :, 3] = 255
    return arr


def paint_rich_window_frame(steel_inset: np.ndarray) -> np.ndarray:
    im = Image.fromarray(np.clip(steel_inset, 0, 255).astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(im, "RGBA")
    d.rectangle((10, 10, 117, 117), outline=(168, 184, 196, 255), width=6)
    d.rectangle((18, 18, 109, 109), outline=(28, 34, 40, 255), width=4)
    d.rectangle((22, 22, 105, 105), outline=(72, 88, 102, 255), width=2)
    # rebate for glass
    d.rectangle((26, 26, 101, 101), fill=(36, 48, 58, 40))
    return np.array(im, dtype=np.float32)


def paint_rich_fluid() -> np.ndarray:
    im = Image.new("RGBA", (128, 128), (48, 58, 68, 255))
    d = ImageDraw.Draw(im, "RGBA")
    d.ellipse((10, 10, 118, 118), fill=(78, 92, 108, 255))
    d.ellipse((18, 18, 110, 110), fill=(28, 36, 46, 255))
    d.ellipse((30, 30, 98, 98), fill=(18, 92, 148, 255))
    d.ellipse((38, 36, 90, 86), fill=(86, 188, 226, 255))
    d.ellipse((44, 40, 72, 66), fill=(210, 242, 255, 180))
    d.ellipse((70, 72, 96, 96), fill=(8, 40, 70, 90))
    for ang, r in enumerate((20, 50, 80, 110, 140, 170, 200, 230, 260, 290, 320, 350)):
        rad = np.deg2rad(r)
        cx, cy = 64 + int(np.cos(rad) * 46), 64 + int(np.sin(rad) * 46)
        d.ellipse((cx - 5, cy - 5, cx + 5, cy + 5), fill=(96, 110, 124, 255))
        d.ellipse((cx - 2, cy - 3, cx + 1, cy), fill=(200, 210, 220, 255))
    return np.array(im, dtype=np.float32)


def paint_rich_item() -> np.ndarray:
    im = Image.new("RGBA", (128, 128), (46, 54, 62, 255))
    d = ImageDraw.Draw(im, "RGBA")
    d.rectangle((14, 14, 113, 113), fill=(168, 88, 36, 255))
    d.rectangle((22, 22, 105, 105), fill=(28, 22, 16, 255))
    d.polygon([(32, 28), (96, 28), (84, 52), (44, 52)], fill=(196, 118, 48, 255))
    d.rectangle((44, 50, 84, 100), fill=(18, 14, 10, 255))
    d.rectangle((48, 56, 80, 72), fill=(72, 40, 16, 255))
    d.rectangle((52, 78, 76, 96), fill=(10, 8, 6, 255))
    d.rectangle((14, 14, 113, 20), fill=(220, 150, 72, 255))
    return np.array(im, dtype=np.float32)


def paint_rich_port_body(steel_inset: np.ndarray) -> np.ndarray:
    im = Image.fromarray(np.clip(steel_inset, 0, 255).astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(im, "RGBA")
    d.rectangle((12, 12, 115, 115), outline=(150, 164, 176, 255), width=5)
    d.rectangle((20, 20, 107, 107), outline=(24, 28, 34, 255), width=3)
    return np.array(im, dtype=np.float32)


def paint_controller_desk() -> np.ndarray:
    im = Image.new("RGBA", (128, 128), (148, 78, 36, 255))
    d = ImageDraw.Draw(im, "RGBA")
    d.rectangle((6, 6, 121, 121), fill=(168, 90, 40, 255))
    d.rectangle((14, 14, 113, 78), fill=(18, 22, 26, 255))
    d.rectangle((18, 18, 109, 74), fill=(28, 40, 48, 255))
    # amber malt-house pictogram
    d.rectangle((48, 24, 80, 36), fill=(214, 140, 48, 255))
    d.polygon([(40, 36), (88, 36), (78, 52), (50, 52)], fill=(196, 118, 40, 255))
    d.rectangle((54, 52, 74, 68), fill=(236, 168, 64, 255))
    d.ellipse((58, 56, 70, 66), fill=(12, 16, 18, 255))
    # lamps
    d.ellipse((22, 24, 36, 38), fill=(48, 196, 78, 255))
    d.ellipse((24, 26, 30, 32), fill=(210, 255, 190, 255))
    d.ellipse((92, 24, 106, 38), fill=(220, 96, 32, 255))
    d.ellipse((94, 26, 100, 32), fill=(255, 210, 140, 255))
    # slider + keys
    d.rectangle((22, 88, 106, 98), fill=(62, 30, 14, 255))
    d.rectangle((58, 86, 78, 100), fill=(210, 132, 56, 255))
    d.rectangle((22, 106, 38, 118), fill=(88, 44, 18, 255))
    d.rectangle((46, 106, 62, 118), fill=(88, 44, 18, 255))
    d.rectangle((70, 106, 86, 118), fill=(88, 44, 18, 255))
    d.rectangle((94, 106, 110, 118), fill=(88, 44, 18, 255))
    return np.array(im, dtype=np.float32)


def paint_controller_icon() -> np.ndarray:
    im = Image.new("RGBA", (128, 128), (128, 64, 28, 255))
    d = ImageDraw.Draw(im, "RGBA")
    d.rectangle((16, 20, 112, 108), fill=(40, 22, 12, 255))
    d.polygon([(28, 48), (64, 22), (100, 48)], fill=(206, 124, 46, 255))
    d.rectangle((36, 48, 92, 100), fill=(176, 92, 36, 255))
    d.rectangle((52, 64, 76, 100), fill=(28, 16, 8, 255))
    d.ellipse((56, 70, 72, 86), fill=(236, 168, 64, 255))
    return np.array(im, dtype=np.float32)


def main() -> None:
    engine = Image.open(ENGINE)
    steel = Image.open(PACK512 / "industrial_casing.png")
    lut = engine_lut(engine)
    siding = paint_siding(steel, lut)

    casing = Image.open(PACK512 / "craft_casing.png").convert("RGBA")
    paste_tile(casing, siding, 128, 256)
    write_atlas("craft_casing", casing)

    ctrl = Image.open(PACK512 / "craft_malt_house_controller.png").convert("RGBA")
    paste_tile(ctrl, paint_controller_desk(), 128, 0)
    paste_tile(ctrl, paint_controller_icon(), 0, 128)
    write_atlas("craft_malt_house_controller", ctrl)

    win = Image.open(PACK512 / "machine_window.png").convert("RGBA")
    paste_tile(win, paint_rich_glass(), 256, 128)
    paste_tile(win, paint_rich_window_frame(crop(win, 128, 0)), 384, 128)
    write_atlas("machine_window", win)

    fluid = Image.open(PACK512 / "fluid_port.png").convert("RGBA")
    paste_tile(fluid, paint_rich_fluid(), 256, 128)
    paste_tile(fluid, paint_rich_port_body(crop(fluid, 128, 0)), 384, 128)
    write_atlas("fluid_port", fluid)

    item = Image.open(PACK512 / "item_port.png").convert("RGBA")
    paste_tile(item, paint_rich_item(), 256, 128)
    paste_tile(item, paint_rich_port_body(crop(item, 128, 0)), 384, 128)
    write_atlas("item_port", item)


if __name__ == "__main__":
    main()
