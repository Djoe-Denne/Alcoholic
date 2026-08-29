"""Native 512 face textures for the craft mega-mesh.

Each mega-block face samples a full 512×512 panel — not a 128 tile stretched.
Painted from the engine copper palette. Not an upsample of the 128 atlas.
Writes master-512 + Alcoholic-512x only.
"""

from __future__ import annotations

import hashlib
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
PACK = ROOT / "resourcepacks" / "Alcoholic-512x" / "assets" / "alcoholic" / "textures" / "block"
MASTER = ROOT / "art" / "blockbench" / "craft_malt_house" / "textures" / "master-512"
ENGINE = (
    ROOT
    / "art"
    / "blockbench"
    / "primitive_combustion_engine"
    / "textures"
    / "master-512"
    / "primitive_combustion_engine.png"
)


def luma(rgb: np.ndarray) -> np.ndarray:
    return 0.2126 * rgb[..., 0] + 0.7152 * rgb[..., 1] + 0.0722 * rgb[..., 2]


def engine_lut() -> np.ndarray:
    eng = np.array(Image.open(ENGINE).convert("RGB"), dtype=np.float32)
    stave = eng[0:128, 256:384].reshape(-1, 3)
    return stave[np.argsort(luma(stave))]


def write_png(name: str, image: Image.Image) -> None:
    MASTER.mkdir(parents=True, exist_ok=True)
    dest_m = MASTER / name
    dest_p = PACK / name
    image.save(dest_m)
    image.save(dest_p)
    digest = hashlib.sha256(dest_m.read_bytes()).hexdigest()
    print(f"{name}: {image.size} sha={digest[:16]}")


def polish(rgb: np.ndarray) -> np.ndarray:
    y = luma(rgb)
    hi = np.clip((y - 128.0) / 90.0, 0, 1)[..., None]
    lo = np.clip((95.0 - y) / 70.0, 0, 1)[..., None]
    rgb = rgb * (1 - 0.38 * hi) + np.array([255.0, 214.0, 148.0]) * (0.38 * hi)
    rgb = rgb * (1 - 0.28 * lo) + np.array([58.0, 18.0, 8.0]) * (0.28 * lo)
    return rgb


def paint_siding(lut: np.ndarray) -> Image.Image:
    rng = np.random.default_rng(512)
    h = w = 512
    t = np.clip(rng.normal(0.50, 0.055, (h, w)), 0.06, 0.94)
    kernel = np.array([0.03, 0.05, 0.08, 0.12, 0.16, 0.22, 0.16, 0.12, 0.08, 0.05, 0.03], dtype=np.float32)
    pad = np.pad(t, ((0, 0), (5, 5)), mode="edge")
    smear = sum(k * pad[:, i : i + w] for i, k in enumerate(kernel))
    fine = rng.normal(0.0, 0.038, (h, w))
    fpad = np.pad(fine, ((0, 0), (3, 3)), mode="edge")
    fk = np.array([0.08, 0.14, 0.22, 0.28, 0.22, 0.14, 0.08], dtype=np.float32)
    grain = sum(k * fpad[:, i : i + w] for i, k in enumerate(fk))
    yy = np.linspace(0.84, 0.30, h)[:, None]
    xx = np.linspace(0.07, -0.06, w)[None, :]
    ys = np.linspace(0.0, 1.0, h)
    spec = np.exp(-((ys - 0.24) ** 2) / (2 * 0.055 ** 2))[:, None]
    spec += 0.32 * np.exp(-((ys - 0.70) ** 2) / (2 * 0.09 ** 2))[:, None]
    sweep = 0.50 + 0.50 * np.sin(np.linspace(0.2, 2.85, w))
    field = smear * 0.34 + yy * 0.36 + xx + grain * 0.16 + spec * sweep[None, :] * 0.30
    field = np.clip((field - 0.12) * 1.42, 0, 1)
    rgb = lut[np.clip((field * (len(lut) - 1)).astype(np.int32), 0, len(lut) - 1)]
    rgb = polish(rgb)
    marks = np.unique(np.clip(rng.integers(40, 472, size=14), 40, 471))
    for y in marks:
        k = 0.10 + 0.06 * float(rng.random())
        rgb[y, 32:480] = rgb[y, 32:480] * (1 - k) + np.array([255.0, 214.0, 148.0]) * k
        if y + 1 < 480:
            rgb[y + 1, 32:480] = rgb[y + 1, 32:480] * (1 - k * 0.45) + np.array([210.0, 132.0, 64.0]) * (k * 0.45)
    for i in range(28):
        k = 1.0 - i / 28.0
        rgb[i, :] = rgb[i, :] * (1 - 0.42 * k) + np.array([255, 208, 132]) * (0.42 * k)
        rgb[h - 1 - i, :] = rgb[h - 1 - i, :] * (1 - 0.48 * k) + np.array([52, 16, 6]) * (0.48 * k)
        rgb[:, i] = rgb[:, i] * (1 - 0.34 * k) + np.array([236, 168, 86]) * (0.34 * k)
        rgb[:, w - 1 - i] = rgb[:, w - 1 - i] * (1 - 0.40 * k) + np.array([64, 22, 8]) * (0.40 * k)
    im = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    d = ImageDraw.Draw(im)
    d.rectangle((18, 18, 493, 493), outline=(236, 168, 82, 255), width=6)
    d.rectangle((28, 28, 483, 483), outline=(72, 26, 10, 255), width=3)
    return im


def paint_glass() -> Image.Image:
    im = Image.new("RGBA", (512, 512), (22, 96, 142, 255))
    d = ImageDraw.Draw(im, "RGBA")
    d.rectangle((0, 0, 511, 511), fill=(24, 102, 150, 255))
    d.polygon([(40, 0), (511, 280), (511, 360), (0, 80)], fill=(120, 196, 226, 255))
    d.ellipse((36, 28, 168, 148), fill=(236, 248, 255, 255))
    d.ellipse((300, 300, 480, 480), fill=(10, 42, 68, 255))
    d.rectangle((0, 0, 511, 511), outline=(8, 28, 42, 255), width=10)
    return im


def paint_window_frame() -> Image.Image:
    im = Image.new("RGBA", (512, 512), (72, 84, 96, 255))
    d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 511, 511), fill=(78, 90, 104, 255))
    d.rectangle((28, 28, 483, 483), outline=(168, 184, 198, 255), width=22)
    d.rectangle((56, 56, 455, 455), outline=(28, 34, 40, 255), width=14)
    d.rectangle((76, 76, 435, 435), outline=(64, 76, 88, 255), width=6)
    return im


def paint_controller() -> Image.Image:
    im = Image.new("RGBA", (512, 512), (168, 90, 40, 255))
    d = ImageDraw.Draw(im)
    d.rectangle((24, 24, 487, 487), fill=(176, 96, 42, 255))
    d.rectangle((48, 48, 463, 300), fill=(18, 22, 26, 255))
    d.rectangle((64, 64, 447, 284), fill=(28, 40, 48, 255))
    d.rectangle((190, 88, 322, 132), fill=(214, 140, 48, 255))
    d.polygon([(160, 132), (352, 132), (318, 196), (194, 196)], fill=(196, 118, 40, 255))
    d.rectangle((214, 196, 298, 268), fill=(236, 168, 64, 255))
    d.ellipse((230, 212, 282, 260), fill=(12, 16, 18, 255))
    d.ellipse((80, 88, 140, 148), fill=(48, 196, 78, 255))
    d.ellipse((88, 96, 116, 124), fill=(210, 255, 190, 255))
    d.ellipse((372, 88, 432, 148), fill=(220, 96, 32, 255))
    d.ellipse((380, 96, 408, 124), fill=(255, 210, 140, 255))
    d.rectangle((80, 348, 432, 392), fill=(62, 30, 14, 255))
    d.rectangle((232, 340, 312, 400), fill=(210, 132, 56, 255))
    for x in (80, 176, 272, 368):
        d.rectangle((x, 424, x + 64, 472), fill=(88, 44, 18, 255))
    return im


def paint_fluid() -> Image.Image:
    im = Image.new("RGBA", (512, 512), (48, 58, 68, 255))
    d = ImageDraw.Draw(im)
    d.ellipse((36, 36, 476, 476), fill=(78, 92, 108, 255))
    d.ellipse((72, 72, 440, 440), fill=(28, 36, 46, 255))
    d.ellipse((120, 120, 392, 392), fill=(18, 92, 148, 255))
    d.ellipse((152, 144, 360, 344), fill=(86, 188, 226, 255))
    d.ellipse((176, 160, 288, 264), fill=(210, 242, 255, 255))
    for r in range(0, 360, 30):
        rad = np.deg2rad(r)
        cx, cy = 256 + int(np.cos(rad) * 184), 256 + int(np.sin(rad) * 184)
        d.ellipse((cx - 18, cy - 18, cx + 18, cy + 18), fill=(96, 110, 124, 255))
        d.ellipse((cx - 8, cy - 12, cx + 4, cy), fill=(200, 210, 220, 255))
    return im


def paint_item() -> Image.Image:
    im = Image.new("RGBA", (512, 512), (46, 54, 62, 255))
    d = ImageDraw.Draw(im)
    d.rectangle((48, 48, 463, 463), fill=(168, 88, 36, 255))
    d.rectangle((80, 80, 431, 431), fill=(28, 22, 16, 255))
    d.polygon([(120, 100), (392, 100), (348, 200), (164, 200)], fill=(196, 118, 48, 255))
    d.rectangle((168, 196, 344, 400), fill=(18, 14, 10, 255))
    d.rectangle((184, 220, 328, 284), fill=(72, 40, 16, 255))
    d.rectangle((200, 312, 312, 384), fill=(10, 8, 6, 255))
    d.rectangle((48, 48, 463, 72), fill=(220, 150, 72, 255))
    return im


def main() -> None:
    lut = engine_lut()
    write_png("craft_siding.png", paint_siding(lut))
    write_png("craft_window_glass.png", paint_glass())
    write_png("craft_window_frame.png", paint_window_frame())
    write_png("craft_malt_house_desk.png", paint_controller())
    write_png("craft_fluid_face.png", paint_fluid())
    write_png("craft_item_face.png", paint_item())
    lines = []
    for p in sorted(MASTER.glob("craft_*.png")):
        lines.append(f"{hashlib.sha256(p.read_bytes()).hexdigest()}  {p.name}")
    (MASTER / "SHA256SUMS.txt").write_text("\n".join(lines) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
