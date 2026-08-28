from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(r"c:/Users/djden/source/repos/Alcoholic")
CASING = ROOT / "art/blockbench/industrial_casing/textures/master-512/industrial_casing.png"
KETTLE = ROOT / "art/blockbench/brewing_kettle/textures/master-512/brewing_kettle.png"
DEST = ROOT / "art/blockbench/industrial_brewing_kettle/textures/master-512/industrial_brewing_kettle.png"

atlas = Image.open(CASING).convert("RGBA")
kettle = Image.open(KETTLE).convert("RGBA")
assert atlas.size == (512, 512)
assert kettle.size == (512, 512)
atlas.paste(kettle.crop((0, 0, 512, 384)), (0, 128))

# Steam occupies the old liquid-gauge tile (256,384).
# Never use a black field: Java faces stretch the whole tile, so a black
# backdrop reads as a missing texture on the chimney puffs.
steam = Image.new("RGBA", (128, 128), (118, 126, 136, 255))
draw = ImageDraw.Draw(steam)
for i, color in enumerate([(148, 154, 164), (186, 192, 200), (226, 230, 236), (246, 248, 250)]):
    inset = 10 + i * 12
    draw.ellipse((inset, inset + 8, 128 - inset, 128 - inset - 4), fill=color)
steam = steam.filter(ImageFilter.GaussianBlur(radius=1.2))
atlas.paste(steam, (256, 384))

# Orange boil glow occupies the mixed-copper tile leftover (128,384).
glow = Image.new("RGBA", (128, 128), (140, 52, 12, 255))
gdraw = ImageDraw.Draw(glow)
for i, color in enumerate([(168, 64, 14), (210, 92, 18), (240, 132, 32), (255, 176, 64)]):
    inset = 8 + i * 14
    gdraw.ellipse((inset, inset, 128 - inset, 128 - inset), fill=color)
glow = glow.filter(ImageFilter.GaussianBlur(radius=0.8))
atlas.paste(glow, (128, 384))

DEST.parent.mkdir(parents=True, exist_ok=True)
atlas.save(DEST)
print("wrote", DEST, DEST.stat().st_size)
