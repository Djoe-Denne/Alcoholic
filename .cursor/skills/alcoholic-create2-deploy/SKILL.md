---
name: alcoholic-create2-deploy
description: Deploy a remapped Alcoholic Forge jar to the CurseForge Create 2 Mekanism instance after each playable build. Rebuild and install Alcoholic-128x when textures or pack resources changed. Use when building, assembling, shipping a jar, deploying, or when the user mentions Create 2 Mekanism, the CurseForge instance, or Alcoholic-128x.
---

# Alcoholic → Create 2 Mekanism

After every playable version (jar, assemble, `reobfJar`, "build and try it", "mets ça en jeu"), deploy here. Do not wait for the user to name the instance.

Instance (verbatim):

`C:\Users\djden\curseforge\minecraft\Instances\Create 2 Mekanism`

Wiki notes: [curseforge-create2-deploy](../../../obsidian/projects/alcoholic/skills/curseforge-create2-deploy.md).

## Always: remapped jar

1. Build the remapped artifact:

```powershell
.\gradlew :platform-forge-1.19.2:reobfJar
```

2. Copy with the script (replaces any older `alcoholic-forge-*.jar` in `mods/`):

```powershell
python tools/deploy_create2.py
```

Source jar: `platform-forge-1.19.2/build/libs/alcoholic-forge-1.19.2-0.1.0-SNAPSHOT.jar` (not the `-sources` jar).

F3+T does **not** reload a new jar. If the copy fails (WinError 32 / file locked), quit the client and rerun the script. Tell the user to relaunch the instance.

## When textures or pack resources changed

Also refresh **Alcoholic-128x** (the instance uses that zip, not 64).

Treat as a pack change if the work added or edited any of:

- `resourcepacks/Alcoholic-*`
- `art/blockbench/**/master-512/`
- `minecraft-common/src/main/resources/assets/alcoholic/textures/`
- generated textures the player will see (items, blocks, GUI plates)

Then:

1. Fill `resourcepacks/Alcoholic-128x` first. Downsample **from the 512 master only** (`tools/build_item_plant_texture_packs.py`, `tools/build_fluid_texture_packs.py`, or the machine-model chain). Never invent 128 art for a 16× generated placeholder (e.g. grimoire icons).
2. Zip and install:

```powershell
python tools/deploy_create2.py --with-128
```

Minecraft 1.19.2 zip rules (the script enforces them):

- `pack.mcmeta` at the **zip root**
- entry paths use `/`, never `\`
- do not wrap the zip in an `Alcoholic-128x/` folder

Do not use `Compress-Archive`. A locked zip: quit the client, rerun.

The in-game pack **Alcoholic-128x** must stay above PureBDcraft and above the mod resources.

## After deploy

- Jar-only change: relaunch the instance.
- Pack-only change with the client already running: F3+T is enough **if** the zip copy succeeded.
- Report: jar path + size, whether the 128 zip was written, and if Minecraft must quit.
