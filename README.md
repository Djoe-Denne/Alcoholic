# Alcoholic

Alcoholic is a brewing, winemaking, and distillation mod. The current target is
Minecraft 1.19.2 on Forge, using Java 17 and a Gradle multi-module build.

- Mod name: `Alcoholic`
- Mod ID: `alcoholic`
- Base package: `com.djden.alcoholic`
- Active platform: Forge 43.x for Minecraft 1.19.2

## Architecture

Dependencies point inward. Domain and application behavior remain independent
of Minecraft loaders, while platform-specific code is confined to adapters and
the composition root.

### Modules

- `alcoholic-api`: versioned public Java surface for datapack codecs,
  process types, liquid properties, and addon registration. This is the only
  module addons may compile against.
- `domain`: pure Java domain vocabulary and rules.
- `platform-api`: pure Java, loader-neutral identifiers and platform ports.
- `application`: pure Java use cases and orchestration.
- `minecraft-common`: shared Minecraft code using vanilla APIs only; Forge,
  Fabric, and third-party mod APIs are forbidden.
- `integration-vinery`: loader-neutral Vinery policy and semantic mapping.
- `integration-brewery`: loader-neutral Brewery policy and semantic mapping.
- `integration-create`: loader-neutral Create integration boundary, including
  translation of generic PRESS definitions into Mechanical Press compacting
  specs. No Create API types.
- `integration-create-forge-1.19.2`: Forge/Create adapter boundary.
- `integration-test-addon`: compile-only example addon used to prove that a
  new process type can be registered through `alcoholic-api` alone.
- `platform-forge-1.19.2`: the only Forge-aware module and the current
  composition root.

A future Fabric distribution must add Fabric platform and integration adapters;
it must not introduce Fabric dependencies into the existing pure or common
modules.

### Dependency graph

```text
alcoholic-api                        -> (none)
domain                               -> alcoholic-api
platform-api                         -> alcoholic-api, domain
application                          -> alcoholic-api, domain, platform-api
minecraft-common                     -> alcoholic-api, domain, platform-api,
                                        application
integration-vinery                   -> application
integration-brewery                  -> application
integration-create                   -> application
integration-create-forge-1.19.2      -> integration-create
integration-test-addon               -> alcoholic-api
platform-forge-1.19.2                -> minecraft-common,
                                        integration-vinery,
                                        integration-brewery,
                                        integration-create-forge-1.19.2
```

Each arrow points from a module to its compile-time project dependencies.

Architecture decisions are recorded in [`docs/adr`](docs/adr).

## Build and verification

On Windows:

```powershell
.\gradlew build
.\gradlew test
.\gradlew check
.\gradlew runData
.\gradlew runGameTestServer
```

Use `./gradlew` instead on POSIX systems. Append `-PwithCreate=true` to a
Forge runtime task when Create must be present, for example:

```powershell
.\gradlew runGameTestServer -PwithCreate=true
```

The property adds Create to the Forge runtime classpath so Mechanical Press
compacting recipes and fluid pipes can be exercised. Generic PRESS
definitions remain the source of truth.

Append `-PwithVinery=true` to launch a Forge runtime with Vinery, Do API, and
Architectury from CurseMaven, without adding a compile-time Vinery dependency:

```powershell
.\gradlew runGameTestServer -PwithVinery=true
```

The profile pins the Minecraft 1.19.2 CurseForge files Vinery `4833017`,
Do API `4676882`, and Architectury `4555749`. Forge development runs remap the
external mods' Mixin reference maps to the official mappings used by this
workspace.

## Status

Phase 6 industrialization is active on top of Phase 5 storage. PRESS,
FERMENT, AGE, BLEND, and BOTTLE remain executable, generic process types.
Liquids are `LiquidDefinition` plus `LiquidBatch` properties and flattened
provenance, exposed to Forge/Create through fluid adapters. An artisanal
press, fermenter, oak barrel, blending crock, industrial press, industrial
fermentation vat, and industrial storage tank implement those
capabilities. Create Mechanical Press + Basin compacting recipes are
generated from PRESS definitions marked `create_compatible`. Industrial
machines are extra executors, not a second recipe system.

The production DAG remains authoritative: no process is injected. Data-only
cider, plus beer / whisky / rum / fruit-liqueur fixtures, validate on the
same engine. Wine content (must, young wine, and finished wine) is datapack
plus Minecraft fluids, not Java branching. Barrels do not age while their
chunk is unloaded.

Phase 2 viticulture remains unchanged: persistent red and white perennial
vines, eight growth stages, trained trellis rows, climate/pruning data packs,
quantized harvest-lot metadata, localized inspection and tooltips, optional
Vinery seed planting and harvest resolution, deterministic generated assets,
and executable Forge GameTests. Harvest-lot sugar and acidity survive
pressing into liquid batches.

Current limits:

- no beer / malt / mash / hops / whisky / rum / distillation gameplay
  beyond data fixtures;
- no industrial aging warehouses, stills, or custom pipes/pumps/belts;
- no gas network (CO2 is modelled and vented);
- Forge 1.19.2 is the only runnable platform; Fabric remains a future adapter.

Architecture decisions for this phase: [ADR-007](docs/adr/ADR-007-production-dag-execution-model.md),
[ADR-008](docs/adr/ADR-008-process-executor-capability-model.md),
[ADR-009](docs/adr/ADR-009-liquid-batch-vs-minecraft-fluid.md),
[ADR-010](docs/adr/ADR-010-liquid-batch-merging-and-metadata-preservation.md),
[ADR-011](docs/adr/ADR-011-aging-process-model.md),
[ADR-012](docs/adr/ADR-012-process-vessel-and-material-interaction.md),
[ADR-013](docs/adr/ADR-013-batch-split-merge-and-blending.md),
[ADR-014](docs/adr/ADR-014-provenance-normalization.md),
[ADR-015](docs/adr/ADR-015-passive-long-running-process-update.md),
[ADR-016](docs/adr/ADR-016-bottled-beverage-snapshot.md),
[ADR-017](docs/adr/ADR-017-variable-size-multiblock-architecture.md),
[ADR-018](docs/adr/ADR-018-multiblock-state-ownership-and-persistence.md),
[ADR-019](docs/adr/ADR-019-port-and-capability-architecture.md),
[ADR-020](docs/adr/ADR-020-structure-validation-and-cache-invalidation.md),
[ADR-021](docs/adr/ADR-021-cross-chunk-multiblock-behavior.md),
[ADR-022](docs/adr/ADR-022-industrial-executor-modifiers.md),
[ADR-023](docs/adr/ADR-023-industrial-passive-process-performance.md).

Addon authors should depend only on `alcoholic-api`. See
[`docs/addons/public-api.md`](docs/addons/public-api.md) and
[`docs/datapacks/beverage-definitions.md`](docs/datapacks/beverage-definitions.md).
