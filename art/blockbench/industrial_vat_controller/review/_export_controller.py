import hashlib
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(r"c:/Users/djden/source/repos/Alcoholic")
BLOCK_ID = sys.argv[1]
ART = ROOT / "art/blockbench" / BLOCK_ID
MASTER = ART / "textures/master-512" / f"{BLOCK_ID}.png"
MOD_TEX = ROOT / "minecraft-common/src/main/resources/assets/alcoholic/textures/block" / f"{BLOCK_ID}.png"

img = Image.open(MASTER).convert("RGBA")
assert img.size == (512, 512), img.size
digest = hashlib.sha256(MASTER.read_bytes()).hexdigest()
(MASTER.parent / "SHA256SUMS.txt").write_text(f"{digest}  {BLOCK_ID}.png\n", encoding="utf-8")
print("sha256", BLOCK_ID, digest)


def save(path: Path, size: int) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    if size == 512:
        img.save(path)
    else:
        img.resize((size, size), Image.Resampling.LANCZOS).save(path)
    print(size, path.name, path.stat().st_size)


for pack in (16, 32, 128, 256, 512):
    save(ROOT / f"resourcepacks/Alcoholic-{pack}x/assets/alcoholic/textures/block/{BLOCK_ID}.png", pack)
save(MOD_TEX, 64)
