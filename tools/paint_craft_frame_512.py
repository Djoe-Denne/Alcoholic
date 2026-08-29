"""Craft frame atlas: H/V beam strips sized to the rail aspect, plus end/corner tiles."""

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

HI = np.array([226.0, 148.0, 72.0])


def luma(rgb: np.ndarray) -> np.ndarray:
    return 0.2126 * rgb[..., 0] + 0.7152 * rgb[..., 1] + 0.0722 * rgb[..., 2]


def engine_lut() -> np.ndarray:
    eng = np.array(Image.open(ENGINE).convert("RGB"), dtype=np.float32)
    lut = eng[0:128, 256:384].reshape(-1, 3)
    return lut[np.argsort(luma(lut))]


def brushed(h: int, w: int, seed: int, top: float, bot: float, axis: str = "x") -> np.ndarray:
    rng = np.random.default_rng(seed)
    t = np.clip(rng.normal(0.50, 0.055, (h, w)), 0.08, 0.92)
    kernel = np.array([0.04, 0.08, 0.14, 0.24, 0.28, 0.24, 0.14, 0.08, 0.04], dtype=np.float32)
    if axis == "x":
        pad = np.pad(t, ((0, 0), (4, 4)), mode="edge")
        smear = sum(k * pad[:, i : i + w] for i, k in enumerate(kernel))
        ramp = np.linspace(top, bot, h)[:, None]
        ys = np.linspace(0.0, 1.0, h)
        spec = 0.22 * np.exp(-((ys - 0.32) ** 2) / (2 * 0.08 ** 2))[:, None]
    else:
        pad = np.pad(t, ((4, 4), (0, 0)), mode="edge")
        smear = sum(k * pad[i : i + h, :] for i, k in enumerate(kernel))
        ramp = np.linspace(top, bot, w)[None, :]
        xs = np.linspace(0.0, 1.0, w)
        spec = 0.22 * np.exp(-((xs - 0.32) ** 2) / (2 * 0.08 ** 2))[None, :]
    return np.clip((smear * 0.42 + ramp * 0.40 + spec) * 1.18, 0, 1)


def apply_lut(field: np.ndarray, lut: np.ndarray) -> np.ndarray:
    idx = np.clip((field * (len(lut) - 1)).astype(np.int32), 0, len(lut) - 1)
    return lut[idx]


def paint_beam_h(lut: np.ndarray, darker: bool) -> Image.Image:
    h, w = 40, 512
    field = brushed(h, w, 77 if not darker else 91, 0.68 if not darker else 0.52, 0.44 if not darker else 0.32)
    rgb = apply_lut(field, lut)
    if darker:
        rgb = rgb * 0.86
    rgb[13:27, :] = rgb[13:27, :] * 0.72 + HI * 0.28
    rgb[0, :] = rgb[2, :]
    rgb[39, :] = rgb[37, :]
    im = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    d = ImageDraw.Draw(im)
    hi = (210, 128, 58, 255) if not darker else (150, 74, 32, 255)
    lo = (92, 38, 16, 255)
    d.rectangle((0, 2, 511, 4), fill=hi)
    d.rectangle((0, 35, 511, 37), fill=lo)
    for x in (48, 128, 208, 288, 368, 448):
        d.ellipse((x - 11, 8, x + 11, 32), fill=(64, 24, 10, 255))
        d.ellipse((x - 8, 11, x + 8, 29), fill=(196, 108, 48, 255))
        d.ellipse((x - 6, 11, x + 2, 20), fill=(255, 214, 150, 255))
    return im.filter(ImageFilter.SMOOTH)


def paint_beam_v(lut: np.ndarray, darker: bool) -> Image.Image:
    h, w = 512, 40
    field = brushed(h, w, 81 if not darker else 95, 0.62 if not darker else 0.48, 0.50 if not darker else 0.36, axis="y")
    rgb = apply_lut(field, lut)
    if darker:
        rgb = rgb * 0.86
    rgb[:, 13:27] = rgb[:, 13:27] * 0.72 + HI * 0.28
    rgb[:, 0] = rgb[:, 2]
    rgb[:, 39] = rgb[:, 37]
    im = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    d = ImageDraw.Draw(im)
    for y in (48, 128, 208, 288, 368, 448):
        d.ellipse((8, y - 11, 32, y + 11), fill=(64, 24, 10, 255))
        d.ellipse((11, y - 8, 29, y + 8), fill=(196, 108, 48, 255))
        d.ellipse((11, y - 6, 20, y + 2), fill=(255, 214, 150, 255))
    return im.filter(ImageFilter.SMOOTH)


def paint_tile(lut: np.ndarray, kind: str) -> Image.Image:
    field = brushed(128, 128, {"end": 11, "corner": 19, "solid": 23, "solid_d": 29}[kind], 0.64, 0.46)
    rgb = apply_lut(field, lut)
    if kind == "solid_d":
        rgb = rgb * 0.82
    if kind == "corner":
        rgb = rgb * 0.80 + HI * 0.12
    im = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    d = ImageDraw.Draw(im)
    if kind == "end":
        d.rectangle((4, 4, 123, 123), outline=(206, 120, 54, 255), width=4)
        d.rectangle((10, 10, 117, 117), outline=(96, 40, 16, 255), width=2)
        d.ellipse((48, 48, 80, 80), fill=(86, 36, 16, 255))
        d.ellipse((52, 52, 76, 76), fill=(176, 88, 38, 255))
        d.ellipse((54, 54, 66, 66), fill=(236, 176, 96, 255))
    elif kind == "corner":
        d.rectangle((6, 6, 121, 121), outline=(220, 140, 64, 255), width=5)
        d.rectangle((14, 14, 113, 113), outline=(72, 28, 10, 255), width=3)
        d.ellipse((44, 44, 84, 84), fill=(86, 36, 16, 255))
        d.ellipse((50, 50, 78, 78), fill=(186, 96, 42, 255))
        d.ellipse((54, 54, 68, 66), fill=(236, 176, 96, 255))
    else:
        d.rectangle((0, 0, 127, 3), fill=(210, 128, 58, 255))
        d.rectangle((0, 124, 127, 127), fill=(72, 28, 10, 255))
    return im.filter(ImageFilter.SMOOTH)


def write_sums(digest: str) -> None:
    sums = MASTER / "SHA256SUMS.txt"
    lines = [ln for ln in sums.read_text(encoding="utf-8").splitlines() if ln.strip()] if sums.exists() else []
    lines = [ln for ln in lines if not ln.endswith("  craft_frame.png")]
    lines.append(f"{digest}  craft_frame.png")
    sums.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> None:
    lut = engine_lut()
    fill = apply_lut(brushed(512, 512, 3, 0.58, 0.48), lut)
    atlas = Image.fromarray(np.clip(fill, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    atlas.paste(paint_beam_h(lut, False), (0, 0))
    atlas.paste(paint_beam_h(lut, True), (0, 42))
    v_h = 512 - 86
    atlas.paste(paint_beam_v(lut, False).crop((0, 0, 40, v_h)), (0, 86))
    atlas.paste(paint_beam_v(lut, True).crop((0, 0, 40, v_h)), (42, 86))
    atlas.paste(paint_tile(lut, "end"), (96, 86))
    atlas.paste(paint_tile(lut, "corner"), (228, 86))
    atlas.paste(paint_tile(lut, "solid"), (360, 86))
    atlas.paste(paint_tile(lut, "solid_d"), (96, 218))
    MASTER.mkdir(parents=True, exist_ok=True)
    dest_m = MASTER / "craft_frame.png"
    dest_p = PACK / "craft_frame.png"
    atlas.save(dest_m)
    atlas.save(dest_p)
    digest = hashlib.sha256(dest_m.read_bytes()).hexdigest()
    write_sums(digest)
    print(f"craft_frame.png sha={digest[:16]}")
    print("tiles H=[0,0,512,40] HD=[0,42,512,82] V=[0,86,40,512] VD=[42,86,82,512]")


if __name__ == "__main__":
    main()
