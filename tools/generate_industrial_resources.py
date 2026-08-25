"""Write Phase 6 generated JSON, textures, and the industrial GameTest pad."""

from __future__ import annotations

import gzip
import json
import struct
from pathlib import Path

from generate_textures import BLOCK_DIR, canvas, put, rect, write_png

ROOT = Path(__file__).resolve().parents[1]
GEN = ROOT / "minecraft-common/src/generated/resources"
STRUCTURE = ROOT / "platform-forge-1.19.2/src/main/resources/data/alcoholic/structures/industrial_pad.nbt"

SIMPLE_BLOCKS = [
    "industrial_casing",
    "machine_window",
    "access_hatch",
    "fluid_port",
    "item_port",
    "kinetic_port",
    "industrial_vat_controller",
    "industrial_tank_controller",
]
PARTS = [
    "industrial_casing",
    "machine_window",
    "access_hatch",
    "fluid_port",
    "item_port",
    "kinetic_port",
]
LANG_EN = {
    "block.alcoholic.industrial_casing": "Industrial Casing",
    "block.alcoholic.machine_window": "Machine Window",
    "block.alcoholic.access_hatch": "Access Hatch",
    "block.alcoholic.fluid_port": "Fluid Port",
    "block.alcoholic.item_port": "Item Port",
    "block.alcoholic.kinetic_port": "Kinetic Port",
    "block.alcoholic.industrial_press_controller": "Industrial Press Controller",
    "block.alcoholic.industrial_vat_controller": "Industrial Fermentation Vat Controller",
    "block.alcoholic.industrial_tank_controller": "Industrial Storage Tank Controller",
    "message.alcoholic.port.mode": "Port mode: %s",
    "death.attack.alcoholic.industrial_press": "%1$s was crushed in an industrial press",
}
LANG_FR = {
    "block.alcoholic.industrial_casing": "Revêtement industriel",
    "block.alcoholic.machine_window": "Hublot de machine",
    "block.alcoholic.access_hatch": "Trappe d'accès",
    "block.alcoholic.fluid_port": "Port fluide",
    "block.alcoholic.item_port": "Port d'objets",
    "block.alcoholic.kinetic_port": "Port cinétique",
    "block.alcoholic.industrial_press_controller": "Contrôleur de pressoir industriel",
    "block.alcoholic.industrial_vat_controller": "Contrôleur de cuve de fermentation industrielle",
    "block.alcoholic.industrial_tank_controller": "Contrôleur de réservoir de stockage industriel",
    "message.alcoholic.port.mode": "Mode du port : %s",
    "death.attack.alcoholic.industrial_press": "%1$s a été écrasé dans un pressoir industriel",
}


def write_json(path: Path, payload) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def cube(name: str) -> None:
    write_json(
        GEN / f"assets/alcoholic/blockstates/{name}.json",
        {"variants": {"": {"model": f"alcoholic:block/{name}"}}},
    )
    write_json(
        GEN / f"assets/alcoholic/models/block/{name}.json",
        {"parent": "minecraft:block/cube_all", "textures": {"all": f"alcoholic:block/{name}"}},
    )
    write_json(
        GEN / f"assets/alcoholic/models/item/{name}.json",
        {"parent": f"alcoholic:block/{name}"},
    )


def self_loot(name: str) -> None:
    write_json(
        GEN / f"data/alcoholic/loot_tables/blocks/{name}.json",
        {
            "type": "minecraft:block",
            "pools": [
                {
                    "rolls": 1,
                    "entries": [{"type": "minecraft:item", "name": f"alcoholic:{name}"}],
                    "conditions": [{"condition": "minecraft:survives_explosion"}],
                }
            ],
        },
    )


def empty_loot(name: str) -> None:
    write_json(GEN / f"data/alcoholic/loot_tables/blocks/{name}.json", {"type": "minecraft:block", "pools": []})


def block_tag(name: str, values: list[str]) -> None:
    write_json(GEN / f"data/alcoholic/tags/blocks/{name}.json", {"replace": False, "values": values})


def shaped(name: str, pattern: list[str], key: dict, count: int = 1) -> None:
    write_json(
        GEN / f"data/alcoholic/recipes/{name}.json",
        {
            "type": "minecraft:crafting_shaped",
            "pattern": pattern,
            "key": {letter: {"item": item} for letter, item in key.items()},
            "result": {"item": f"alcoholic:{name}", "count": count},
        },
    )


def steel_texture(accent) -> list:
    image = canvas()
    dark = (48, 52, 58, 255)
    mid = (92, 98, 108, 255)
    light = (150, 156, 166, 255)
    rect(image, 0, 0, 15, 15, mid)
    for x in range(0, 16, 4):
        rect(image, x, 0, x, 15, dark)
    for y in range(0, 16, 4):
        rect(image, 0, y, 15, y, dark)
    rect(image, 1, 1, 2, 2, light)
    rect(image, 12, 12, 14, 14, accent)
    return image


def window_texture() -> list:
    image = canvas()
    frame = (70, 76, 84, 255)
    glass = (120, 180, 200, 140)
    rect(image, 0, 0, 15, 15, frame)
    rect(image, 2, 2, 13, 13, glass)
    return image


def name(value: str) -> bytes:
    encoded = value.encode("utf-8")
    return struct.pack(">H", len(encoded)) + encoded


def tag_int(tag_name: str, value: int) -> bytes:
    return b"\x03" + name(tag_name) + struct.pack(">i", value)


def tag_list(tag_name: str, element_type: int, values: list[bytes]) -> bytes:
    return b"\x09" + name(tag_name) + bytes([element_type]) + struct.pack(">i", len(values)) + b"".join(values)


def tag_string(tag_name: str, value: str) -> bytes:
    encoded = value.encode("utf-8")
    return b"\x08" + name(tag_name) + struct.pack(">H", len(encoded)) + encoded


def write_pad() -> None:
    palette_entry = tag_string("Name", "minecraft:air") + b"\x00"
    root = (
        b"\x0a\x00\x00"
        + tag_int("DataVersion", 3120)
        + tag_list("size", 3, [struct.pack(">i", value) for value in (16, 18, 16)])
        + tag_list("entities", 10, [])
        + tag_list("blocks", 10, [])
        + tag_list("palette", 10, [palette_entry])
        + b"\x00"
    )
    STRUCTURE.parent.mkdir(parents=True, exist_ok=True)
    STRUCTURE.write_bytes(gzip.compress(root, compresslevel=9, mtime=0))


def merge_lang(filename: str, extra: dict[str, str]) -> None:
    path = GEN / "assets/alcoholic/lang" / filename
    data = json.loads(path.read_text(encoding="utf-8"))
    data.update(extra)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def main() -> None:
    for name in SIMPLE_BLOCKS:
        cube(name)
    write_json(
        GEN / "assets/alcoholic/blockstates/industrial_press_controller.json",
        {
            "variants": {
                "formed=false": {"model": "alcoholic:block/industrial_press_controller"},
                "formed=true": {"model": "alcoholic:block/industrial_press_controller_formed"},
            }
        },
    )
    write_json(
        GEN / "assets/alcoholic/models/block/industrial_press_controller.json",
        {
            "parent": "minecraft:block/cube_all",
            "textures": {"all": "alcoholic:block/industrial_press_controller"},
        },
    )
    write_json(
        GEN / "assets/alcoholic/models/block/industrial_press_controller_formed.json",
        {
            "parent": "minecraft:block/cube_all",
            "textures": {"all": "alcoholic:block/industrial_press_controller_formed"},
        },
    )
    write_json(
        GEN / "assets/alcoholic/models/item/industrial_press_controller.json",
        {"parent": "alcoholic:block/industrial_press_controller"},
    )

    accents = {
        "industrial_casing": (120, 120, 128, 255),
        "access_hatch": (180, 140, 60, 255),
        "fluid_port": (40, 90, 180, 255),
        "item_port": (180, 120, 40, 255),
        "kinetic_port": (40, 160, 90, 255),
        "industrial_vat_controller": (80, 140, 70, 255),
        "industrial_tank_controller": (70, 90, 140, 255),
        "industrial_press_controller": (160, 70, 70, 255),
        "industrial_press_controller_formed": (210, 90, 70, 255),
    }
    for name, accent in accents.items():
        write_png(BLOCK_DIR / f"{name}.png", steel_texture(accent))
    write_png(BLOCK_DIR / "machine_window.png", window_texture())

    block_tag("industrial_tank_casing", ["alcoholic:industrial_casing", "alcoholic:access_hatch"])
    block_tag("fermenter_casing", ["alcoholic:industrial_casing", "alcoholic:access_hatch"])
    block_tag("pressure_safe_casing", ["alcoholic:industrial_casing", "alcoholic:access_hatch"])
    block_tag("valid_machine_windows", ["alcoholic:machine_window"])
    block_tag("industrial_ports", ["alcoholic:fluid_port", "alcoholic:item_port", "alcoholic:kinetic_port"])

    for part in PARTS:
        self_loot(part)
    empty_loot("industrial_press_controller")
    empty_loot("industrial_vat_controller")
    empty_loot("industrial_tank_controller")

    shaped("industrial_casing", ["III", "I I", "III"], {"I": "minecraft:iron_ingot"}, 4)
    shaped("machine_window", ["IGI", "G G", "IGI"], {"I": "minecraft:iron_ingot", "G": "minecraft:glass"}, 4)
    shaped("access_hatch", [" I ", "IHI", " I "], {"I": "minecraft:iron_ingot", "H": "minecraft:iron_trapdoor"})
    shaped("fluid_port", [" I ", "IBI", " I "], {"I": "minecraft:iron_ingot", "B": "minecraft:bucket"})
    shaped("item_port", [" I ", "IHI", " I "], {"I": "minecraft:iron_ingot", "H": "minecraft:hopper"})
    shaped("kinetic_port", [" I ", "ISI", " I "], {"I": "minecraft:iron_ingot", "S": "minecraft:iron_nugget"})
    shaped(
        "industrial_press_controller",
        ["IPI", "ICI", "III"],
        {"I": "minecraft:iron_ingot", "P": "alcoholic:artisanal_press", "C": "alcoholic:industrial_casing"},
    )
    shaped(
        "industrial_vat_controller",
        ["IFI", "ICI", "III"],
        {"I": "minecraft:iron_ingot", "F": "alcoholic:artisanal_fermenter", "C": "alcoholic:industrial_casing"},
    )
    shaped(
        "industrial_tank_controller",
        ["IBI", "ICI", "III"],
        {"I": "minecraft:iron_ingot", "B": "minecraft:bucket", "C": "alcoholic:industrial_casing"},
    )

    write_json(
        GEN / "data/alcoholic/alcoholic/machines/industrial_press.json",
        {
            "id": "alcoholic:industrial_press",
            "kind": "press",
            "process": "alcoholic:press",
            "min_exterior": {"x": 3, "y": 4, "z": 3},
            "max_exterior": {"x": 7, "y": 8, "z": 7},
            "required_controllers": 1,
            "casing_tags": ["alcoholic:pressure_safe_casing"],
            "window_tags": ["alcoholic:valid_machine_windows"],
            "port_tags": ["alcoholic:industrial_ports"],
            "required_ports": ["kinetic_port"],
            "hollow_interior": True,
            "capacity_per_internal_block": 4000,
            "controller": "alcoholic:industrial_press_controller",
            "modifiers": {"yield": 1.05, "speed": 2.0, "thermal_stability": 1.0, "max_batch_units": 2147483647},
            "kinetic": {"min_rpm": 16, "max_rpm": 256, "required": True},
        },
    )
    write_json(
        GEN / "data/alcoholic/alcoholic/machines/industrial_fermentation_vat.json",
        {
            "id": "alcoholic:industrial_fermentation_vat",
            "kind": "ferment",
            "process": "alcoholic:ferment",
            "min_exterior": {"x": 3, "y": 4, "z": 3},
            "max_exterior": {"x": 9, "y": 16, "z": 9},
            "required_controllers": 1,
            "casing_tags": ["alcoholic:fermenter_casing"],
            "window_tags": ["alcoholic:valid_machine_windows"],
            "port_tags": ["alcoholic:industrial_ports"],
            "required_ports": [],
            "hollow_interior": True,
            "capacity_per_internal_block": 8000,
            "controller": "alcoholic:industrial_vat_controller",
            "modifiers": {"yield": 1.0, "speed": 1.0, "thermal_stability": 4.0, "max_batch_units": 1},
        },
    )
    write_json(
        GEN / "data/alcoholic/alcoholic/machines/industrial_storage_tank.json",
        {
            "id": "alcoholic:industrial_storage_tank",
            "kind": "storage",
            "min_exterior": {"x": 3, "y": 4, "z": 3},
            "max_exterior": {"x": 9, "y": 16, "z": 9},
            "required_controllers": 1,
            "casing_tags": ["alcoholic:industrial_tank_casing"],
            "window_tags": ["alcoholic:valid_machine_windows"],
            "port_tags": ["alcoholic:industrial_ports"],
            "required_ports": [],
            "hollow_interior": True,
            "capacity_per_internal_block": 16000,
            "controller": "alcoholic:industrial_tank_controller",
        },
    )

    write_json(
        GEN / "data/minecraft/tags/blocks/mineable/pickaxe.json",
        {
            "replace": False,
            "values": [
                "alcoholic:industrial_casing",
                "alcoholic:machine_window",
                "alcoholic:access_hatch",
                "alcoholic:fluid_port",
                "alcoholic:item_port",
                "alcoholic:kinetic_port",
                "alcoholic:industrial_press_controller",
                "alcoholic:industrial_vat_controller",
                "alcoholic:industrial_tank_controller",
            ],
        },
    )
    merge_lang("en_us.json", LANG_EN)
    merge_lang("fr_fr.json", LANG_FR)
    write_pad()


if __name__ == "__main__":
    main()
