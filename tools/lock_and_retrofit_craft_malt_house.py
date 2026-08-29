"""Lock craft_malt_house masters and retrofit their copper onto 1x1 craft blocks.

Does not change 1x1 geometry or UVs. Shared industrial fittings stay industrial.
"""

from __future__ import annotations

import base64
import hashlib
import json
import shutil
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

import sys

sys.path.insert(0, str(Path(__file__).resolve().parent))
from export_formed_meshes import convert_model

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "art" / "blockbench"
MEGA = ART / "craft_malt_house"
MASTER = MEGA / "textures" / "master-512"
PACKS = (16, 32, 128, 256, 512)
MOD_TEX = ROOT / "minecraft-common" / "src" / "main" / "resources" / "assets" / "alcoholic" / "textures" / "block"
FORMED = (
    ROOT
    / "minecraft-common"
    / "src"
    / "main"
    / "resources"
    / "assets"
    / "alcoholic"
    / "models"
    / "block"
    / "formed"
)
KEEP_TEX = {
    "craft_siding.png",
    "craft_frame.png",
    "craft_window_glass.png",
    "craft_window_frame.png",
    "craft_malt_house_desk.png",
    "craft_malt_house_controller.png",
    "craft_fluid_face.png",
    "craft_item_face.png",
    "brewing_kettle.png",
}
MEGA_RUNTIME = (
    "craft_siding",
    "craft_frame",
    "craft_window_glass",
    "craft_window_frame",
    "craft_malt_house_desk",
    "craft_fluid_face",
    "craft_item_face",
)
CONTROLLERS = (
    "craft_malt_house_controller",
    "craft_mill_controller",
    "craft_mash_tun_controller",
    "craft_brewing_kettle_controller",
    "craft_vat_controller",
)
# Unique desk / icon tiles on the malt-house 1x1 atlas — do not overwrite.
MALT_KEEP = (
    (128, 0),
    (0, 128),
    (128, 256),
    (256, 256),
    (384, 256),
    (0, 384),
    (128, 384),
    (256, 384),
    (384, 384),
)


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_sums(folder: Path) -> dict[str, str]:
    lines = []
    digest = {}
    for path in sorted(folder.glob("*.png")):
        digest[path.name] = sha256(path)
        lines.append(f"{digest[path.name]}  {path.name}")
    (folder / "SHA256SUMS.txt").write_text("\n".join(lines) + "\n", encoding="utf-8")
    return digest


def downsample(src: Path, dest: Path, size: int) -> None:
    dest.parent.mkdir(parents=True, exist_ok=True)
    if size == 512:
        shutil.copy2(src, dest)
        return
    Image.open(src).convert("RGBA").resize((size, size), Image.Resampling.LANCZOS).save(dest)


def ship_png(master: Path, block_id: str) -> None:
    for pack in PACKS:
        downsample(
            master,
            ROOT / f"resourcepacks/Alcoholic-{pack}x/assets/alcoholic/textures/block/{block_id}.png",
            pack,
        )
    downsample(master, MOD_TEX / f"{block_id}.png", 64)


def resolve_tex(cube: str, side: str, rules: dict[str, str]) -> str:
    return rules.get(f"{cube}.{side}") or rules.get(cube) or "craft_frame.png"


def lock_bbmodel() -> None:
    bb_path = MEGA / "craft_malt_house.bbmodel"
    data = json.loads(bb_path.read_text(encoding="utf-8"))
    rules = json.loads((MEGA / "textures" / "face_lock.json").read_text(encoding="utf-8"))
    textures = [t for t in data.get("textures") or [] if t.get("name") in KEEP_TEX]
    for index, tex in enumerate(textures):
        tex["id"] = str(index)
    id_by_name = {t["name"]: str(t["id"]) for t in textures}
    for tex in textures:
        name = tex["name"]
        src = MASTER / name
        if not src.exists() and name == "brewing_kettle.png":
            src = (
                ART
                / "brewing_kettle"
                / "textures"
                / "master-512"
                / "brewing_kettle.png"
            )
        if src.exists():
            tex["path"] = str(src).replace("\\", "/")
            tex["relative_path"] = f"textures/master-512/{name}"
            tex["saved"] = True
            tex["internal"] = False
    missing = []
    for el in data.get("elements") or []:
        cube = el.get("name") or ""
        for side, face in (el.get("faces") or {}).items():
            name = resolve_tex(cube, side, rules)
            tid = id_by_name.get(name)
            if tid is None:
                missing.append((cube, side, name))
                continue
            face["texture"] = tid
    if missing:
        raise SystemExit(f"unmapped textures: {missing[:8]}")
    data["textures"] = textures
    bb_path.write_text(json.dumps(data), encoding="utf-8")
    print(f"locked bbmodel cubes={len(data.get('elements') or [])} textures={len(textures)}")


def export_formed() -> None:
    model = convert_model("craft_malt_house")
    dest = FORMED / "craft_malt_house.json"
    dest.write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")
    print(f"formed export elements={len(model['elements'])} textures={len(model['textures'])}")


def paint_rivet(draw: ImageDraw.ImageDraw, cx: int, cy: int, r: int = 8) -> None:
    draw.ellipse((cx - r, cy - r + 1, cx + r, cy + r + 1), fill=(62, 22, 10, 255))
    draw.ellipse((cx - r, cy - r, cx + r, cy + r), fill=(118, 52, 24, 255))
    draw.ellipse((cx - r + 2, cy - r + 2, cx + r - 3, cy + r - 3), fill=(176, 86, 38, 255))
    draw.ellipse((cx - r + 3, cy - r + 2, cx - 1, cy - 1), fill=(228, 168, 92, 255))


def tile128(im: Image.Image, x: int, y: int) -> Image.Image:
    return im.crop((x, y, x + 128, y + 128)).convert("RGBA")


def scale128(im: Image.Image) -> Image.Image:
    return im.convert("RGBA").resize((128, 128), Image.Resampling.LANCZOS)


def darker(im: Image.Image, k: float) -> Image.Image:
    arr = np.array(im.convert("RGBA"), dtype=np.float32)
    arr[:, :, :3] *= k
    arr[:, :, 3] = 255
    return Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), "RGBA")


def beam_tile(frame: Image.Image, box: tuple[int, int, int, int]) -> Image.Image:
    return scale128(frame.crop(box))


def rivet_field(siding: Image.Image) -> Image.Image:
    im = scale128(siding).filter(ImageFilter.SMOOTH)
    d = ImageDraw.Draw(im)
    for x, y in ((22, 22), (64, 22), (106, 22), (22, 64), (106, 64), (22, 106), (64, 106), (106, 106)):
        paint_rivet(d, x, y, 7)
    return im


def assemble_casing() -> Image.Image:
    siding = Image.open(MASTER / "craft_siding.png").convert("RGBA")
    frame = Image.open(MASTER / "craft_frame.png").convert("RGBA")
    out = Image.new("RGBA", (512, 512), (36, 14, 8, 255))
    plate = scale128(siding)
    frame_h = beam_tile(frame, (0, 0, 512, 40))
    pad = tile128(frame, 96, 86)
    corner = tile128(frame, 228, 86)
    solid = tile128(frame, 360, 86)
    out.paste(rivet_field(siding), (0, 0))
    out.paste(frame_h, (128, 0))
    out.paste(plate, (256, 0))
    out.paste(pad, (384, 0))
    out.paste(darker(plate, 0.92), (0, 128))
    out.paste(darker(plate, 0.78), (128, 128))
    out.paste(corner, (256, 128))
    out.paste(beam_tile(frame, (0, 42, 512, 82)), (384, 128))
    out.paste(solid, (0, 256))
    return out.filter(ImageFilter.SMOOTH)


def embed_bb_texture(bb_path: Path, png: Path, name: str) -> None:
    data = json.loads(bb_path.read_text(encoding="utf-8"))
    raw = png.read_bytes()
    data_url = "data:image/png;base64," + base64.b64encode(raw).decode("ascii")
    if data.get("textures"):
        tex = data["textures"][0]
        tex["name"] = name
        tex["source"] = data_url
        tex["path"] = str(png).replace("\\", "/")
        tex["relative_path"] = f"textures/master-512/{name}"
        tex["saved"] = True
        tex["internal"] = False
    bb_path.write_text(json.dumps(data), encoding="utf-8")


def paste_tiles(dst: Image.Image, src: Image.Image, skip: set[tuple[int, int]]) -> Image.Image:
    out = dst.copy()
    for y in range(0, 512, 128):
        for x in range(0, 512, 128):
            if (x, y) in skip:
                continue
            out.paste(src.crop((x, y, x + 128, y + 128)), (x, y))
    return out


def write_block_master(block_id: str, image: Image.Image) -> Path:
    dest = ART / block_id / "textures" / "master-512" / f"{block_id}.png"
    dest.parent.mkdir(parents=True, exist_ok=True)
    image.save(dest)
    (dest.parent / "SHA256SUMS.txt").write_text(f"{sha256(dest)}  {block_id}.png\n", encoding="utf-8")
    ship_png(dest, block_id)
    bb = ART / block_id / f"{block_id}.bbmodel"
    if bb.exists():
        embed_bb_texture(bb, dest, f"{block_id}.png")
    print(f"{block_id}: sha={sha256(dest)[:16]}")
    return dest


def retrofit_1x1() -> None:
    casing = assemble_casing()
    write_block_master("craft_casing", casing)
    malt = Image.open(
        ART / "craft_malt_house_controller" / "textures" / "master-512" / "craft_malt_house_controller.png"
    ).convert("RGBA")
    write_block_master(
        "craft_malt_house_controller",
        paste_tiles(malt, casing, set(MALT_KEEP)),
    )
    hull_only = {(256, 0), (0, 0), (384, 0)}
    for block_id in CONTROLLERS[1:]:
        current = Image.open(ART / block_id / "textures" / "master-512" / f"{block_id}.png").convert("RGBA")
        skip = {(x, y) for y in range(0, 512, 128) for x in range(0, 512, 128) if (x, y) not in hull_only}
        write_block_master(block_id, paste_tiles(current, casing, skip))


def write_lock_readme(digest: dict[str, str]) -> None:
    rows = "\n".join(f"- `{name}` — `{h}`" for name, h in digest.items())
    (MEGA / "textures" / "README.md").write_text(
        f"""# Craft malt house (formed) textures

**LOCKED 2026-08-29.** Do not repaint, remesh, or remap UVs without an
explicit unlock. `master-512` is the only source.

The approved mega-mesh lives in `../craft_malt_house.bbmodel`.
1×1 runtime cubes (`craft_casing`, `craft_malt_house_controller`) copy
tiles from these masters — they do not write back here.

Authored in **3×3×3 block space** (16 units per block). The BER scales
that unit cube by 3 at the art size. Other legal cubes (4³, 5³) keep the
9-slice `craft_casing` hull and real 1×1 fittings.

## Masters

{rows}

## Brief

- **Id** : `alcoholic:craft_malt_house` — mega-mesh formé, pas un nouveau bloc.
- **Art size** : 3×3×3. Face utile −Z.
- **Kit** : `craft_siding` (un panneau / face), `craft_frame` (H/V + coins),
  vitre / cadre, pupitre, ports fluide / item.
- **Intérieur** : plateau + grain + touraille (`brewing_kettle` tiles).
""",
        encoding="utf-8",
    )
    (ART / "craft_casing" / "textures" / "README.md").write_text(
        """# Craft casing textures

`master-512` contains the approved 512 atlas and `SHA256SUMS.txt`.
It is the only source used to generate lower-resolution runtime textures.

Locked copper is copied from `craft_malt_house` (`craft_siding` +
`craft_frame`). Geometry stays the industrial 1×1 hull. Do not write this
atlas back onto `industrial_casing`.

## Brief modelage (agent Blockbench)

- **Id / type** : `craft_casing` — cube 1×1 hull partagé (revêtement).
- **Kit hull** : master cuivre à recopier pour les contrôleurs craft.
- **Face utile** : −Z. Java Block/Item.
""",
        encoding="utf-8",
    )


def main() -> None:
    lock_bbmodel()
    digest = write_sums(MASTER)
    write_lock_readme(digest)
    for name in MEGA_RUNTIME:
        ship_png(MASTER / f"{name}.png", name)
    export_formed()
    retrofit_1x1()
    print("done")


if __name__ == "__main__":
    main()
