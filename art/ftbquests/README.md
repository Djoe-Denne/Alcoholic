# FTB quest flipbooks

Pedagogical 128×128 comic diagrams for the optional FTB chapter.
Six vertical strips, six frames each. Oak uses the locked engine
palette (mean RGB about 108, 69, 35).

Regenerate:

```text
python art/ftbquests/render_flipbooks.py
```

Output:

- this folder (`<id>.png` + `<id>.png.mcmeta`)
- `minecraft-common/src/main/resources/assets/alcoholic/textures/item/ftbquests/`

Chapter images reference `alcoholic:item/ftbquests/<id>` (no `.png`)
so FTB Library loads an atlas sprite and honours `.mcmeta`.
