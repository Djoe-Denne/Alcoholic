# Alcoholic items and vineyard texture prompt set

Generation mode: built-in ImageGen.

## Shared visual prompt

> Create one high-resolution square master for a Minecraft Java texture that
> remains readable after reduction to 16 x 16. Match Alcoholic's existing 512
> px 3D block atlases: warm hand-painted fantasy-comic rendering inspired by
> BDCraft's visual language, dark chocolate-brown contours, copper/gold
> highlights, chunky silhouettes, and controlled cel-shaded pseudo-3D depth.
> Use a genuine transparent alpha background for item and cutout textures. No
> checkerboard, text, watermark, border, frame, floor, cast shadow, scenery,
> inventory UI, or pixel grid. Center one subject with even padding and avoid
> micro-detail that will disappear at 16 x 16.

Style references:

- `resourcepacks/Alcoholic-512x/assets/alcoholic/textures/block/artisanal_fermenter.png`
- `resourcepacks/Alcoholic-512x/assets/alcoholic/textures/block/brewing_kettle.png`
- `resourcepacks/Alcoholic-512x/assets/alcoholic/textures/block/malt_mill.png`

## Item subjects

| Texture | Subject prompt |
| --- | --- |
| `barley` | One diagonal golden barley ear with a short stem and readable awns. |
| `barley_seeds` | Six plump golden-tan barley kernels in one compact cluster. |
| `beer_bucket` | Galvanized iron bucket with amber beer and a compact cream foam cap. |
| `beverage_bottle` | Corked dark-amber artisanal bottle with golden drink and a blank cream label. |
| `empty_bottle` | Empty clear pale-aqua handcrafted glass bottle with an open neck. |
| `grape_pomace` | Cohesive mound of pressed burgundy grape skins, seeds, and short stem fragments. |
| `grist` | Mound of pale coarse cracked grain with angular flakes and granules. |
| `hop_rhizome` | Knobby brown hop rhizome with two rootlets and one green shoot bud. |
| `hopped_wort_bucket` | Base iron bucket edited to contain cloudy resinous olive-gold hopped wort. |
| `hops` | One fresh vivid-green hop cone with layered bracts. |
| `malted_barley` | Mound of amber malted kernels with a few tiny rootlets. |
| `pruning_shears` | Dark-steel vineyard shears with brass pivot and burgundy wrapped handles. |
| `red_grape_cutting` | Short woody cutting, green leaf, red buds, and a red cultivar tag. |
| `red_grape_must_bucket` | Base iron bucket edited to contain opaque burgundy-purple grape must. |
| `red_grapes` | Tapered bunch of ripe burgundy-purple grapes, short stem, and one green leaf. |
| `red_wine_bucket` | Base iron bucket edited to contain clear deep ruby wine. |
| `spent_grain` | Cohesive mound of wet dark-tan spent grain and crushed husks. |
| `trellis_spool` | Oak spool with tidy coils of galvanized vineyard wire. |
| `white_grape_cutting` | Short woody cutting, green leaf, pale buds, and a gold cultivar tag. |
| `white_grape_must_bucket` | Base iron bucket edited to contain cloudy pale yellow-green grape must. |
| `white_grapes` | Tapered bunch of translucent pale yellow-green grapes and one green leaf. |
| `white_wine_bucket` | Base iron bucket edited to contain clear pale straw-gold wine. |
| `wort_bucket` | Base iron bucket edited to contain cloudy copper-gold wort. |
| `yeast` | Warm cream-beige porous cake of brewer's yeast with a few crumbs. |
| `young_red_wine_bucket` | Base iron bucket edited to contain cloudy raspberry-purple young wine with three bubbles. |
| `young_white_wine_bucket` | Base iron bucket edited to contain cloudy pale lemon-gold young wine with three bubbles. |

## Crop and hop-bine subjects

| Texture | Subject prompt |
| --- | --- |
| `barley_crop_0` | Four short fresh-green shoots, no stems or ears, about 30% canvas height. |
| `barley_crop_1` | Four green-gold stalks with two small immature ears, about 62% height. |
| `barley_crop_2` | Five mature golden barley stalks with compact ears and narrow leaves. |
| `hop_bine_0` | Two short curling shoots with four serrated leaves, no cones. |
| `hop_bine_1` | Three rising bines, several leaves, and one tiny immature cone. |
| `hop_bine_2` | Three mature twisting bines, broad leaves, and four pale-green cones. |
| `wild_hops` | Untamed forest-floor hop bush with short curling vines, broad serrated leaves, and several pale yellow-green cones; asymmetrical but readable as a cross-plane plant at 16x16. |

## Wild hops item

| Texture | Subject prompt |
| --- | --- |
| `wild_hops` | Compact uprooted wild-hop sprig with a short curling vine, small rhizome/root nub, three serrated leaves, two pale-green cones, and one tendril; bold isolated inventory silhouette. |

## Grapevine stages

All grapevines use the same centered front-facing gnarled trunk with two arms,
transparent gaps, broad leaves, and curling tendrils. The neutral stages below
share one master between red and white cultivars because their fruit color is
not yet visible.

| Stage | Subject prompt |
| --- | --- |
| `planted` | Tiny cutting with two dormant buds and one very small green shoot. |
| `establishing` | Young slim vine, two short shoots, three small leaves, and one tendril. |
| `vegetative` | Nearly full-size vine with six broad leaves and no flowers or grapes. |
| `flowering` | Five broad leaves and exactly three cream-white blossom clusters. |
| `green_fruit` | Five leaves and exactly three small clusters of matte pale-green berries. |
| `dormant` | Bare pruned winter vine with nodes and no leaves, tendrils, flowers, or grapes. |
| `red_grapevine_ripening` | Three bunches mixing pale-green, rose, crimson, and burgundy berries. |
| `red_grapevine_harvest_ready` | Three full bunches of ripe glossy burgundy-purple grapes. |
| `white_grapevine_ripening` | Three bunches mixing pale-green and creamy yellow-gold berries. |
| `white_grapevine_harvest_ready` | Three full bunches of ripe translucent pale yellow-green/golden grapes. |

## Vineyard post

> Create a fully opaque, edge-to-edge, flat orthographic Minecraft block UV
> texture rather than an inventory icon: hand-hewn dark oak with vertical grain,
> alternating warm-brown regions, one oval knot, darker worn edge bands, and two
> small iron-gray trellis staples. No perspective, post silhouette, vines,
> grapes, scenery, or transparent margin.

## Vineyard end post

> Create a fully opaque, edge-to-edge, flat orthographic Minecraft block UV
> texture rather than an inventory icon: the same Alcoholic hand-hewn dark oak
> as `vineyard_post` (warm medium oak, vertical flowing grain, one oval knot,
> darker worn edge bands), but painted for a thicker end post with a heavier
> top cap. Keep a darker horizontal band across the upper fifth for the
> chapiteau, and add two or three small iron-gray trellis staples. No
> perspective, post silhouette, vines, grapes, scenery, or transparent margin.

## World fluids

Painted still/flow tiles are fully opaque and fill the square edge to edge.
Still masters live at 512 x 512; flow masters live at 1024 x 1024. Do not
include a bucket, rim, handle, background, or inventory silhouette. Java
applies a white tint, so the PNG carries the liquid color.

| Texture | Subject prompt |
| --- | --- |
| `beer_still` | Top-down seamless amber beer with cream foam patches and a few bubbles. |
| `beer_flow` | Same beer, stretched into downward diagonal current streaks. |
| `hopped_wort_still` | Top-down seamless cloudy olive-gold hopped wort with resin flecks. |
| `hopped_wort_flow` | Same hopped wort as downward diagonal current streaks. |
| `red_grape_must_still` | Top-down seamless opaque burgundy must with pulp specks. |
| `red_grape_must_flow` | Same red must as downward diagonal current streaks. |
| `white_grape_must_still` | Top-down seamless cloudy pale yellow-gold must with tiny flecks. |
| `white_grape_must_flow` | Same white must as downward diagonal current streaks. |
| `young_red_wine_still` | Top-down seamless cloudy raspberry-purple young wine with three bubbles. |
| `young_red_wine_flow` | Same young red wine as downward diagonal current streaks. |
| `young_white_wine_still` | Top-down seamless cloudy lemon-gold young wine with three bubbles. |
| `young_white_wine_flow` | Same young white wine as downward diagonal current streaks. |
| `red_wine_still` | Top-down seamless clear deep ruby wine, glossy, no foam. |
| `red_wine_flow` | Same red wine as downward diagonal glossy current streaks. |
| `white_wine_still` | Top-down seamless clear straw-gold wine, glossy, no foam. |
| `white_wine_flow` | Same white wine as downward diagonal glossy current streaks. |
| `wort_still` | Top-down seamless cloudy copper-gold wort with malt haze. |
| `wort_flow` | Same wort as downward diagonal current streaks. |

## Trellis wire

> Create a fully opaque, edge-to-edge, flat orthographic Minecraft block UV
> texture rather than an inventory icon: galvanized vineyard wire metal filling
> the whole square. Cool iron-gray steel with narrow painted highlights, a
> slight helical twist / strand suggestion, and dark chocolate-brown contour
> edges. No wood, no transparency, no coil, no spool, no perspective, no
> scenery, and no wire silhouette on empty background.
