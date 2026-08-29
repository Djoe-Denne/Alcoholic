#!/usr/bin/env python3
"""Export formed Blockbench overviews to 1.19.2 BER JSON (0-16 = art cuboid)."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "art" / "blockbench"
OUT = ROOT / "minecraft-common" / "src" / "main" / "resources" / "assets" / "alcoholic" / "models" / "block" / "formed"
TEX_OUT = ROOT / "minecraft-common" / "src" / "main" / "resources" / "assets" / "alcoholic" / "textures" / "block" / "formed"

ART_SIZES = {
    "industrial_malt_house": (5, 4, 5),
    "industrial_roller_mill": (3, 4, 3),
    "industrial_mash_tun": (5, 5, 5),
    "industrial_brewing_kettle": (5, 6, 5),
    "industrial_fermentation_vat": (3, 5, 3),
    "industrial_conditioning_vessel": (3, 6, 3),
    "industrial_storage_tank": (3, 5, 3),
    "industrial_press": (3, 4, 3),
    "craft_malt_house": (3, 3, 3),
}

TEXTURE_RL = {
    "industrial_casing": "alcoholic:block/industrial_casing",
    "industrial_casing.png": "alcoholic:block/industrial_casing",
    "access_hatch.png": "alcoholic:block/access_hatch",
    "machine_window.png": "alcoholic:block/machine_window",
    "item_port.png": "alcoholic:block/item_port",
    "fluid_port.png": "alcoholic:block/fluid_port",
    "kinetic_port.png": "alcoholic:block/kinetic_port",
    "industrial_malt_house_controller.png": "alcoholic:block/industrial_malt_house_controller",
    "industrial_roller_mill_controller.png": "alcoholic:block/industrial_roller_mill_controller",
    "industrial_mash_tun_controller.png": "alcoholic:block/industrial_mash_tun_controller",
    "industrial_brewing_kettle_controller.png": "alcoholic:block/industrial_brewing_kettle_controller",
    "industrial_vat_controller.png": "alcoholic:block/industrial_vat_controller",
    "industrial_conditioning_vessel_controller.png": "alcoholic:block/industrial_conditioning_vessel_controller",
    "industrial_tank_controller.png": "alcoholic:block/industrial_tank_controller",
    "industrial_press_controller.png": "alcoholic:block/industrial_press_controller",
    "industrial_brewing_kettle.png": "alcoholic:block/formed/industrial_brewing_kettle",
    "craft_casing": "alcoholic:block/craft_casing",
    "craft_casing.png": "alcoholic:block/craft_casing",
    "craft_malt_house_controller.png": "alcoholic:block/craft_malt_house_controller",
    "craft_malt_house": "alcoholic:block/formed/craft_malt_house",
    "craft_malt_house.png": "alcoholic:block/formed/craft_malt_house",
}


def clamp(value: float, lo: float = 0.0, hi: float = 16.0) -> float:
    return max(lo, min(hi, value))


def round6(value: float) -> float:
    return round(value + 0.0, 6)


def texture_rl(name: str) -> str:
    return TEXTURE_RL.get(name, TEXTURE_RL.get(name.replace(".png", ""), f"alcoholic:block/{Path(name).stem}"))


def convert_model(name: str) -> dict:
    width, height, depth = ART_SIZES[name]
    data = json.loads((ART / name / f"{name}.bbmodel").read_text(encoding="utf-8"))
    textures = data.get("textures") or []
    tex_map: dict[str, str] = {}
    texture_defs: dict[str, str] = {}
    for index, texture in enumerate(textures):
        rl = texture_rl(str(texture.get("name") or f"tex_{index}"))
        key = str(index)
        tex_map[key] = f"#{key}"
        if texture.get("id") is not None:
            tex_map[str(texture["id"])] = f"#{key}"
        texture_defs[key] = rl
    texture_defs["particle"] = next(iter(texture_defs.values()), "alcoholic:block/industrial_casing")
    resolution = (data.get("resolution") or {}).get("width") or 512
    elements = []
    for raw in data.get("elements") or []:
        fr = list(raw.get("from") or [0, 0, 0])
        to = list(raw.get("to") or [0, 0, 0])
        from_j = [round6(fr[0] / width), round6(fr[1] / height), round6(fr[2] / depth)]
        to_j = [round6(to[0] / width), round6(to[1] / height), round6(to[2] / depth)]
        for axis in range(3):
            if to_j[axis] <= from_j[axis]:
                from_j[axis], to_j[axis] = to_j[axis], from_j[axis]
            if abs(to_j[axis] - from_j[axis]) < 1e-4:
                to_j[axis] = from_j[axis] + 0.01
        faces_out = {}
        for face_name, face in (raw.get("faces") or {}).items():
            if not isinstance(face, dict):
                continue
            tex = face.get("texture")
            ref = tex_map.get(str(tex), "#0")
            uv = list(face.get("uv") or [0, 0, resolution, resolution])
            uv_j = [round6(clamp(v * 16.0 / resolution)) for v in uv]
            entry = {"uv": uv_j, "texture": ref}
            rotation = face.get("rotation")
            if rotation in (90, 180, 270):
                entry["rotation"] = int(rotation)
            faces_out[face_name] = entry
        if not faces_out:
            continue
        element = {"name": raw.get("name") or "cube", "from": from_j, "to": to_j, "faces": faces_out}
        elements.append(element)
    return {
        "parent": "minecraft:block/block",
        "textures": texture_defs,
        "elements": elements,
    }


def downsample_png(src: Path, dest: Path, size: int = 64) -> None:
    dest.parent.mkdir(parents=True, exist_ok=True)
    try:
        from PIL import Image

        image = Image.open(src).convert("RGBA")
        image = image.resize((size, size), Image.Resampling.BOX)
        image.save(dest)
        return
    except Exception:
        pass
    # Fallback: nearest-neighbor from a standard 8-bit RGBA PNG.
    raw = src.read_bytes()
    if raw[:8] != b"\x89PNG\r\n\x1a\n":
        dest.write_bytes(raw)
        return
    dest.write_bytes(raw)


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    kettle = ART / "industrial_brewing_kettle" / "textures" / "master-512" / "industrial_brewing_kettle.png"
    if kettle.exists():
        downsample_png(kettle, TEX_OUT / "industrial_brewing_kettle.png")
    for name in ART_SIZES:
        model = convert_model(name)
        path = OUT / f"{name}.json"
        path.write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")
        print(f"{name}: {len(model['elements'])} elements -> {path.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
