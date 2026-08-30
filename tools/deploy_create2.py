"""Copy the remapped Alcoholic jar (and optional 128 pack) to Create 2 Mekanism."""

from __future__ import annotations

import argparse
import shutil
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
INSTANCE = Path(r"C:\Users\djden\curseforge\minecraft\Instances\Create 2 Mekanism")
LIBS = ROOT / "platform-forge-1.19.2" / "build" / "libs"
PACK_128 = ROOT / "resourcepacks" / "Alcoholic-128x"
JAR_PREFIX = "alcoholic-forge-"


def find_jar() -> Path:
    jars = [
        path
        for path in LIBS.glob(f"{JAR_PREFIX}*.jar")
        if "sources" not in path.name and "javadoc" not in path.name
    ]
    if not jars:
        raise SystemExit(
            f"No remapped jar in {LIBS}. Run .\\gradlew :platform-forge-1.19.2:reobfJar first."
        )
    return max(jars, key=lambda path: path.stat().st_mtime)


def copy_jar(jar: Path) -> Path:
    mods = INSTANCE / "mods"
    if not mods.is_dir():
        raise SystemExit(f"Missing instance mods folder: {mods}")
    for stale in mods.glob(f"{JAR_PREFIX}*.jar"):
        try:
            stale.unlink()
        except OSError as error:
            raise SystemExit(
                f"Cannot replace {stale.name} ({error}). Quit Minecraft and retry."
            ) from error
    dest = mods / jar.name
    try:
        shutil.copy2(jar, dest)
    except OSError as error:
        raise SystemExit(
            f"Cannot write {dest} ({error}). Quit Minecraft and retry."
        ) from error
    return dest


def zip_pack(source: Path, dest: Path) -> None:
    if not (source / "pack.mcmeta").is_file():
        raise SystemExit(f"Missing pack.mcmeta at {source}")
    dest.parent.mkdir(parents=True, exist_ok=True)
    temporary = dest.with_suffix(".zip.partial")
    try:
        with zipfile.ZipFile(temporary, "w", compression=zipfile.ZIP_DEFLATED) as archive:
            for path in sorted(source.rglob("*")):
                if not path.is_file():
                    continue
                archive.write(path, path.relative_to(source).as_posix())
        temporary.replace(dest)
    except OSError as error:
        temporary.unlink(missing_ok=True)
        raise SystemExit(
            f"Cannot write {dest} ({error}). Quit Minecraft and retry."
        ) from error


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--with-128",
        action="store_true",
        help="Rebuild Alcoholic-128x.zip from resourcepacks/Alcoholic-128x",
    )
    args = parser.parse_args()

    jar = find_jar()
    installed = copy_jar(jar)
    print(f"jar {jar.name} -> {installed} ({installed.stat().st_size} bytes)")

    if args.with_128:
        dest = INSTANCE / "resourcepacks" / "Alcoholic-128x.zip"
        zip_pack(PACK_128, dest)
        print(f"pack Alcoholic-128x.zip -> {dest} ({dest.stat().st_size} bytes)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
