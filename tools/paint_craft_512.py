"""Paint craft 512 masters from Alcoholic-512x tiles. No downsample."""

from __future__ import annotations

import colorsys
import hashlib
import json
import shutil
from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
PACK512 = ROOT / "resourcepacks" / "Alcoholic-512x" / "assets" / "alcoholic" / "textures" / "block"
ART = ROOT / "art" / "blockbench"

CONTROLLERS = {
    "craft_malt_house_controller": "industrial_malt_house_controller",
    "craft_mill_controller": "industrial_roller_mill_controller",
    "craft_mash_tun_controller": "industrial_mash_tun_controller",
    "craft_brewing_kettle_controller": "industrial_brewing_kettle_controller",
    "craft_vat_controller": "industrial_vat_controller",
}


def hull_names() -> set[str]:
    data = json.loads((ART / "craft_casing" / "craft_casing.bbmodel").read_text(encoding="utf-8"))
    return {el["name"] for el in data["elements"] if "from" in el}


def uv_mask(elements: list[dict], names: set[str], size: int = 512) -> np.ndarray:
    mask = np.zeros((size, size), dtype=bool)
    for el in elements:
        if el.get("name") not in names:
            continue
        for face in el.get("faces", {}).values():
            uv = face.get("uv")
            if not uv or len(uv) != 4:
                continue
            u1, v1, u2, v2 = uv
            x0, x1 = sorted((int(np.floor(min(u1, u2))), int(np.ceil(max(u1, u2)))))
            y0, y1 = sorted((int(np.floor(min(v1, v2))), int(np.ceil(max(v1, v2)))))
            mask[max(0, y0):min(size, y1), max(0, x0):min(size, x1)] = True
    return mask


def luma(rgb: np.ndarray) -> np.ndarray:
    return 0.2126 * rgb[:, :, 0] + 0.7152 * rgb[:, :, 1] + 0.0722 * rgb[:, :, 2]


def kettle_hs_lut() -> tuple[np.ndarray, np.ndarray, tuple[float, float]]:
    kettle = np.array(
        Image.open(PACK512 / "brewing_kettle.png").convert("RGBA"), dtype=np.float32
    )
    rgb, a = kettle[:, :, :3], kettle[:, :, 3]
    r, g, b = rgb[:, :, 0], rgb[:, :, 1], rgb[:, :, 2]
    copper = (a > 200) & (r > g + 15) & (r > b + 20) & (g < 190)
    L = luma(rgb)
    hs = np.zeros((256, 2), dtype=np.float32)
    counts = np.zeros(256, dtype=np.float32)
    ys, xs = np.nonzero(copper)
    for y, x in zip(ys, xs):
        rr, gg, bb = rgb[y, x] / 255.0
        h, l, s = colorsys.rgb_to_hls(float(rr), float(gg), float(bb))
        bin_i = int(np.clip(round(l * 255.0), 0, 255))
        hs[bin_i, 0] += h
        hs[bin_i, 1] += s
        counts[bin_i] += 1
    filled = counts > 0
    hs[filled] /= counts[filled, None]
    last_h, last_s = 0.07, 0.55
    for i in range(256):
        if counts[i] > 0:
            last_h, last_s = float(hs[i, 0]), float(hs[i, 1])
        else:
            hs[i] = (last_h, last_s)
    last_h, last_s = 0.07, 0.55
    for i in range(255, -1, -1):
        if counts[i] > 0:
            last_h, last_s = float(hs[i, 0]), float(hs[i, 1])
        else:
            hs[i, 0] = 0.6 * hs[i, 0] + 0.4 * last_h
            hs[i, 1] = 0.6 * hs[i, 1] + 0.4 * last_s
    copper_L = L[copper]
    return hs, counts, (float(np.percentile(copper_L, 2)), float(np.percentile(copper_L, 98)))


def colorize_copper(src: Image.Image, preserve: np.ndarray | None = None) -> Image.Image:
    lut, _, (p2, p98) = kettle_hs_lut()
    arr = np.array(src.convert("RGBA"), dtype=np.float32)
    rgb, a = arr[:, :, :3], arr[:, :, 3]
    L = luma(rgb)
    opaque = a > 0
    s2, s98 = np.percentile(L[opaque], [2, 98])
    matched = np.clip((L - s2) / (s98 - s2 + 1e-6), 0, 1)
    matched = matched * (p98 - p2) + p2
    flat = rgb.reshape(-1, 3)
    uniq, inv = np.unique(np.round(flat).astype(np.uint8), axis=0, return_inverse=True)
    mapped = np.zeros((len(uniq), 3), dtype=np.float32)
    for i, (rr, gg, bb) in enumerate(uniq):
        h, _l, s = colorsys.rgb_to_hls(rr / 255.0, gg / 255.0, bb / 255.0)
        # representative matched L for this RGB = same as source luma of the color
        src_l = 0.2126 * rr + 0.7152 * gg + 0.0722 * bb
        ml = (src_l - s2) / (s98 - s2 + 1e-6)
        ml = float(np.clip(ml, 0, 1) * (p98 - p2) + p2)
        li = int(np.clip(round(ml), 0, 255))
        ch, cs = float(lut[li, 0]), float(lut[li, 1])
        hue = (ch + 0.015 * (h - 0.55)) % 1.0
        sat = float(np.clip(cs * (0.88 + 0.25 * s), 0.15, 0.78))
        light = float(np.clip(ml / 255.0, 0.04, 0.92))
        nr, ng, nb = colorsys.hls_to_rgb(hue, light, sat)
        mapped[i] = (nr * 255.0, ng * 255.0, nb * 255.0)
    out = mapped[inv].reshape(rgb.shape)
    # Push tan toward brewing_kettle copper (less yellow, more roux).
    out[:, :, 1] *= 0.90
    out[:, :, 2] *= 0.82
    if preserve is not None:
        keep = preserve | (a == 0)
        out[keep] = rgb[keep]
    result = np.dstack([np.clip(out, 0, 255).astype(np.uint8), a.astype(np.uint8)])
    return Image.fromarray(result, "RGBA")


def write_master(craft_id: str, image: Image.Image) -> Path:
    master = ART / craft_id / "textures" / "master-512" / f"{craft_id}.png"
    master.parent.mkdir(parents=True, exist_ok=True)
    image.save(master)
    pack = PACK512 / f"{craft_id}.png"
    pack.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(master, pack)
    digest = hashlib.sha256(master.read_bytes()).hexdigest()
    (master.parent / "SHA256SUMS.txt").write_text(f"{digest}  {craft_id}.png\n", encoding="utf-8")
    print(f"{craft_id}: unique={len(set(image.getdata()))} sha={digest[:16]}")
    return master


def paint_casing() -> None:
    steel = Image.open(PACK512 / "industrial_casing.png")
    write_master("craft_casing", colorize_copper(steel))


def paint_controller(craft_id: str, industrial_id: str, hull: set[str]) -> None:
    src_bb = json.loads((ART / industrial_id / f"{industrial_id}.bbmodel").read_text(encoding="utf-8"))
    src = Image.open(ART / industrial_id / "textures" / "master-512" / f"{industrial_id}.png")
    elements = [el for el in src_bb["elements"] if "from" in el]
    extra = {el["name"] for el in elements if el["name"] not in hull}
    unique_desk = uv_mask(elements, extra) & ~uv_mask(elements, hull)
    write_master(craft_id, colorize_copper(src, unique_desk))


def main() -> None:
    hull = hull_names()
    paint_casing()
    for craft_id, industrial_id in CONTROLLERS.items():
        paint_controller(craft_id, industrial_id, hull)


if __name__ == "__main__":
    main()
