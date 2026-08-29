"""Seed craft_mill mega-mesh from the locked malt-house hull language."""

from __future__ import annotations

import copy
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "art" / "blockbench" / "craft_malt_house" / "craft_malt_house.bbmodel"
DST = ROOT / "art" / "blockbench" / "craft_mill" / "craft_mill.bbmodel"

DROP = {
    "win_r_body",
    "win_r_glass",
    "win_r_xh",
    "win_r_xv",
    "sill_r",
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
    el = next(e for e in src["elements"] if e.get("name") == name)
    return copy.deepcopy(el["faces"])


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


def main() -> None:
    src = json.loads(SRC.read_text(encoding="utf-8"))
    data = copy.deepcopy(src)
    data["name"] = "craft_mill"
    data["visible_box"] = [3, 3, 0]

    for tex in data["textures"]:
        if tex.get("name") == "craft_malt_house_controller.png":
            tex["name"] = "craft_mill_controller.png"
            tex["relative_path"] = (
                "../../../resourcepacks/Alcoholic-512x/assets/alcoholic/"
                "textures/block/craft_mill_controller.png"
            )
            tex.pop("path", None)

    existing_ids = {str(t.get("id")) for t in data["textures"]}
    extras = [
        (
            "19",
            "kinetic_port.png",
            "../../../resourcepacks/Alcoholic-512x/assets/alcoholic/textures/block/kinetic_port.png",
        ),
        (
            "20",
            "access_hatch.png",
            "../../../resourcepacks/Alcoholic-512x/assets/alcoholic/textures/block/access_hatch.png",
        ),
        (
            "21",
            "industrial_casing.png",
            "../../../resourcepacks/Alcoholic-512x/assets/alcoholic/textures/block/industrial_casing.png",
        ),
    ]
    for tid, name, rel in extras:
        if tid in existing_ids:
            continue
        data["textures"].append(
            {
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
            }
        )

    kept = [e for e in data["elements"] if e.get("name") not in DROP]
    steel = faces_of(src, "win_l_xh")
    glass = faces_of(src, "win_l_glass")
    body = faces_of(src, "win_l_body")
    siding = faces_of(src, "copper_core")
    ctrl_body = faces_of(src, "ctrl_body")
    fluid = faces_of(src, "fluid_flange")
    item = faces_of(src, "item_mouth")
    iron = {s: {"uv": [0, 0, 512, 512], "texture": "21"} for s in ctrl_body}
    kinetic = {s: {"uv": [0, 0, 512, 512], "texture": "19"} for s in ctrl_body}
    hatch = {s: {"uv": [0, 0, 512, 512], "texture": "20"} for s in ctrl_body}

    extra = [
        cube("win_wide_body", [16.25, 32.2, -1.45], [47.75, 47.8, 1.7], body),
        cube("win_wide_glass", [18.4, 34.4, -2.55], [45.6, 45.6, 0.15], glass),
        cube("win_wide_bar_a", [24.1, 34.2, -3.05], [25.7, 45.8, -1.7], steel),
        cube("win_wide_bar_b", [31.1, 34.2, -3.1], [32.7, 45.8, -1.75], steel),
        cube("win_wide_bar_c", [38.1, 34.2, -3.05], [39.7, 45.8, -1.7], steel),
        cube("win_wide_sill", [16.35, 31.25, -1.95], [47.65, 32.15, 0.55], siding),
        cube("hatch_body", [0.25, 16.2, -1.5], [15.75, 31.8, 1.75], hatch),
        cube("hatch_door", [1.8, 17.8, -2.55], [14.2, 30.2, -0.35], hatch),
        cube("hatch_hinge_u", [1.15, 27.4, -3.05], [3.35, 29.8, -1.55], steel),
        cube("hatch_hinge_d", [1.15, 18.2, -3.05], [3.35, 20.6, -1.55], steel),
        cube("hatch_latch", [12.4, 22.4, -3.15], [14.6, 25.6, -1.65], steel),
        cube("kin_body", [46.25, 16.2, 16.2], [49.55, 31.8, 31.8], kinetic),
        cube("kin_ring", [48.55, 18.6, 18.6], [50.85, 29.4, 29.4], kinetic),
        cube("kin_hub", [49.9, 21.1, 21.1], [51.55, 26.9, 26.9], steel),
        cube("kin_shaft", [51.1, 22.15, 22.15], [55.4, 25.85, 25.85], steel),
        cube("kin_key", [53.3, 21.55, 23.35], [54.7, 26.45, 24.65], iron),
        cube("roller_lo", [17.6, 34.15, 6.4], [43.8, 37.85, 10.2], iron),
        cube("roller_lo_flat", [17.8, 33.55, 7.05], [43.6, 38.45, 9.55], iron),
        cube("roller_hi", [17.6, 39.25, 8.1], [43.8, 42.95, 11.9], iron),
        cube("roller_hi_flat", [17.8, 38.65, 8.75], [43.6, 43.55, 11.25], iron),
        cube("roller_lo_ring_w", [17.15, 33.85, 6.15], [18.55, 38.15, 10.45], fluid),
        cube("roller_lo_ring_e", [42.85, 33.85, 6.15], [44.25, 38.15, 10.45], fluid),
        cube("roller_hi_ring_w", [17.15, 38.95, 7.85], [18.55, 43.25, 12.15], fluid),
        cube("roller_hi_ring_e", [42.85, 38.95, 7.85], [44.25, 43.25, 12.15], fluid),
        cube("hopper_lip", [17.4, 47.95, 17.4], [30.6, 49.15, 30.6], siding),
        cube("hopper_well", [19.2, 46.85, 19.2], [28.8, 48.55, 28.8], item),
    ]
    data["elements"] = kept + extra
    DST.parent.mkdir(parents=True, exist_ok=True)
    DST.write_text(json.dumps(data), encoding="utf-8")
    print(f"wrote {DST} cubes={len(data['elements'])}")


if __name__ == "__main__":
    main()
