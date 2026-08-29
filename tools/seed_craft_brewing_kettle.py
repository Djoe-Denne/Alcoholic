"""Seed craft_brewing_kettle mega-mesh from the malt-house hull language."""

from __future__ import annotations

import copy
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "art" / "blockbench" / "craft_malt_house" / "craft_malt_house.bbmodel"
DST = ROOT / "art" / "blockbench" / "craft_brewing_kettle" / "craft_brewing_kettle.bbmodel"

DROP = {
    "vent_body",
    "vent_slot",
    "vent_cap",
    "vent_ring",
    "floor_tray",
    "grain_l",
    "grain_c",
    "grain_r",
    "kiln_box",
    "kiln_arch",
    "flame_a",
    "flame_b",
}


def faces_of(src: dict, name: str) -> dict:
    return copy.deepcopy(next(e for e in src["elements"] if e.get("name") == name)["faces"])


def cube(name: str, frm: list[float], to: list[float], faces: dict) -> dict:
    return {
        "name": name,
        "box_uv": False,
        "render_order": "default",
        "rescale": False,
        "locked": False,
        "shade": True,
        "light_emission": 0,
        "export": True,
        "scope": 0,
        "allow_mirror_modeling": True,
        "from": frm,
        "to": to,
        "autouv": 0,
        "color": 1,
        "origin": [24, 24, 24],
        "faces": faces,
    }


def tex_faces(tid: str) -> dict:
    return {s: {"uv": [0, 0, 512, 512], "texture": tid} for s in
            ("north", "east", "south", "west", "up", "down")}


def apply_siding(elements: list[dict]) -> None:
    plate = {
        "plate_n": "north",
        "plate_s": "south",
        "plate_w": "west",
        "plate_e": "east",
        "plate_u": "up",
        "plate_d": "down",
    }
    for e in elements:
        if e.get("name") == "copper_core":
            for f in e["faces"].values():
                f["texture"] = "13"
        keep = plate.get(e.get("name") or "")
        if keep:
            for side, f in e["faces"].items():
                f["texture"] = "13" if side == keep else "18"


def main() -> None:
    src = json.loads(SRC.read_text(encoding="utf-8"))
    data = copy.deepcopy(src)
    data["name"] = "craft_brewing_kettle"
    data["visible_box"] = [3, 3, 0]
    for tex in data["textures"]:
        if tex.get("name") == "craft_malt_house_controller.png":
            tex["name"] = "craft_brewing_kettle_controller.png"
            tex["relative_path"] = (
                "../../../resourcepacks/Alcoholic-512x/assets/alcoholic/"
                "textures/block/craft_brewing_kettle_controller.png"
            )
            tex.pop("path", None)
    existing = {str(t.get("id")) for t in data["textures"]}
    if "20" not in existing:
        data["textures"].append({
            "name": "access_hatch.png",
            "relative_path": "../../../resourcepacks/Alcoholic-512x/assets/alcoholic/textures/block/access_hatch.png",
            "id": "20",
            "width": 512,
            "height": 512,
            "uv_width": 512,
            "uv_height": 512,
            "visible": True,
            "internal": True,
            "saved": True,
        })
    if "21" not in existing:
        data["textures"].append({
            "name": "industrial_casing.png",
            "relative_path": "../../../resourcepacks/Alcoholic-512x/assets/alcoholic/textures/block/industrial_casing.png",
            "id": "21",
            "width": 512,
            "height": 512,
            "uv_width": 512,
            "uv_height": 512,
            "visible": True,
            "internal": True,
            "saved": True,
        })

    kept = [e for e in data["elements"] if e.get("name") not in DROP]
    apply_siding(kept)
    steel = faces_of(src, "win_l_xh")
    siding = faces_of(src, "copper_core")
    for f in siding.values():
        f["texture"] = "13"
    kettle = faces_of(src, "ctrl_body")
    iron = tex_faces("21")
    hatch = tex_faces("20")

    extra = [
        cube("hatch_body", [0.25, 16.2, -1.5], [15.75, 31.8, 1.75], hatch),
        cube("hatch_door", [1.8, 17.8, -2.55], [14.2, 30.2, -0.35], hatch),
        cube("hatch_hinge_u", [1.15, 27.4, -3.05], [3.35, 29.8, -1.55], steel),
        cube("hatch_hinge_d", [1.15, 18.2, -3.05], [3.35, 20.6, -1.55], steel),
        cube("hatch_latch", [12.4, 22.4, -3.15], [14.6, 25.6, -1.65], steel),
        cube("hoop_n_lo", [2.2, 8.8, -0.62], [45.8, 12.1, 0.95], iron),
        cube("hoop_s_lo", [2.2, 8.8, 47.05], [45.8, 12.1, 48.62], iron),
        cube("hoop_w_lo", [-0.62, 8.8, 2.2], [0.95, 12.1, 45.8], iron),
        cube("hoop_e_lo", [47.05, 8.8, 2.2], [48.62, 12.1, 45.8], iron),
        cube("hoop_n_hi", [2.2, 28.6, -0.68], [45.8, 31.8, 0.88], iron),
        cube("hoop_s_hi", [2.2, 28.6, 47.12], [45.8, 31.8, 48.68], iron),
        cube("hoop_w_hi", [-0.68, 28.6, 2.2], [0.88, 31.8, 45.8], iron),
        cube("hoop_e_hi", [47.12, 28.6, 2.2], [48.68, 31.8, 45.8], iron),
        cube("wort_level", [8.4, 18.2, 6.6], [39.6, 24.4, 39.8], kettle),
        cube("dome_low", [13.4, 47.92, 13.4], [34.6, 50.35, 34.6], siding),
        cube("dome_mid", [16.8, 50.05, 16.8], [31.2, 52.55, 31.2], siding),
        cube("dome_cap", [19.6, 52.25, 19.6], [28.4, 53.85, 28.4], siding),
        cube("steam_stack", [21.4, 53.55, 21.4], [26.6, 57.45, 26.6], iron),
        cube("steam_cap", [20.6, 57.15, 20.6], [27.4, 58.35, 27.4], iron),
    ]
    data["elements"] = kept + extra
    DST.parent.mkdir(parents=True, exist_ok=True)
    DST.write_text(json.dumps(data), encoding="utf-8")
    print(f"wrote {DST} cubes={len(data['elements'])}")


if __name__ == "__main__":
    main()
