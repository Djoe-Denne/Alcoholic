"""Unique materials for the 1x1 malt-house controller pictogram.

The three grain columns were sampling the painted house/icon tile, so the
symbol read as a three-fingered hand. Each part now has its own material.
Also repaints atlas tile (0,128): mega `ctrl_icon` still samples that island.
"""

from __future__ import annotations

import hashlib
import json
import shutil
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "art" / "blockbench" / "craft_malt_house_controller"
MASTER = ART / "textures" / "master-512" / "craft_malt_house_controller.png"
GAME = (
    ROOT
    / "minecraft-common"
    / "src"
    / "main"
    / "resources"
    / "assets"
    / "alcoholic"
    / "models"
    / "block"
    / "craft_malt_house_controller.json"
)
PACKS = (16, 32, 128, 256, 512)

# 512-space UVs. Grain columns crop different kernel patches.
UV = {
    "steel_core": [268, 20, 372, 108],
    "lever_shaft": [268, 20, 372, 108],
    "grain_l": [132, 268, 168, 348],
    "grain_c": [172, 260, 208, 356],
    "grain_r": [212, 272, 248, 352],
    "floor_tray": [392, 400, 504, 504],
    "kiln_box": [264, 264, 376, 376],
    "kiln_arch": [280, 292, 360, 368],
    "flame_a": [408, 272, 456, 344],
    "flame_b": [440, 288, 488, 360],
    "pic_plate": [8, 392, 120, 504],
    "slot_well": [136, 392, 248, 504],
    "accent_line": [136, 392, 248, 504],
    "lever_knob": [264, 392, 376, 504],
}


def paste_rgb(atlas: Image.Image, xy: tuple[int, int], rgb: np.ndarray) -> None:
    im = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    atlas.paste(im, xy)


def barley_tile(seed: int = 41) -> np.ndarray:
    rng = np.random.default_rng(seed)
    bed = np.zeros((128, 128, 3), dtype=np.float32)
    yy = np.linspace(0.0, 1.0, 128)[:, None]
    bed[..., 0] = 148 + 36 * (1 - yy)
    bed[..., 1] = 88 + 22 * (1 - yy)
    bed[..., 2] = 26 + 10 * (1 - yy)
    bed += rng.normal(0, 3, bed.shape)
    im = Image.fromarray(np.clip(bed, 0, 255).astype(np.uint8), "RGB")
    d = ImageDraw.Draw(im)
    for _ in range(420):
        x = int(rng.integers(0, 120))
        y = int(rng.integers(0, 122))
        rw = int(rng.integers(5, 10))
        rh = int(rng.integers(3, 6))
        shade = float(rng.uniform(0.82, 1.14))
        c = (
            int(np.clip(198 * shade, 80, 255)),
            int(np.clip(128 * shade, 48, 220)),
            int(np.clip(42 * shade, 12, 120)),
        )
        d.ellipse((x, y, x + rw, y + rh), fill=c)
        hx = x + max(1, rw // 4)
        hy = y + max(1, rh // 5)
        d.ellipse((hx, hy, hx + max(2, rw // 3), hy + max(2, rh // 3)), fill=(
            min(255, c[0] + 28),
            min(255, c[1] + 18),
            min(255, c[2] + 8),
        ))
    return np.array(im, dtype=np.float32)


def brick_tile(seed: int = 17) -> np.ndarray:
    rng = np.random.default_rng(seed)
    im = Image.new("RGB", (128, 128), (42, 16, 8))
    d = ImageDraw.Draw(im)
    bh, bw = 14, 28
    for row, y in enumerate(range(2, 128, bh)):
        ox = 0 if row % 2 == 0 else bw // 2
        for x in range(-bw, 128, bw):
            shade = float(rng.uniform(0.78, 1.08))
            c = (
                int(np.clip(118 * shade, 30, 200)),
                int(np.clip(52 * shade, 14, 120)),
                int(np.clip(22 * shade, 6, 70)),
            )
            d.rectangle((x + ox + 1, y + 1, x + ox + bw - 2, y + bh - 2), fill=c)
            d.line((x + ox + 3, y + 3, x + ox + bw - 6, y + 3), fill=(
                min(255, c[0] + 28),
                min(255, c[1] + 14),
                min(255, c[2] + 6),
            ))
    # darker kiln mouth in the center so the arch cube reads as an opening
    d.ellipse((36, 40, 92, 104), fill=(18, 8, 4))
    d.ellipse((44, 48, 84, 96), fill=(10, 4, 2))
    return np.array(im, dtype=np.float32)


def flame_tile() -> np.ndarray:
    yy, xx = np.mgrid[0:128, 0:128]
    cx, cy = 64.0, 78.0
    dx = (xx - cx) / 28.0
    dy = (cy - yy) / 46.0
    r = np.sqrt(dx * dx + np.clip(dy, -0.2, 2.2) ** 2)
    field = np.clip(1.15 - r, 0, 1)
    field = np.power(field, 0.65)
    out = np.zeros((128, 128, 3), dtype=np.float32)
    out[..., 0] = 48 + field * 207
    out[..., 1] = 12 + field * 214
    out[..., 2] = 4 + field * 88
    tip = np.clip((70 - yy) / 70.0, 0, 1) * field
    out[..., 1] = np.clip(out[..., 1] + tip * 40, 0, 255)
    out[..., 2] = np.clip(out[..., 2] + tip * 50, 0, 255)
    return out


def plaque_tile() -> np.ndarray:
    yy = np.linspace(0.62, 0.32, 128)[:, None]
    xx = np.linspace(0.38, 0.58, 128)[None, :]
    t = np.clip(0.55 * yy + 0.45 * xx, 0, 1)
    out = np.zeros((128, 128, 3), dtype=np.float32)
    out[..., 0] = 42 + t * 90
    out[..., 1] = 16 + t * 48
    out[..., 2] = 8 + t * 20
    im = Image.fromarray(np.clip(out, 0, 255).astype(np.uint8), "RGB")
    d = ImageDraw.Draw(im)
    d.rectangle((4, 4, 123, 123), outline=(168, 92, 36), width=3)
    d.rectangle((10, 10, 117, 117), outline=(52, 20, 8), width=2)
    return np.array(im, dtype=np.float32)


def well_tile() -> np.ndarray:
    out = np.zeros((128, 128, 3), dtype=np.float32)
    out[..., 0], out[..., 1], out[..., 2] = 18, 8, 4
    return out + np.random.default_rng(5).normal(0, 1.4, out.shape)


def knob_tile() -> np.ndarray:
    yy, xx = np.mgrid[0:128, 0:128]
    r = np.sqrt(((xx - 64) / 54.0) ** 2 + ((yy - 64) / 54.0) ** 2)
    t = np.clip(1.05 - r, 0, 1)
    out = np.zeros((128, 128, 3), dtype=np.float32)
    out[..., 0] = 92 + t * 150
    out[..., 1] = 36 + t * 140
    out[..., 2] = 12 + t * 80
    return out


def tray_tile() -> np.ndarray:
    im = Image.new("RGB", (128, 128), (72, 36, 16))
    d = ImageDraw.Draw(im)
    for y in range(10, 120, 14):
        for x in range(10, 120, 14):
            d.ellipse((x, y, x + 6, y + 6), fill=(28, 12, 6))
    d.rectangle((2, 2, 125, 125), outline=(148, 78, 32), width=3)
    return np.array(im, dtype=np.float32)


def icon_tile() -> np.ndarray:
    """Painted malt-house mark for mega ctrl_icon — heaps + kiln, not a house-hand."""
    im = Image.new("RGB", (128, 128), (28, 12, 6))
    d = ImageDraw.Draw(im)
    d.rectangle((6, 6, 121, 121), outline=(118, 64, 28), width=4)
    d.rectangle((12, 12, 115, 115), fill=(36, 16, 8))
    # kiln body
    d.rectangle((44, 78, 84, 108), fill=(96, 44, 18))
    d.rectangle((50, 84, 78, 108), fill=(16, 6, 2))
    d.polygon([(58, 100), (64, 78), (70, 100)], fill=(244, 168, 36))
    d.polygon([(61, 94), (64, 82), (67, 94)], fill=(255, 226, 120))
    # three barley heaps
    for cx, top, w in ((36, 36, 16), (64, 28, 18), (92, 40, 15)):
        d.ellipse((cx - w, top + 18, cx + w, top + 46), fill=(168, 96, 28))
        d.ellipse((cx - w + 3, top + 8, cx + w - 3, top + 34), fill=(204, 132, 44))
        d.ellipse((cx - 5, top + 4, cx + 5, top + 16), fill=(228, 168, 72))
        for ox, oy in ((-6, 22), (4, 18), (-2, 30), (6, 28)):
            d.ellipse((cx + ox, top + oy, cx + ox + 5, top + oy + 3), fill=(236, 178, 80))
    d.rectangle((30, 70, 98, 78), fill=(78, 40, 16))
    return np.array(im.filter(ImageFilter.SMOOTH), dtype=np.float32)


def paint_materials(atlas: Image.Image) -> Image.Image:
    paste_rgb(atlas, (0, 128), icon_tile())
    paste_rgb(atlas, (128, 256), barley_tile())
    paste_rgb(atlas, (256, 256), brick_tile())
    paste_rgb(atlas, (384, 256), flame_tile())
    paste_rgb(atlas, (0, 384), plaque_tile())
    paste_rgb(atlas, (128, 384), well_tile())
    paste_rgb(atlas, (256, 384), knob_tile())
    paste_rgb(atlas, (384, 384), tray_tile())
    return atlas


def uv16(uv: list[int]) -> list[float]:
    return [round(v / 32.0, 2) for v in uv]


def remap_game() -> None:
    data = json.loads(GAME.read_text(encoding="utf-8"))
    for el in data["elements"]:
        uv = UV.get(el.get("name") or "")
        if not uv:
            continue
        u16 = uv16(uv)
        for face in el["faces"].values():
            face["uv"] = u16
    GAME.write_text(json.dumps(data, indent="\t") + "\n", encoding="utf-8")


def remap_bbmodel() -> None:
    path = ART / "craft_malt_house_controller.bbmodel"
    data = json.loads(path.read_text(encoding="utf-8"))
    for el in data.get("elements") or []:
        uv = UV.get(el.get("name") or "")
        if not uv:
            continue
        for face in (el.get("faces") or {}).values():
            face["uv"] = list(uv)
    path.write_text(json.dumps(data), encoding="utf-8")


def ship(master: Path) -> None:
    digest = hashlib.sha256(master.read_bytes()).hexdigest()
    (master.parent / "SHA256SUMS.txt").write_text(
        f"{digest}  craft_malt_house_controller.png\n", encoding="utf-8"
    )
    img = Image.open(master).convert("RGBA")
    for pack in PACKS:
        dest = ROOT / f"resourcepacks/Alcoholic-{pack}x/assets/alcoholic/textures/block/craft_malt_house_controller.png"
        dest.parent.mkdir(parents=True, exist_ok=True)
        if pack == 512:
            shutil.copy2(master, dest)
        else:
            img.resize((pack, pack), Image.Resampling.LANCZOS).save(dest)
    img.resize((64, 64), Image.Resampling.LANCZOS).save(
        ROOT
        / "minecraft-common/src/main/resources/assets/alcoholic/textures/block/craft_malt_house_controller.png"
    )
    print(f"pictogram tiles sha={digest[:16]}")


def main() -> None:
    atlas = paint_materials(Image.open(MASTER).convert("RGBA"))
    atlas.save(MASTER)
    remap_bbmodel()
    remap_game()
    ship(MASTER)


if __name__ == "__main__":
    main()
