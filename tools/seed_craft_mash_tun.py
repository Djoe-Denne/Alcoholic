"""Seed craft_mash_tun mega-mesh from the malt-house hull language."""

from __future__ import annotations

import copy
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "art" / "blockbench" / "craft_malt_house" / "craft_malt_house.bbmodel"
DST = ROOT / "art" / "blockbench" / "craft_mash_tun" / "craft_mash_tun.bbmodel"

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


def tex_faces(tid: str) -> dict:
    return {s: {"uv": [0, 0, 512, 512], "texture": tid} for s in
            ("north", "east", "south", "west", "up", "down")}


def main() -> None:
    src = json.loads(SRC.read_text(encoding="utf-8"))
    data = copy.deepcopy(src)
    data["name"] = "craft_mash_tun"
    data["visible_box"] = [3, 3, 0]

    for tex in data["textures"]:
        if tex.get("name") == "craft_malt_house_controller.png":
            tex["name"] = "craft_mash_tun_controller.png"
            tex["relative_path"] = (
                "../../../resourcepacks/Alcoholic-512x/assets/alcoholic/"
                "textures/block/craft_mash_tun_controller.png"
            )
            tex.pop("path", None)

    existing_ids = {str(t.get("id")) for t in data["textures"]}
    extras = [
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
    siding = faces_of(src, "copper_core")
    kettle = faces_of(src, "ctrl_body")
    iron = tex_faces("21")
    hatch = tex_faces("20")

    extra = [
        cube("hatch_body", [0.25, 16.2, -1.5], [15.75, 31.8, 1.75], hatch),
        cube("hatch_door", [1.8, 17.8, -2.55], [14.2, 30.2, -0.35], hatch),
        cube("hatch_hinge_u", [1.15, 27.4, -3.05], [3.35, 29.8, -1.55], steel),
        cube("hatch_hinge_d", [1.15, 18.2, -3.05], [3.35, 20.6, -1.55], steel),
        cube("hatch_latch", [12.4, 22.4, -3.15], [14.6, 25.6, -1.65], steel),
        cube("hoop_n_lo", [2.2, 9.4, -0.62], [45.8, 12.6, 0.95], iron),
        cube("hoop_s_lo", [2.2, 9.4, 47.05], [45.8, 12.6, 48.62], iron),
        cube("hoop_w_lo", [-0.62, 9.4, 2.2], [0.95, 12.6, 45.8], iron),
        cube("hoop_e_lo", [47.05, 9.4, 2.2], [48.62, 12.6, 45.8], iron),
        cube("hoop_n_hi", [2.2, 22.2, -0.68], [45.8, 25.4, 0.88], iron),
        cube("hoop_s_hi", [2.2, 22.2, 47.12], [45.8, 25.4, 48.68], iron),
        cube("hoop_w_hi", [-0.68, 22.2, 2.2], [0.88, 25.4, 45.8], iron),
        cube("hoop_e_hi", [47.12, 22.2, 2.2], [48.68, 25.4, 45.8], iron),
        cube("vat_floor", [8.2, 15.85, 8.2], [39.8, 17.15, 39.8], kettle),
        cube("rake_shaft", [22.9, 17.05, 17.8], [25.1, 36.4, 20.0], iron),
        cube("rake_arm", [15.6, 20.15, 17.15], [32.4, 21.65, 20.65], iron),
        cube("rake_tine_l", [16.3, 17.2, 17.55], [18.1, 20.25, 20.25], steel),
        cube("rake_tine_c", [22.95, 17.2, 17.45], [25.05, 20.25, 20.35], steel),
        cube("rake_tine_r", [29.9, 17.2, 17.55], [31.7, 20.25, 20.25], steel),
        cube("lid_pad", [13.6, 47.92, 13.6], [34.4, 49.35, 34.4], siding),
        cube("lid_vent", [19.6, 49.15, 19.6], [28.4, 52.35, 28.4], siding),
        cube("lid_cap", [20.6, 52.15, 20.6], [27.4, 53.45, 27.4], iron),
        cube("foot_nw", [-0.45, -1.15, -0.45], [4.15, 0.55, 4.15], siding),
        cube("foot_ne", [43.85, -1.15, -0.45], [48.45, 0.55, 4.15], siding),
        cube("foot_sw", [-0.45, -1.15, 43.85], [4.15, 0.55, 48.45], siding),
        cube("foot_se", [43.85, -1.15, 43.85], [48.45, 0.55, 48.45], siding),
    ]
    data["elements"] = kept + extra
    DST.parent.mkdir(parents=True, exist_ok=True)
    DST.write_text(json.dumps(data), encoding="utf-8")
    print(f"wrote {DST} cubes={len(data['elements'])}")


if __name__ == "__main__":
    main()
