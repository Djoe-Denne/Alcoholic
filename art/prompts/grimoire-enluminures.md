# Enluminures des grimoires

Planches GUI `100×56` : `assets/alcoholic/textures/gui/grimoire/<wine|beer>/<id>.png`.

Génération : ImageGen paysage. **Ne jamais cropper** : `tools/export_grimoire_plates.py` fait un *fit* (letterbox parchemin) vers `100×56` (jar) et `800×448` (Alcoholic-128x). Un crop 3:2 → 25:14 coupe soleil et rinceaux.

Coller **un prompt à la fois** : `{SHARED}` + une ligne **A** ou **B**.

- **A — Atelier** : l’objet / le lieu de jeu, une miniature de chantier.
- **B — Emblème** : allégorie de manuscrit, un signe lisible à 100×56.

---

## Shared visual prompt

> Medieval book illumination for the Alcoholic Minecraft grimoire, a single
> wide miniature painted on warm parchment. Book-of-Hours / 15th-century
> workshop plate crossed with Alcoholic's BDCraft language: chunky painted
> silhouettes, dark chocolate-brown contours, copper and gold-leaf highlights,
> cel-shaded pseudo-3D, no photorealism. Thin gold fillet and a simple vine
> or barley border that stays inside the frame. One clear focal subject,
> even padding, readable after a hard downscale to 100×56 pixels. Keep the
> gold fillet and vines inside the frame. No crop. No letters, numbers, captions, watermark, UI,
> inventory, pixel grid, checkerboard, modern factory chrome, or extra
> scenes in the corners. Not vanilla Minecraft, not anime, not a photograph.

Wine ink: burgundy, leaf green, oak, parchment.  
Beer ink: barley gold, hop green, copper, cream foam, parchment.

---

## Vin — `textures/gui/grimoire/wine/`

| Id | Titre | A — Atelier | B — Emblème |
| --- | --- | --- | --- |
| `frontispiece` | Frontispice | Closed leather book lying open just enough to show cream pages, a crushed burgundy grape cluster staining the cover and one page, a few loose seeds, gold vine corner. | A capital letter-less medallion: grapes turning into an open book, juice becoming ink, gold vine knot, no alphabet. |
| `vineyard` | La vigne | Two oak posts of equal height, a taut galvanized wire, a perennial trained vine with leaves and a ripe burgundy bunch. Plains light, no house. | A wild vine on a birch edge and one short cutting in a gardener's hand; gnarled trunk, tendrils, no trellis yet. |
| `workshop` | L'atelier | Three machines in a quiet row: basket press, wooden fermenter, oak barrel. Empty buckets at their feet. No gears, no pipes. | An empty open hand above a machine lid, and three buckets as the only tools; warm workshop light, no Create brass. |
| `press` | Le pressurage | Basket press mid-squeeze: grapes in, burgundy must streaming, a dark pomace cake beside it. Twenty-tick hush, no bottle in sight. | A single iron bucket walking away from the press, full of opaque must; the press stays behind, pomace left on the plate. |
| `ferment` | La fermentation | Wooden fermenter, a cream yeast cake on the rim, cloudy must becoming wine, three bubbles, a faint vent of CO2 leaving, never stored. | Must and yeast as two streams joining in a warm 18–24 °C band suggested by a soft gold halo, not a thermometer. |
| `cellar` | La cave | Only an oak barrel. No cellar room, no stone cave. Young wine poured in; the chunk of earth around it stays loaded and lit. | A seasoned barrel remembering the last liquid as a ghost stain on the staves, a small 1.15 flourish in gold leaf, still no architecture. |
| `bottle` | La bouteille | Empty pale-aqua bottle at a barrel tap, cork ready. The bottle is a sealed portrait, not a tiny tank. | A corked bottle holding a still snapshot of the wine (ruby, not flowing); fermenter and barrel behind, press absent. |
| `warnings` | Ce qu'il ne faut pas faire | Press and fermenter with a broken gap between them, no bucket; pruning shears laid on summer leaves, unused. | A split medallion: grape on the left, barley on the right, a gold bar between them so the two lots never merge. |
| `industrial` | L'usine | Steel press, vat, and tank in the back; the oak barrel still in front, the only cave. Same work, louder hands. | The wine graph as three gold stations (press, ferment, oak). A missing fourth station outlined in empty gold: no industrial cave yet. |

---

## Bière — `textures/gui/grimoire/beer/`

| Id | Titre | A — Atelier | B — Emblème |
| --- | --- | --- | --- |
| `frontispiece` | Frontispice | Closed leather book dusted with golden barley grains and one pale awn; no barrel, no crock on the desk. | Barley ear and hop cone crossed like a heraldic charge on parchment; the path ends at a fermenter, aging barrel omitted. |
| `fields` | Les champs | Farmland barley (annual, golden ears) beside hop bines on a wire between two posts, four blocks high at most. | Wild hops on a forest floor and a knobby rhizome with one green bud; barley is only a distant gold strip. |
| `brewery` | La brasserie | Five stations in one yard: malting floor, mill touching a small engine, mash tun on magma, kettle on a lit campfire, fermenter in still air. | A courtyard plan as an illuminated diagram: five simple glyphs, mill glued to its engine, no pipes. |
| `malt` | Le maltage | Malting floor with a thin barley carpet, a hand sneaking a scoop of pale malt. Floor is not a mill. | Three malt mounds (pale, amber, dark); the pale heap is haloed gold as the official beer graph. |
| `mill` | Le moulin | Malt mill flush against a fueled engine (coal glow). Grist spilling. Without the engine the stones would sleep. | Engine and mill as a touching pair; a ghost mill alone, stalled, faded, to show drive is required. |
| `mash` | Le brassage | Oak mash tun sitting on magma (warm, not fire). Grist and a water bucket going in; wort and spent grain coming out. | Two tun mouths: inlet and wort drain, magma glow under the hull, 62–68 °C suggested by a soft copper halo. |
| `boil` | L'ébullition | Copper kettle on a lit campfire, wort rolling, one hop charge at the start. Bittering, not a second aroma dump. | Campfire correctly under the kettle; a magma block under a second kettle is marked by a dull broken ring — too cold to boil. |
| `ferment` | La fermentation | The same wooden fermenter as the vineyard, now filled with hopped wort and yeast. CO2 vents. No barrel waiting. | Sugar dying into ethanol as a gold-to-amber change in the vat; beer is born here, aging is absent. |
| `heat` | La chaleur | Two vessels side by side: tun on magma, kettle on campfire. Heat is only the block below. | A painted legend of four under-blocks (magma, furnace, campfire, lava) as simple colored tiles, no digits, one correct pair glowing. |
| `bottle` | La bouteille | Empty bottle offered only to the fermenter. Cork, 250 mB snapshot, not a tank you can pour back. | Mash tun and kettle turning the bottle away; only the fermenter accepts it. |
| `warnings` | Ce qu'il ne faut pas faire | A press, an oak barrel, and a crock all crossed by a quiet gold stroke; a mill with no engine, stones still. | Wrong marriages: magma under a kettle, campfire under a tun, a bottle dipping into wort — each pair dimmed, the true path bright. |

---

## Recette de collage

```
{SHARED}

Scene: {A or B line}. Wine ink / beer ink as specified. Single plate, 16:9.
```

Si le rendu est trop photo : `more painted, less photo, BDcraft illumination`.  
Si trop de texte apparaît : `no letters, no numbers, no captions`.  
Si trop de détail : `fewer objects, thicker contours, readable at 100 pixels wide`.
