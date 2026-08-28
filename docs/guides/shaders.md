# Guide : shaders et liquides monde

Les neuf fluides monde d’Alcoholic sont déjà des `LiquidBlock` translucides.
Iris / Oculus les dessinent dans le passage eau. Complementary, BSL et la
plupart des packs ne leur appliquent **vagues et réfraction** que si leurs
ids figurent dans **`shaders/block.properties` du pack de shaders**.

Alcoholic **ne patche pas** ce fichier au runtime. Iris ne fusionne pas un
`block.properties` venant du JAR ou d’un resource pack : le mettre dans
`Alcoholic-64x` écraserait le mapping Complementary (feuilles, verre, etc.).
Un mixin Oculus pourrait injecter les ids après chargement ; c’est fragile
d’une version à l’autre (`net.coderbot.iris` vs `net.irisshaders.iris`, id
eau différent selon le pack). On ne le fait pas.

Le catalogue est `#alcoholic:world_fluids` (blocs + fluides source/flowing).
Ne l’ajoute **pas** à `minecraft:water` (hydratation, bateaux, gameplay).

Vin : [vigne artisanale](vigne-artisanale.md). Bière :
[brasserie artisanale](brasserie-artisanale.md).

## Complementary Reimagined r5.x

L’eau n’est **pas** `block.8`. Dans le zip du shader, ouvre
`shaders/block.properties` et complète :

```properties
block.32000=water flowing_water alcoholic:red_grape_must alcoholic:white_grape_must alcoholic:young_red_wine alcoholic:young_white_wine alcoholic:red_wine alcoholic:white_wine alcoholic:wort alcoholic:hopped_wort alcoholic:beer
```

Ne mets pas ces ids dans `layer.translucent` : Complementary y met le verre,
pas l’eau. Le bloc qui coule porte le même id que la source
(`alcoholic:beer`, pas `flowing_beer`).

Recharge les shaders (menu Iris, touche **O** sur l’instance type) ou
relance le client. Une mise à jour CurseForge du zip Complementary
réécrit le fichier : il faut recoller la ligne.

## Autres packs (BSL, Complementary plus ancien, etc.)

Souvent `block.8=minecraft:water` (parfois aussi `minecraft:flowing_water`).
Ajoute les neuf mêmes ids sur **cette** ligne, pas sur `32000`.

Iris 1.7+ accepte un tag : `block.8 = %alcoholic:world_fluids` (ou
`block.32000 = %alcoholic:world_fluids` selon le pack). Oculus 1.6 /
Minecraft 1.19.2 ne le fait pas : liste les ids.
