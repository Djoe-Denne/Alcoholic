"""Export craft 1x1 models and downsample every craft 512 master.

Does not recolor or rewrite Blockbench sources. Industrial fittings are
left alone. Formed BER JSON is written by export_formed_meshes.py.
"""

from __future__ import annotations

import hashlib
import json
import shutil
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "art" / "blockbench"
MODELS = ROOT / "minecraft-common" / "src" / "main" / "resources" / "assets" / "alcoholic" / "models" / "block"
TEX_MOD = ROOT / "minecraft-common" / "src" / "main" / "resources" / "assets" / "alcoholic" / "textures" / "block"
PACKS = (16, 32, 128, 256, 512)

ONES = (
    "craft_casing",
    "craft_malt_house_controller",
    "craft_mill_controller",
    "craft_mash_tun_controller",
    "craft_brewing_kettle_controller",
    "craft_vat_controller",
)

MASTERS = (
    ART / "craft_casing" / "textures" / "master-512" / "craft_casing.png",
    ART / "craft_malt_house_controller" / "textures" / "master-512" / "craft_malt_house_controller.png",
    ART / "craft_mill_controller" / "textures" / "master-512" / "craft_mill_controller.png",
    ART / "craft_mash_tun_controller" / "textures" / "master-512" / "craft_mash_tun_controller.png",
    ART / "craft_brewing_kettle_controller" / "textures" / "master-512" / "craft_brewing_kettle_controller.png",
    ART / "craft_vat_controller" / "textures" / "master-512" / "craft_vat_controller.png",
    ART / "craft_malt_house" / "textures" / "master-512" / "craft_siding.png",
    ART / "craft_malt_house" / "textures" / "master-512" / "craft_frame.png",
    ART / "craft_malt_house" / "textures" / "master-512" / "craft_window_glass.png",
    ART / "craft_malt_house" / "textures" / "master-512" / "craft_window_frame.png",
    ART / "craft_malt_house" / "textures" / "master-512" / "craft_malt_house_desk.png",
    ART / "craft_malt_house" / "textures" / "master-512" / "craft_fluid_face.png",
    ART / "craft_malt_house" / "textures" / "master-512" / "craft_item_face.png",
    ART / "craft_malt_house" / "textures" / "master-512" / "craft_malt_house.png",
)


def uv16(uv: list[float]) -> list[float]:
    return [round(float(v) / 32.0, 2) for v in uv]


def to_game_elements(elements: list[dict]) -> list[dict]:
    out = []
    for el in elements:
        if "from" not in el:
            continue
        faces = {}
        for side, face in (el.get("faces") or {}).items():
            if not isinstance(face, dict) or "uv" not in face:
                continue
            nf = {"uv": uv16(face["uv"]), "texture": "#0"}
            if face.get("rotation") in (90, 180, 270):
                nf["rotation"] = int(face["rotation"])
            if "cullface" in face:
                nf["cullface"] = face["cullface"]
            faces[side] = nf
        item = {"name": el.get("name") or "cube", "from": el["from"], "to": el["to"], "faces": faces}
        if el.get("rotation"):
            item["rotation"] = el["rotation"]
        if el.get("shade") is False:
            item["shade"] = False
        out.append(item)
    return out


def export_ones() -> None:
    for block_id in ONES:
        bb = json.loads((ART / block_id / f"{block_id}.bbmodel").read_text(encoding="utf-8"))
        dest = MODELS / f"{block_id}.json"
        display = {}
        if dest.exists():
            display = json.loads(dest.read_text(encoding="utf-8")).get("display") or {}
        game = {
            "parent": "minecraft:block/block",
            "textures": {
                "0": f"alcoholic:block/{block_id}",
                "particle": f"alcoholic:block/{block_id}",
            },
            "elements": to_game_elements(bb.get("elements") or []),
        }
        if display:
            game["display"] = display
        dest.write_text(json.dumps(game, indent="\t") + "\n", encoding="utf-8")
        print(f"1x1 {block_id}: {len(game['elements'])} cubes")


def ship_texture(master: Path, sums: dict[Path, list[str]]) -> None:
    if not master.exists():
        print(f"skip missing {master}")
        return
    name = master.name
    digest = hashlib.sha256(master.read_bytes()).hexdigest()
    sums.setdefault(master.parent, []).append(f"{digest}  {name}")
    img = Image.open(master).convert("RGBA")
    for pack in PACKS:
        dest = ROOT / f"resourcepacks/Alcoholic-{pack}x/assets/alcoholic/textures/block/{name}"
        dest.parent.mkdir(parents=True, exist_ok=True)
        if pack == 512:
            shutil.copy2(master, dest)
        else:
            img.resize((pack, pack), Image.Resampling.LANCZOS).save(dest)
    img.resize((64, 64), Image.Resampling.LANCZOS).save(TEX_MOD / name)
    print(f"tex {name} sha={digest[:16]}")


def main() -> None:
    export_ones()
    sums: dict[Path, list[str]] = {}
    for master in MASTERS:
        ship_texture(master, sums)
    for folder, lines in sums.items():
        (folder / "SHA256SUMS.txt").write_text("\n".join(sorted(lines)) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
