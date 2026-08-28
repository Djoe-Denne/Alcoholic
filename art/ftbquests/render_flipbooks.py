#!/usr/bin/env python3
"""Paint six 128x128 x 6-frame FTB quest flipbooks and copy them into the JAR."""

from __future__ import annotations

import shutil
import struct
import zlib
from pathlib import Path

SIZE = 128
FRAMES = 6
OAK = (108, 69, 35)
OAK_LIGHT = (148, 98, 54)
OAK_DARK = (72, 44, 22)
OAK_FOOT = (95, 61, 31)
IRON = (168, 168, 176)
IRON_DARK = (96, 96, 104)
GRAPE = (120, 36, 72)
MUST = (148, 48, 80)
WORT = (196, 156, 64)
WINE = (88, 20, 40)
WINE_DEEP = (56, 12, 28)
FOAM = (232, 220, 196)
PARCHMENT = (214, 190, 148)
PARCHMENT_DARK = (186, 158, 112)
MAGMA = (220, 80, 24)
MAGMA_CORE = (255, 180, 64)
BUBBLE = (248, 248, 252)
GLASS = (196, 220, 228)
GLASS_DARK = (120, 148, 160)
GRAIN = (188, 148, 72)
SHADOW = (40, 28, 18)

HERE = Path(__file__).resolve().parent
JAR = HERE.parents[1] / (
    "minecraft-common/src/main/resources/assets/alcoholic/textures/item/ftbquests"
)


def clamp(value: int) -> int:
    return 0 if value < 0 else 255 if value > 255 else value


def mix(a: tuple[int, int, int], b: tuple[int, int, int], t: float) -> tuple[int, int, int]:
    return (
        clamp(int(a[0] + (b[0] - a[0]) * t)),
        clamp(int(a[1] + (b[1] - a[1]) * t)),
        clamp(int(a[2] + (b[2] - a[2]) * t)),
    )


class Canvas:
    def __init__(self) -> None:
        self.px = [list(PARCHMENT) for _ in range(SIZE * SIZE)]

    def _idx(self, x: int, y: int) -> int | None:
        if 0 <= x < SIZE and 0 <= y < SIZE:
            return y * SIZE + x
        return None

    def put(self, x: int, y: int, color: tuple[int, int, int], alpha: float = 1.0) -> None:
        i = self._idx(int(x), int(y))
        if i is None:
            return
        if alpha >= 1.0:
            self.px[i] = color
            return
        self.px[i] = mix(self.px[i], color, alpha)

    def fill(self, color: tuple[int, int, int]) -> None:
        for i in range(len(self.px)):
            self.px[i] = color

    def rect(
        self,
        x0: int,
        y0: int,
        x1: int,
        y1: int,
        color: tuple[int, int, int],
        alpha: float = 1.0,
    ) -> None:
        for y in range(y0, y1):
            for x in range(x0, x1):
                self.put(x, y, color, alpha)

    def oval(
        self,
        cx: int,
        cy: int,
        rx: int,
        ry: int,
        color: tuple[int, int, int],
        alpha: float = 1.0,
    ) -> None:
        for y in range(cy - ry, cy + ry + 1):
            for x in range(cx - rx, cx + rx + 1):
                nx = (x - cx) / max(rx, 1)
                ny = (y - cy) / max(ry, 1)
                if nx * nx + ny * ny <= 1.0:
                    self.put(x, y, color, alpha)

    def ring(
        self,
        cx: int,
        cy: int,
        rx: int,
        ry: int,
        thickness: int,
        color: tuple[int, int, int],
    ) -> None:
        self.oval(cx, cy, rx, ry, color)
        inner = max(1, rx - thickness)
        inner_y = max(1, ry - thickness)
        self.oval(cx, cy, inner, inner_y, PARCHMENT)

    def line(self, x0: int, y0: int, x1: int, y1: int, color: tuple[int, int, int], w: int = 1) -> None:
        steps = max(abs(x1 - x0), abs(y1 - y0), 1)
        for i in range(steps + 1):
            t = i / steps
            x = int(x0 + (x1 - x0) * t)
            y = int(y0 + (y1 - y0) * t)
            for dy in range(-w + 1, w):
                for dx in range(-w + 1, w):
                    self.put(x + dx, y + dy, color)

    def grain(self, x0: int, y0: int, x1: int, y1: int, vertical: bool) -> None:
        for y in range(y0, y1):
            for x in range(x0, x1):
                wave = (x * 3 + y) if vertical else (y * 3 + x)
                tone = 0.12 if (wave % 9) < 2 else 0.0
                knot = 0.2 if ((x + y * 5) % 37 == 0) else 0.0
                self.put(x, y, mix(OAK, OAK_DARK if tone or knot else OAK_LIGHT, tone + knot))

    def workshop_floor(self) -> None:
        self.fill(PARCHMENT)
        for y in range(SIZE):
            shade = 0.08 if (y // 16) % 2 == 0 else 0.0
            for x in range(SIZE):
                if (x + y) % 23 == 0:
                    self.put(x, y, PARCHMENT_DARK, 0.25)
                elif shade:
                    self.put(x, y, PARCHMENT_DARK, shade)

    def raw(self) -> list[tuple[int, int, int]]:
        return self.px


def write_png(path: Path, frames: list[Canvas]) -> None:
    width = SIZE
    height = SIZE * len(frames)
    raw = bytearray()
    for frame in frames:
        pixels = frame.raw()
        for y in range(SIZE):
            raw.append(0)
            for x in range(SIZE):
                r, g, b = pixels[y * SIZE + x]
                raw.extend((r, g, b))
    compressed = zlib.compress(bytes(raw), 9)
    ihdr = struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0)

    def chunk(tag: bytes, data: bytes) -> bytes:
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)

    path.write_bytes(
        b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", compressed) + chunk(b"IEND", b"")
    )


def write_mcmeta(path: Path) -> None:
    path.write_text(
        '{\n'
        '  "animation": {\n'
        '    "frametime": 6,\n'
        '    "interpolate": false,\n'
        '    "frames": [0, 1, 2, 3, 4, 5, 4, 3, 2, 1]\n'
        '  }\n'
        '}\n',
        encoding="utf-8",
    )


def press_frame(i: int) -> Canvas:
    c = Canvas()
    c.workshop_floor()
    screw = 28 + int(i * 7)
    juice = 4 + i
    c.rect(30, 18, 98, 26, OAK_DARK)
    c.grain(30, 18, 98, 26, False)
    c.rect(36, 26, 46, 102, OAK)
    c.grain(36, 26, 46, 102, True)
    c.rect(82, 26, 92, 102, OAK)
    c.grain(82, 26, 92, 102, True)
    c.rect(32, 98, 96, 110, OAK_FOOT)
    c.rect(48, 88, 80, 100, GRAPE)
    c.oval(64, 92, 18, 8, GRAPE)
    if juice:
        c.rect(50, 100 - juice, 78, 100, MUST)
    c.rect(58, 22, 70, screw + 10, IRON_DARK)
    c.rect(52, screw, 76, screw + 10, IRON)
    c.rect(48, screw + 10, 80, screw + 16, OAK_LIGHT)
    c.oval(64, 22, 10, 6, IRON)
    return c


def mash_frame(i: int) -> Canvas:
    c = Canvas()
    c.workshop_floor()
    glow = 0.35 + 0.1 * (i % 2)
    c.rect(44, 104, 84, 118, MAGMA)
    c.oval(64, 111, 22, 8, mix(MAGMA, MAGMA_CORE, glow))
    c.rect(38, 40, 90, 104, OAK)
    c.grain(38, 40, 90, 104, True)
    c.oval(64, 42, 28, 10, OAK_DARK)
    c.oval(64, 42, 22, 7, mix(WORT, GRAIN, 0.4))
    wort_top = 86 - i * 4
    c.rect(44, wort_top, 84, 98, WORT)
    c.oval(64, wort_top, 20, 6, mix(WORT, FOAM, 0.3))
    grain_y = 36 + i * 5
    c.oval(52, grain_y, 5, 3, GRAIN)
    c.oval(70, grain_y + 3, 4, 3, GRAIN)
    c.rect(40, 98, 88, 106, OAK_FOOT)
    return c


def fermenter_frame(i: int) -> Canvas:
    c = Canvas()
    c.workshop_floor()
    c.rect(34, 36, 94, 108, OAK)
    c.grain(34, 36, 94, 108, True)
    c.oval(64, 38, 32, 12, OAK_DARK)
    c.oval(64, 38, 26, 9, mix(WINE, MUST, 0.35))
    c.rect(42, 52, 86, 100, mix(WINE, MUST, 0.25))
    c.oval(64, 100, 22, 7, OAK_FOOT)
    bubbles = [(50, 70), (64, 62), (76, 74), (58, 84), (72, 56), (48, 90)]
    for n, (bx, by) in enumerate(bubbles):
        lift = (i * 6 + n * 9) % 40
        c.oval(bx, by - lift // 2, 3, 3, BUBBLE, 0.85)
    c.oval(64, 52, 20, 5, FOAM, 0.7)
    return c


def barrel_frame(i: int) -> Canvas:
    c = Canvas()
    c.workshop_floor()
    c.oval(64, 64, 40, 28, OAK)
    c.grain(28, 40, 100, 90, False)
    c.oval(64, 64, 40, 28, OAK, 0.15)
    for band_y in (48, 64, 80):
        c.rect(26, band_y, 102, band_y + 4, IRON_DARK)
    wine = mix(WINE, WINE_DEEP, i / 5)
    c.oval(64, 64, 18, 14, wine)
    c.oval(58, 58, 6, 4, FOAM, 0.35)
    ring = 10 + i * 2
    c.ring(108, 28, ring, ring, 2, mix(OAK_LIGHT, WINE, i / 5))
    return c


def crock_frame(i: int) -> Canvas:
    c = Canvas()
    c.workshop_floor()
    c.rect(18, 48, 50, 100, OAK)
    c.grain(18, 48, 50, 100, True)
    c.rect(78, 48, 110, 100, OAK)
    c.grain(78, 48, 110, 100, True)
    c.rect(48, 70, 80, 108, OAK_DARK)
    c.grain(48, 70, 80, 108, True)
    c.rect(22, 56, 46, 92, WINE)
    c.rect(82, 56, 106, 92, mix(WINE, MUST, 0.5))
    pour = i * 6
    c.line(48, 60, 58, 70 + pour // 2, WINE, 2)
    c.line(80, 60, 70, 70 + pour // 2, MUST, 2)
    blend_h = 8 + i * 4
    c.rect(54, 100 - blend_h, 74, 100, mix(WINE, MUST, 0.5))
    c.oval(64, 72, 12, 5, OAK_LIGHT)
    return c


def bottle_frame(i: int) -> Canvas:
    c = Canvas()
    c.workshop_floor()
    c.rect(22, 36, 62, 104, OAK)
    c.grain(22, 36, 62, 104, True)
    vat_top = 50 + i * 7
    c.rect(28, vat_top, 56, 98, WINE)
    c.rect(30, 40, 34, 100, mix(FOAM, GLASS, 0.4))
    c.rect(78, 48, 98, 104, GLASS)
    c.rect(84, 28, 92, 48, GLASS_DARK)
    fill = 14 + i * 12
    c.rect(82, 104 - fill, 94, 102, WINE)
    c.line(56, 50, 84, 40, WINE, 2)
    c.oval(88, 26, 6, 4, OAK_DARK)
    return c


def main() -> None:
    painters = {
        "press": press_frame,
        "mash_tun": mash_frame,
        "fermenter": fermenter_frame,
        "barrel": barrel_frame,
        "crock": crock_frame,
        "bottle": bottle_frame,
    }
    JAR.mkdir(parents=True, exist_ok=True)
    for name, painter in painters.items():
        frames = [painter(i) for i in range(FRAMES)]
        local_png = HERE / f"{name}.png"
        write_png(local_png, frames)
        write_mcmeta(HERE / f"{name}.png.mcmeta")
        shutil.copy2(local_png, JAR / f"{name}.png")
        shutil.copy2(HERE / f"{name}.png.mcmeta", JAR / f"{name}.png.mcmeta")
        print(f"wrote {name}.png ({SIZE}x{SIZE * FRAMES})")


if __name__ == "__main__":
    main()
