"""Export one craft controller from its industrial sister.

Recolors hull UV islands to workshop copper. Leaves desk / pictogram /
fitting cubes on the industrial paint (steel, lamps off, icons).
"""

from __future__ import annotations

import argparse
import base64
import hashlib
import json
import shutil
from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "art" / "blockbench"
PACKS = (16, 32, 128, 256, 512)
COPPER = {
    "shadow": np.array([92.0, 42.0, 22.0]),
    "mid": np.array([183.0, 116.0, 54.0]),
    "hi": np.array([228.0, 168.0, 98.0]),
}

PAIRS = {
    "craft_malt_house_controller": "industrial_malt_house_controller",
    "craft_mill_controller": "industrial_roller_mill_controller",
    "craft_mash_tun_controller": "industrial_mash_tun_controller",
    "craft_brewing_kettle_controller": "industrial_brewing_kettle_controller",
    "craft_vat_controller": "industrial_vat_controller",
}


def load_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def hull_names() -> set[str]:
    data = load_json(ART / "craft_casing" / "craft_casing.bbmodel")
    return {el["name"] for el in data["elements"] if "from" in el}


def uv_rects(elements: list[dict], names: set[str]) -> list[tuple[int, int, int, int]]:
    rects = []
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
            rects.append((max(0, x0 - 1), max(0, y0 - 1), min(512, x1 + 1), min(512, y1 + 1)))
    return rects


def desk_mask(elements: list[dict], desk: set[str], size: int = 512) -> np.ndarray:
    mask = np.zeros((size, size), dtype=bool)
    for x0, y0, x1, y1 in uv_rects(elements, desk):
        mask[y0:y1, x0:x1] = True
    return mask


def recolor_copper(src: Image.Image, preserve: np.ndarray) -> Image.Image:
    arr = np.array(src.convert("RGBA"), dtype=np.float32)
    rgb, a = arr[:, :, :3], arr[:, :, 3]
    luma = 0.2126 * rgb[:, :, 0] + 0.7152 * rgb[:, :, 1] + 0.0722 * rgb[:, :, 2]
    opaque = a > 0
    p2, p98 = np.percentile(luma[opaque], [2, 98])
    t = np.clip((luma - p2) / (p98 - p2 + 1e-6), 0, 1)
    out = np.zeros_like(rgb)
    split = 0.52
    mid_m = t < split
    t1 = np.clip(t / split, 0, 1)
    t2 = np.clip((t - split) / (1 - split), 0, 1)
    out[mid_m] = COPPER["shadow"] + (COPPER["mid"] - COPPER["shadow"]) * t1[mid_m, None]
    out[~mid_m] = COPPER["mid"] + (COPPER["hi"] - COPPER["mid"]) * t2[~mid_m, None]
    dev = (rgb.mean(axis=2) - luma) * 0.45
    out = np.clip(out + dev[:, :, None], 0, 255)
    warm = 1.0 + 0.04 * (1.0 - np.abs(t - 0.5) * 2)
    out[:, :, 0] = np.clip(out[:, :, 0] * warm, 0, 255)
    out[:, :, 2] = np.clip(out[:, :, 2] * (2.0 - warm), 0, 255)
    keep = preserve | (a == 0)
    out[keep] = rgb[keep]
    result = np.dstack([out.astype(np.uint8), a.astype(np.uint8)])
    return Image.fromarray(result, "RGBA")


def uv16(uv: list[float]) -> list[float]:
    return [round(v / 32.0, 2) for v in uv]


def to_game_elements(elements: list[dict]) -> list[dict]:
    out = []
    for el in elements:
        faces = {}
        for side, face in el.get("faces", {}).items():
            nf = {"uv": uv16(face["uv"]), "texture": "#0"}
            if "rotation" in face:
                nf["rotation"] = face["rotation"]
            if "cullface" in face:
                nf["cullface"] = face["cullface"]
            faces[side] = nf
        item = {"name": el["name"], "from": el["from"], "to": el["to"], "faces": faces}
        if el.get("rotation"):
            item["rotation"] = el["rotation"]
        if el.get("shade") is False:
            item["shade"] = False
        out.append(item)
    return out


def downsample(master: Path, dest: Path, size: int) -> None:
    dest.parent.mkdir(parents=True, exist_ok=True)
    img = Image.open(master).convert("RGBA")
    if size == 512:
        shutil.copy2(master, dest)
    else:
        img.resize((size, size), Image.Resampling.BOX).save(dest)


def write_readme(craft_id: str, industrial_id: str, extra: list[str]) -> None:
    path = ART / craft_id / "textures" / "README.md"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        f"""# {craft_id} textures

`master-512` contains the approved 512 atlas and `SHA256SUMS.txt`.
It is the only source used to generate lower-resolution runtime textures.

Hull cubes are a copper recolor of `{industrial_id}`. Desk / pictogram /
fitting cubes stay on the industrial paint (steel pupitre, lamps off).

Do not write this atlas back onto `{industrial_id}`.

## Brief

- **Id** : `{craft_id}` — contrôleur unitaire 1×1 (non formé).
- **Refs** : `art/blockbench/{craft_id}/reference.png`.
- **Kit hull** : cuivre `craft_casing` ; pupitre −Z ; extras = {", ".join(extra[:8])}…
- **Face utile** : −Z. Java Block/Item.
- **Datagen** : `CraftAssetData` blockstate + item only.
""",
        encoding="utf-8",
    )


def export_one(craft_id: str) -> None:
    industrial_id = PAIRS[craft_id]
    src_bb = load_json(ART / industrial_id / f"{industrial_id}.bbmodel")
    src_png = Image.open(
        ART / industrial_id / "textures" / "master-512" / f"{industrial_id}.png"
    ).convert("RGBA")
    assert src_png.size == (512, 512), src_png.size

    hull = hull_names()
    elements = [el for el in src_bb["elements"] if "from" in el]
    extra = [el["name"] for el in elements if el["name"] not in hull]
    hull_mask = desk_mask(elements, hull)
    extra_mask = desk_mask(elements, set(extra))
    unique_desk = extra_mask & ~hull_mask
    painted = recolor_copper(src_png, unique_desk)

    art_dir = ART / craft_id
    master = art_dir / "textures" / "master-512" / f"{craft_id}.png"
    master.parent.mkdir(parents=True, exist_ok=True)
    painted.save(master)
    digest = hashlib.sha256(master.read_bytes()).hexdigest()
    (master.parent / "SHA256SUMS.txt").write_text(
        f"{digest}  {craft_id}.png\n", encoding="utf-8"
    )
    write_readme(craft_id, industrial_id, extra)

    for pack in PACKS:
        downsample(
            master,
            ROOT / f"resourcepacks/Alcoholic-{pack}x/assets/alcoholic/textures/block/{craft_id}.png",
            pack,
        )
    downsample(
        master,
        ROOT / "minecraft-common/src/main/resources/assets/alcoholic/textures/block" / f"{craft_id}.png",
        64,
    )

    display_src = ROOT / "minecraft-common/src/main/resources/assets/alcoholic/models/block" / f"{industrial_id}.json"
    display = load_json(display_src).get("display", {}) if display_src.exists() else {}
    game = {
        "parent": "minecraft:block/block",
        "textures": {
            "0": f"alcoholic:block/{craft_id}",
            "particle": f"alcoholic:block/{craft_id}",
        },
        "elements": to_game_elements(elements),
        "display": display,
    }
    game_path = ROOT / "minecraft-common/src/main/resources/assets/alcoholic/models/block" / f"{craft_id}.json"
    game_path.write_text(json.dumps(game, indent="\t") + "\n", encoding="utf-8")

    bb = json.loads(json.dumps(src_bb))
    bb["name"] = craft_id
    raw = master.read_bytes()
    data_url = "data:image/png;base64," + base64.b64encode(raw).decode("ascii")
    if bb.get("textures"):
        tex = bb["textures"][0]
        tex["name"] = f"{craft_id}.png"
        tex["source"] = data_url
        tex["path"] = str(master).replace("\\", "/")
        tex["relative_path"] = f"textures/master-512/{craft_id}.png"
        tex["saved"] = True
        tex["internal"] = False
    bb_path = art_dir / f"{craft_id}.bbmodel"
    bb_path.write_text(json.dumps(bb), encoding="utf-8")

    print(
        f"{craft_id}: cubes={len(elements)} extra={len(extra)} "
        f"desk_only={int(unique_desk.sum())} sha={digest[:16]} "
        f"model={game_path.stat().st_size}"
    )


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("ids", nargs="+", choices=sorted(PAIRS) + ["all"])
    args = parser.parse_args()
    targets = list(PAIRS) if args.ids == ["all"] else args.ids
    for craft_id in targets:
        export_one(craft_id)


if __name__ == "__main__":
    main()
