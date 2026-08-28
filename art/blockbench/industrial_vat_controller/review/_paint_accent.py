import shutil
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(r"c:/Users/djden/source/repos/Alcoholic")
SRC = ROOT / "art/blockbench/industrial_vat_controller/textures/master-512/industrial_vat_controller.png"
BLOCK_ID = sys.argv[1]
TARGET = tuple(int(v) for v in sys.argv[2].split(","))
DEST = ROOT / "art/blockbench" / BLOCK_ID / "textures/master-512" / f"{BLOCK_ID}.png"

img = Image.open(SRC).convert("RGBA")
px = img.load()
tr, tg, tb = TARGET
tlum = 0.30 * tr + 0.59 * tg + 0.11 * tb


def luma(r: int, g: int, b: int) -> float:
    return 0.30 * r + 0.59 * g + 0.11 * b


for y in range(128, 256):
    for x in range(0, 128):
        r, g, b, a = px[x, y]
        if a == 0:
            continue
        scale = luma(r, g, b) / max(tlum, 1.0)
        px[x, y] = (
            max(0, min(255, round(tr * scale))),
            max(0, min(255, round(tg * scale))),
            max(0, min(255, round(tb * scale))),
            a,
        )

DEST.parent.mkdir(parents=True, exist_ok=True)
img.save(DEST)
print("wrote", DEST, DEST.stat().st_size)
