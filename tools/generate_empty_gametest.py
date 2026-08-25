"""Generate the deterministic empty structure used by Forge GameTests."""

from __future__ import annotations

import gzip
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = (
    ROOT
    / "platform-forge-1.19.2/src/main/resources/data/alcoholic/structures/empty.nbt"
)


def name(value: str) -> bytes:
    encoded = value.encode("utf-8")
    return struct.pack(">H", len(encoded)) + encoded


def tag_int(tag_name: str, value: int) -> bytes:
    return b"\x03" + name(tag_name) + struct.pack(">i", value)


def tag_list(tag_name: str, element_type: int, values: list[bytes]) -> bytes:
    return (
        b"\x09"
        + name(tag_name)
        + bytes([element_type])
        + struct.pack(">i", len(values))
        + b"".join(values)
    )


def tag_string(tag_name: str, value: str) -> bytes:
    encoded = value.encode("utf-8")
    return b"\x08" + name(tag_name) + struct.pack(">H", len(encoded)) + encoded


def main() -> None:
    palette_entry = tag_string("Name", "minecraft:air") + b"\x00"
    root = (
        b"\x0a\x00\x00"
        + tag_int("DataVersion", 3120)
        + tag_list("size", 3, [struct.pack(">i", value) for value in (3, 3, 3)])
        + tag_list("entities", 10, [])
        + tag_list("blocks", 10, [])
        + tag_list("palette", 10, [palette_entry])
        + b"\x00"
    )
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_bytes(gzip.compress(root, compresslevel=9, mtime=0))


if __name__ == "__main__":
    main()
