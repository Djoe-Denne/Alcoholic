"""Build animated still/flow fluid strips for Alcoholic resource packs.

Painted fluids (beer, hopped wort, grape musts) become vanilla-style vertical
frame strips. 16/32/64 use 32 frames (frametime 2). 128/256/512 use 8 frames
(frametime 8). Each frame is resized on its own before restacking.
"""

from __future__ import annotations

import argparse
import gc
import json
import math
import time
from pathlib import Path

import numpy as np
from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
PACKS = ROOT / "resourcepacks"
MASTER_PACK = PACKS / "Alcoholic-512x"
ASSET_ROOT = Path("assets/alcoholic/textures")
BUILTIN_BLOCK = ROOT / "minecraft-common/src/main/resources" / ASSET_ROOT / "block"
RESOLUTIONS = (512, 256, 128, 64, 32, 16)

ANIMATED_FLUIDS = (
    "beer",
    "hopped_wort",
    "red_grape_must",
    "white_grape_must",
)

TINTED_WATER_FLUIDS = (
    "red_wine",
    "white_wine",
    "wort",
    "young_red_wine",
    "young_white_wine",
)

TRAITS = {
    "beer": {
        "foam": True,
        "foam_color": (252, 225, 184),
        "bubbles": 7,
        "bubble_color": (255, 244, 220),
        "pulp": False,
        "scroll": 0.18,
        "flow_scroll": 0.55,
    },
    "hopped_wort": {
        "foam": False,
        "foam_color": (220, 200, 120),
        "bubbles": 5,
        "bubble_color": (236, 214, 120),
        "pulp": True,
        "pulp_color": (90, 72, 8),
        "scroll": 0.14,
        "flow_scroll": 0.48,
    },
    "red_grape_must": {
        "foam": False,
        "foam_color": (246, 184, 221),
        "bubbles": 6,
        "bubble_color": (255, 190, 210),
        "pulp": True,
        "pulp_color": (42, 2, 18),
        "scroll": 0.12,
        "flow_scroll": 0.42,
    },
    "white_grape_must": {
        "foam": False,
        "foam_color": (252, 239, 176),
        "bubbles": 6,
        "bubble_color": (255, 248, 210),
        "pulp": True,
        "pulp_color": (168, 132, 28),
        "scroll": 0.12,
        "flow_scroll": 0.42,
    },
}


def frame_count(resolution: int) -> int:
    return 8 if resolution >= 128 else 32


def frame_time(resolution: int) -> int:
    return 8 if resolution >= 128 else 2


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


def save_mcmeta(path: Path, frametime: int) -> None:
    path.write_text(
        json.dumps({"animation": {"frametime": frametime}}, indent=2) + "\n",
        encoding="utf-8",
    )


def square_master(image: Image.Image) -> Image.Image:
    rgba = image.convert("RGBA")
    side = min(rgba.width, rgba.height)
    return rgba.crop((0, 0, side, side))


def sample_master(master: np.ndarray, u: np.ndarray, v: np.ndarray) -> np.ndarray:
    height, width = master.shape[:2]
    x = np.mod(np.floor(u * width).astype(np.int32), width)
    y = np.mod(np.floor(v * height).astype(np.int32), height)
    return master[y, x]


def wrap01(value: np.ndarray) -> np.ndarray:
    return np.mod(value, 1.0)


def render_frame(
    master: np.ndarray,
    size: int,
    phase: float,
    flowing: bool,
    traits: dict,
    seed: int,
) -> Image.Image:
    yy, xx = np.mgrid[0:size, 0:size]
    u = xx / size
    v = yy / size
    scroll = traits["flow_scroll"] if flowing else traits["scroll"]
    warp_u = 0.07 * np.sin(2.0 * math.pi * (2.0 * v + phase))
    warp_v = 0.07 * np.sin(2.0 * math.pi * (2.0 * u + phase * 0.7))
    if flowing:
        warp_v += 0.05 * np.sin(2.0 * math.pi * (4.0 * v - phase))
        su = wrap01(u + warp_u + 0.04 * phase)
        sv = wrap01(v + warp_v + phase * scroll)
    else:
        su = wrap01(u + warp_u)
        sv = wrap01(v + warp_v + phase * scroll)

    pixels = sample_master(master, su, sv).astype(np.float32)

    foam_field = 0.5 + 0.5 * np.sin(2.0 * math.pi * (3.0 * su + phase * 0.25)) * np.sin(
        2.0 * math.pi * (2.0 * sv - phase * 0.18)
    )
    if traits["foam"]:
        foam = foam_field > 0.68
        cream = np.array(traits["foam_color"], dtype=np.float32)
        mix = np.clip((foam_field - 0.68) / 0.32, 0.0, 1.0)[:, :, None]
        pixels = np.where(foam[:, :, None], pixels * (1.0 - mix) + cream * mix, pixels)

    if traits["pulp"]:
        pulp_hash = np.sin((su * 47.0 + seed) * 12.9898 + (sv * 91.0) * 78.233) * 43758.5453
        pulp = np.mod(pulp_hash, 1.0) < 0.018
        pulp_color = np.array(traits["pulp_color"], dtype=np.float32)
        pixels = np.where(pulp[:, :, None], pixels * 0.35 + pulp_color * 0.65, pixels)

    rng = np.random.default_rng(seed)
    bubble_color = np.array(traits["bubble_color"], dtype=np.float32)
    for index in range(int(traits["bubbles"])):
        bx = float(rng.random())
        by0 = float(rng.random())
        radius = 0.018 + 0.012 * float(rng.random())
        if size <= 16:
            radius = max(radius, 0.06)
        rise = (by0 - phase * (0.55 + 0.2 * index / max(1, traits["bubbles"]))) % 1.0
        dx = np.minimum(np.abs(su - bx), 1.0 - np.abs(su - bx))
        dy = np.minimum(np.abs(sv - rise), 1.0 - np.abs(sv - rise))
        dist = np.sqrt(dx * dx + dy * dy)
        ring = (dist < radius) & (dist > radius * 0.45)
        core = dist < radius * 0.28
        pixels = np.where(ring[:, :, None], pixels * 0.25 + bubble_color * 0.75, pixels)
        pixels = np.where(core[:, :, None], np.minimum(pixels + 40.0, 255.0), pixels)

    return Image.fromarray(np.clip(pixels, 0, 255).astype(np.uint8), "RGB").convert("RGBA")


def stack_frames(frames: list[Image.Image]) -> Image.Image:
    width, height = frames[0].size
    strip = Image.new("RGBA", (width, height * len(frames)))
    for index, frame in enumerate(frames):
        strip.paste(frame, (0, index * height))
    return strip


def write_strip(strip: Image.Image, path: Path, frametime: int) -> None:
    save_png(strip, path)
    save_mcmeta(path.with_name(path.name + ".mcmeta"), frametime)


def build_fluid(name: str) -> None:
    traits = TRAITS[name]
    still_path = MASTER_PACK / ASSET_ROOT / "block" / f"{name}_still.png"
    flow_path = MASTER_PACK / ASSET_ROOT / "block" / f"{name}_flow.png"
    if not still_path.exists() or not flow_path.exists():
        raise FileNotFoundError(f"Missing 512x fluid masters for {name}")

    still_master = np.asarray(square_master(Image.open(still_path)).convert("RGB"), dtype=np.uint8)
    flow_master = np.asarray(square_master(Image.open(flow_path)).convert("RGB"), dtype=np.uint8)
    seed = abs(hash(name)) % 10_000

    for resolution in RESOLUTIONS:
        frames = frame_count(resolution)
        frametime = frame_time(resolution)
        still_frames = [
            render_frame(still_master, resolution, index / frames, False, traits, seed)
            for index in range(frames)
        ]
        flow_frames = [
            render_frame(flow_master, resolution * 2, index / frames, True, traits, seed + 17)
            for index in range(frames)
        ]
        still_strip = stack_frames(still_frames)
        flow_strip = stack_frames(flow_frames)
        pack_root = PACKS / f"Alcoholic-{resolution}x" / ASSET_ROOT / "block"
        write_strip(still_strip, pack_root / f"{name}_still.png", frametime)
        write_strip(flow_strip, pack_root / f"{name}_flow.png", frametime)
        if resolution == 16:
            write_strip(still_strip, BUILTIN_BLOCK / f"{name}_still.png", frametime)
            write_strip(flow_strip, BUILTIN_BLOCK / f"{name}_flow.png", frametime)


def clean_tinted_water() -> None:
    names = [f"{name}_{kind}" for name in TINTED_WATER_FLUIDS for kind in ("still", "flow")]
    roots = [PACKS / f"Alcoholic-{resolution}x" / ASSET_ROOT / "block" for resolution in RESOLUTIONS]
    roots.append(BUILTIN_BLOCK)
    for root in roots:
        for name in names:
            for suffix in (".png", ".png.mcmeta"):
                path = root / f"{name}{suffix}"
                if path.exists():
                    path.unlink()


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--fluid",
        choices=ANIMATED_FLUIDS,
        help="Build a single painted animated fluid",
    )
    parser.add_argument(
        "--clean-tinted",
        action="store_true",
        help="Remove world still/flow textures for tinted vanilla-water fluids",
    )
    args = parser.parse_args()
    if args.clean_tinted:
        clean_tinted_water()
        print(f"Removed world textures for {len(TINTED_WATER_FLUIDS)} tinted-water fluids.")
        return
    names = (args.fluid,) if args.fluid else ANIMATED_FLUIDS
    for name in names:
        build_fluid(name)
        print(f"Built animated still/flow strips for {name}.")


if __name__ == "__main__":
    main()
