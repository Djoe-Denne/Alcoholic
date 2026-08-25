"""Generate deterministic Phase 2 placeholder textures using only the stdlib."""

from __future__ import annotations

import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BLOCK_DIR = ROOT / "minecraft-common/src/main/resources/assets/alcoholic/textures/block"
ITEM_DIR = ROOT / "minecraft-common/src/main/resources/assets/alcoholic/textures/item"
SIZE = 16
TRANSPARENT = (0, 0, 0, 0)
STAGES = (
    "planted",
    "establishing",
    "vegetative",
    "flowering",
    "green_fruit",
    "ripening",
    "harvest_ready",
    "dormant",
)


def canvas() -> list[list[tuple[int, int, int, int]]]:
    return [[TRANSPARENT for _ in range(SIZE)] for _ in range(SIZE)]


def put(image, x: int, y: int, color) -> None:
    if 0 <= x < SIZE and 0 <= y < SIZE:
        image[y][x] = color


def rect(image, x0: int, y0: int, x1: int, y1: int, color) -> None:
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            put(image, x, y, color)


def write_png(path: Path, image) -> None:
    raw = b"".join(
        b"\x00" + b"".join(bytes(pixel) for pixel in row)
        for row in image
    )

    def chunk(kind: bytes, payload: bytes) -> bytes:
        return (
            struct.pack(">I", len(payload))
            + kind
            + payload
            + struct.pack(">I", zlib.crc32(kind + payload) & 0xFFFFFFFF)
        )

    png = (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", struct.pack(">IIBBBBB", SIZE, SIZE, 8, 6, 0, 0, 0))
        + chunk(b"IDAT", zlib.compress(raw, 9))
        + chunk(b"IEND", b"")
    )
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(png)


def vine_texture(stage: str, grape_color) -> list:
    image = canvas()
    stem = (101, 73, 39, 255)
    stem_light = (132, 92, 49, 255)
    leaf = (45, 126, 55, 255)
    leaf_light = (67, 157, 66, 255)
    stage_index = STAGES.index(stage)
    top_by_stage = {
        "planted": 12,
        "establishing": 9,
        "vegetative": 5,
        "flowering": 2,
        "green_fruit": 1,
        "ripening": 1,
        "harvest_ready": 1,
        "dormant": 2,
    }
    top = top_by_stage[stage]
    rect(image, 7, max(1, top), 8, 15, stem)
    for y in range(max(2, top + 2), 14, 4):
        put(image, 6, y, stem_light)
        put(image, 9, y - 1, stem_light)
        if stage == "dormant":
            put(image, 5, y - 1, stem)
            put(image, 10, y, stem)
            continue
        for x, dy in ((5, 0), (6, -1), (9, -1), (10, 0)):
            put(image, x, y + dy, leaf)
        put(image, 5, y - 1, leaf_light)
        put(image, 10, y - 1, leaf_light)

    if stage == "flowering":
        flower = (241, 220, 188, 255)
        for x, y in ((4, 5), (6, 8), (10, 4), (11, 9), (5, 12)):
            put(image, x, y, flower)
            put(image, x + 1, y, (255, 245, 221, 255))

    if stage in {"green_fruit", "ripening", "harvest_ready"}:
        berries = [(5, 8), (6, 9), (5, 10), (10, 6), (9, 7), (10, 8)]
        if stage == "harvest_ready":
            berries += [(6, 11), (9, 10), (10, 11)]
        green = (91, 151, 69, 255)
        fruit_color = {
            "green_fruit": green,
            "ripening": tuple(
                (green[index] + grape_color[index]) // 2 for index in range(3)
            )
            + (255,),
            "harvest_ready": grape_color,
        }[stage]
        highlight = tuple(min(255, c + 30) for c in fruit_color[:3]) + (255,)
        for index, (x, y) in enumerate(berries):
            color = grape_color if stage == "ripening" and index % 2 else fruit_color
            put(image, x, y, color)
            put(image, x - 1, y - 1, highlight)

    # A fixed stage marker makes adjacent placeholders easy to distinguish.
    for marker in range(stage_index + 1):
        put(image, marker, 15, (214, 182, 87, 255))
    return image


def grape_item(grape_color) -> list:
    image = canvas()
    stem = (91, 72, 35, 255)
    leaf = (55, 145, 63, 255)
    rect(image, 7, 1, 8, 5, stem)
    rect(image, 4, 3, 7, 5, leaf)
    berries = [
        (6, 5), (8, 5), (10, 5),
        (5, 7), (7, 7), (9, 7), (11, 7),
        (6, 9), (8, 9), (10, 9),
        (7, 11), (9, 11), (8, 13),
    ]
    highlight = tuple(min(255, c + 38) for c in grape_color[:3]) + (255,)
    for x, y in berries:
        rect(image, x - 1, y - 1, x + 1, y + 1, grape_color)
        put(image, x - 1, y - 1, highlight)
    return image


def cutting_item(grape_color) -> list:
    image = canvas()
    stem = (112, 79, 41, 255)
    leaf = (49, 137, 57, 255)
    for index in range(11):
        put(image, 4 + index // 2, 14 - index, stem)
    rect(image, 5, 7, 8, 9, leaf)
    put(image, 4, 8, leaf)
    put(image, 9, 8, leaf)
    put(image, 10, 5, grape_color)
    return image


def post_texture(end_post: bool) -> list:
    image = canvas()
    dark = (78, 52, 31, 255)
    wood = (119, 82, 47, 255)
    light = (151, 108, 62, 255)
    rect(image, 0, 0, 15, 15, wood)
    for x in range(1, 16, 4):
        rect(image, x, 0, x + 1, 15, dark)
        rect(image, x + 2, 0, x + 2, 15, light)
    if end_post:
        rect(image, 0, 2, 15, 4, dark)
        rect(image, 0, 11, 15, 13, dark)
        for index in range(16):
            put(image, index, index, light)
    return image


def wire_texture() -> list:
    image = canvas()
    dark = (68, 72, 76, 255)
    steel = (142, 151, 158, 255)
    for x in range(SIZE):
        put(image, x, 7, dark)
        put(image, x, 8, steel if x % 3 else dark)
    return image


def spool_item() -> list:
    image = canvas()
    rim = (91, 62, 35, 255)
    wood = (150, 103, 55, 255)
    wire = (132, 144, 151, 255)
    rect(image, 3, 2, 12, 4, rim)
    rect(image, 3, 11, 12, 13, rim)
    rect(image, 5, 4, 10, 11, wood)
    for y in range(5, 11, 2):
        rect(image, 4, y, 11, y, wire)
    return image


def shears_item() -> list:
    image = canvas()
    steel = (181, 190, 196, 255)
    shine = (229, 235, 238, 255)
    handle = (89, 48, 36, 255)
    for index in range(10):
        put(image, 3 + index, 2 + index, steel)
        put(image, 12 - index, 2 + index, steel)
        if index < 6:
            put(image, 4 + index, 2 + index, shine)
    rect(image, 2, 11, 5, 14, handle)
    rect(image, 10, 11, 13, 14, handle)
    rect(image, 3, 12, 4, 13, TRANSPARENT)
    rect(image, 11, 12, 12, 13, TRANSPARENT)
    return image


def press_texture() -> list:
    image = canvas()
    wood = (132, 90, 48, 255)
    dark = (88, 58, 30, 255)
    iron = (156, 162, 168, 255)
    rect(image, 0, 8, 15, 15, wood)
    rect(image, 2, 4, 13, 10, dark)
    rect(image, 6, 0, 9, 8, iron)
    rect(image, 4, 0, 11, 2, iron)
    return image


def barrel_texture() -> list:
    image = canvas()
    wood = (140, 96, 52, 255)
    dark = (92, 60, 30, 255)
    band = (70, 70, 74, 255)
    rect(image, 2, 1, 13, 14, wood)
    rect(image, 2, 3, 13, 4, band)
    rect(image, 2, 11, 13, 12, band)
    rect(image, 1, 5, 2, 10, dark)
    rect(image, 13, 5, 14, 10, dark)
    return image


def crock_texture() -> list:
    image = canvas()
    clay = (168, 112, 72, 255)
    rim = (120, 78, 48, 255)
    rect(image, 3, 4, 12, 14, clay)
    rect(image, 2, 3, 13, 5, rim)
    return image


def bottle_item(filled: bool) -> list:
    image = canvas()
    glass = (186, 214, 220, 255)
    neck = (150, 176, 182, 255)
    fluid = (92, 24, 40, 255)
    rect(image, 6, 1, 9, 4, neck)
    rect(image, 5, 4, 10, 14, glass)
    if filled:
        rect(image, 6, 7, 9, 13, fluid)
    return image


def fermenter_texture() -> list:
    image = canvas()
    wood = (124, 82, 44, 255)
    band = (72, 48, 28, 255)
    rect(image, 1, 1, 14, 14, wood)
    rect(image, 1, 4, 14, 5, band)
    rect(image, 1, 10, 14, 11, band)
    return image


def yeast_item() -> list:
    image = canvas()
    beige = (214, 196, 132, 255)
    dark = (176, 148, 82, 255)
    rect(image, 4, 5, 11, 12, beige)
    put(image, 6, 7, dark)
    put(image, 9, 9, dark)
    put(image, 8, 6, dark)
    return image


def pomace_item() -> list:
    image = canvas()
    pulp = (92, 48, 58, 255)
    seed = (48, 32, 22, 255)
    rect(image, 3, 6, 12, 13, pulp)
    put(image, 5, 8, seed)
    put(image, 8, 10, seed)
    put(image, 10, 8, seed)
    return image


def bucket_item(fluid) -> list:
    image = canvas()
    iron = (150, 156, 162, 255)
    dark = (92, 98, 104, 255)
    rect(image, 4, 4, 11, 14, iron)
    rect(image, 5, 6, 10, 12, fluid)
    rect(image, 4, 3, 11, 4, dark)
    return image


def main() -> None:
    colors = {
        "red": (126, 36, 58, 255),
        "white": (205, 208, 115, 255),
    }
    for name, color in colors.items():
        for legacy_stage in range(5):
            legacy_path = BLOCK_DIR / f"{name}_grapevine_stage{legacy_stage}.png"
            if legacy_path.exists():
                legacy_path.unlink()
        for stage in STAGES:
            write_png(
                BLOCK_DIR / f"{name}_grapevine_{stage}.png",
                vine_texture(stage, color),
            )
        write_png(ITEM_DIR / f"{name}_grapes.png", grape_item(color))
        write_png(ITEM_DIR / f"{name}_grape_cutting.png", cutting_item(color))
    write_png(BLOCK_DIR / "vineyard_post.png", post_texture(False))
    write_png(BLOCK_DIR / "end_post.png", post_texture(True))
    write_png(BLOCK_DIR / "trellis_wire.png", wire_texture())
    write_png(ITEM_DIR / "trellis_spool.png", spool_item())
    write_png(ITEM_DIR / "pruning_shears.png", shears_item())
    write_png(BLOCK_DIR / "artisanal_press.png", press_texture())
    write_png(BLOCK_DIR / "artisanal_fermenter.png", fermenter_texture())
    write_png(ITEM_DIR / "yeast.png", yeast_item())
    write_png(ITEM_DIR / "grape_pomace.png", pomace_item())
    write_png(ITEM_DIR / "red_grape_must_bucket.png", bucket_item((122, 36, 58, 255)))
    write_png(ITEM_DIR / "white_grape_must_bucket.png", bucket_item((230, 213, 106, 255)))
    write_png(ITEM_DIR / "young_red_wine_bucket.png", bucket_item((90, 18, 38, 255)))
    write_png(ITEM_DIR / "young_white_wine_bucket.png", bucket_item((232, 211, 107, 255)))
    write_png(ITEM_DIR / "red_wine_bucket.png", bucket_item((74, 14, 28, 255)))
    write_png(ITEM_DIR / "white_wine_bucket.png", bucket_item((230, 200, 90, 255)))
    write_png(BLOCK_DIR / "oak_barrel.png", barrel_texture())
    write_png(BLOCK_DIR / "artisanal_blending_crock.png", crock_texture())
    write_png(ITEM_DIR / "empty_bottle.png", bottle_item(False))
    write_png(ITEM_DIR / "beverage_bottle.png", bottle_item(True))


if __name__ == "__main__":
    main()
