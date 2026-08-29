"""Seed craft_vat mega-mesh from the malt-house hull language."""

from __future__ import annotations

import copy
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "art" / "blockbench" / "craft_malt_house" / "craft_malt_house.bbmodel"
DST = ROOT / "art" / "blockbench" / "craft_vat" / "craft_vat.bbmodel"

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
    data["name"] = "craft_vat"
    data["visible_box"] = [3, 3, 0]
    for tex in data["textures"]:
        if tex.get("name") == "craft_malt_house_controller.png":
            tex["name"] = "craft_vat_controller.png"
            tex["relative_path"] = (
                "../../../resourcepacks/Alcoholic-512x/assets/alcoholic/"
                "textures/block/craft_vat_controller.png"
            )
            tex.pop("path", None)
    existing = {str(t.get("id")) for t in data["textures"]}
    extras = [
        ("20", "access_hatch.png", "../../../resourcepacks/Alcoholic-512x/assets/alcoholic/textures/block/access_hatch.png"),
        ("21", "industrial_casing.png", "../../../resourcepacks/Alcoholic-512x/assets/alcoholic/textures/block/industrial_casing.png"),
    ]
    for tid, name, rel in extras:
        if tid in existing:
            continue
        data["textures"].append({
            "name": name,
            "relative_path": rel,
            "id": tid,
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
    glass = faces_of(src, "win_l_glass")
    kettle = faces_of(src, "ctrl_body")
    iron = tex_faces("21")
    hatch = tex_faces("20")

    extra = [
        cube("hatch_body", [0.25, 16.2, -1.5], [15.75, 31.8, 1.75], hatch),
        cube("hatch_door", [1.8, 17.8, -2.55], [14.2, 30.2, -0.35], hatch),
        cube("hatch_hinge_u", [1.15, 27.4, -3.05], [3.35, 29.8, -1.55], steel),
        cube("hatch_hinge_d", [1.15, 18.2, -3.05], [3.35, 20.6, -1.55], steel),
        cube("hatch_latch", [12.4, 22.4, -3.15], [14.6, 25.6, -1.65], steel),
        cube("hoop_n_lo", [2.2, 7.6, -0.62], [45.8, 11.0, 0.95], iron),
        cube("hoop_s_lo", [2.2, 7.6, 47.05], [45.8, 11.0, 48.62], iron),
        cube("hoop_w_lo", [-0.62, 7.6, 2.2], [0.95, 11.0, 45.8], iron),
        cube("hoop_e_lo", [47.05, 7.6, 2.2], [48.62, 11.0, 45.8], iron),
        cube("hoop_n_mid", [2.2, 20.4, -0.65], [45.8, 23.6, 0.9], iron),
        cube("hoop_s_mid", [2.2, 20.4, 47.1], [45.8, 23.6, 48.65], iron),
        cube("hoop_w_mid", [-0.65, 20.4, 2.2], [0.9, 23.6, 45.8], iron),
        cube("hoop_e_mid", [47.1, 20.4, 2.2], [48.65, 23.6, 45.8], iron),
        cube("hoop_n_hi", [2.2, 33.2, -0.68], [45.8, 36.4, 0.88], iron),
        cube("hoop_s_hi", [2.2, 33.2, 47.12], [45.8, 36.4, 48.68], iron),
        cube("hoop_w_hi", [-0.68, 33.2, 2.2], [0.88, 36.4, 45.8], iron),
        cube("hoop_e_hi", [47.12, 33.2, 2.2], [48.68, 36.4, 45.8], iron),
        cube("wash_level", [8.4, 16.6, 6.8], [39.6, 28.8, 39.8], kettle),
        cube("bubble_a", [14.2, 29.1, 8.4], [16.4, 31.2, 10.6], glass),
        cube("bubble_b", [27.6, 30.2, 9.1], [29.4, 31.9, 10.9], glass),
        cube("airlock_base", [20.2, 47.92, 20.2], [27.8, 49.55, 27.8], siding),
        cube("airlock_glass", [21.4, 49.35, 21.4], [26.6, 53.15, 26.6], glass),
        cube("airlock_collar", [20.8, 52.85, 20.8], [27.2, 54.15, 27.2], iron),
        cube("airlock_cross_x", [21.1, 50.4, 23.5], [26.9, 51.5, 24.5], steel),
        cube("airlock_cross_z", [23.5, 50.4, 21.1], [24.5, 51.5, 26.9], steel),
    ]
    data["elements"] = kept + extra
    DST.parent.mkdir(parents=True, exist_ok=True)
    DST.write_text(json.dumps(data), encoding="utf-8")
    print(f"wrote {DST} cubes={len(data['elements'])}")


if __name__ == "__main__":
    main()
