"""Fit grimoire masters into GUI plates and the 128 pack. Never crop."""

from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
MASTERS = ROOT / "art" / "grimoire" / "masters"
GUI = ROOT / "minecraft-common" / "src" / "main" / "resources" / "assets" / "alcoholic" / "textures" / "gui" / "grimoire"
PACK_128 = ROOT / "resourcepacks" / "Alcoholic-128x" / "assets" / "alcoholic" / "textures" / "gui" / "grimoire"
GUI_SIZE = (100, 56)
HD_SIZE = (800, 448)
PARCHMENT = (232, 210, 168, 255)
KINDS = ("wine", "beer")


def fit(source: Image.Image, size: tuple[int, int]) -> Image.Image:
    src = source.convert("RGBA")
    width, height = size
    scale = min(width / src.width, height / src.height)
    fitted = src.resize(
        (max(1, round(src.width * scale)), max(1, round(src.height * scale))),
        Image.Resampling.LANCZOS,
    )
    canvas = Image.new("RGBA", size, PARCHMENT)
    canvas.paste(fitted, ((width - fitted.width) // 2, (height - fitted.height) // 2), fitted)
    return canvas


def export_one(kind: str, path: Path) -> None:
    with Image.open(path) as image:
        master = image.copy()
    stem = path.stem
    gui_dir = GUI / kind
    hd_dir = PACK_128 / kind
    gui_dir.mkdir(parents=True, exist_ok=True)
    hd_dir.mkdir(parents=True, exist_ok=True)
    fit(master, GUI_SIZE).save(gui_dir / f"{stem}.png")
    fit(master, HD_SIZE).save(hd_dir / f"{stem}.png")
    print(f"{kind}/{stem}.png  {master.size[0]}x{master.size[1]} -> {GUI_SIZE[0]}x{GUI_SIZE[1]} + {HD_SIZE[0]}x{HD_SIZE[1]}")


def main() -> int:
    for kind in KINDS:
        folder = MASTERS / kind
        if not folder.is_dir():
            raise SystemExit(f"Missing masters: {folder}")
        for path in sorted(folder.glob("*.png")):
            export_one(kind, path)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
