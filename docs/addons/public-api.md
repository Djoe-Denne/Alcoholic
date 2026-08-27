# Alcoholic public API

The only supported Java dependency for addons is the `alcoholic-api` artifact.

Maven coordinates after a local publish:

```text
com.djden.alcoholic:alcoholic-api:0.1.0-SNAPSHOT
```

Publish locally with:

```powershell
.\gradlew :alcoholic-api:publish
```

The repository is written to `build/maven-repo`.

## Stability

Types annotated with `@PublicApi` are the supported surface. Internal modules
(`domain`, `application`, `minecraft-common`, loader adapters) may change
without notice. Do not compile against them.

`ApiVersion.VALUE` is the current API version.

## Registration lifecycle

1. During mod construction, obtain `AlcoholicApi.shared()`.
2. Register process types and liquid properties.
3. Alcoholic freezes the API at `FMLCommonSetupEvent` with lowest priority.
4. Datapacks may then reference those IDs. Registration after freeze fails.

```java
AlcoholicApi api = AlcoholicApi.shared();
api.processes().register(
        ResourceId.parse("mymod:rice_polishing"),
        RicePolishingConfig.CODEC,
        (request, config, context) -> ProcessResult.unsupported(
                ResourceId.parse("mymod:rice_polishing")
        )
);
api.properties().register(
        ResourceId.parse("mymod:polishing_ratio"),
        Double.class,
        DataCodecs.DOUBLE
);
```

A datapack can then use `mymod:rice_polishing` in a beverage graph without
changing Alcoholic sources.

## Codecs

Public codecs use `DataNode` and `DataCodec<T>`. They are pure Java 17.
Do not depend on Gson, `JsonElement`, `ResourceLocation`, Forge, or Minecraft
types at the API boundary.

`JsonDataParser` can parse JSON text into `DataNode` for tests and tools.

## Process executors

Machines implement `ProcessExecutor` and advertise process capabilities such
as `alcoholic:press`. They must not inspect beverage identities.

```text
Generic PRESS definition
        |
        +--> Artisanal press (capability alcoholic:press)
        |
        +--> Create compacting adapter (same capability)
        |
        +--> industrial press executor

Generic MILL definition
        |
        +--> Alcoholic Malt Mill
        |
        +--> Create Millstone / Crushing Wheels [optional]
```

No official Alcoholic production DAG may require an executor supplied
only by an optional integration. Create and other mods add extra
executors and automation.

The application layer asks whether executor X can run node Y with inputs Z,
then applies the registered `ProcessType`. Transformation logic lives in the
process type, not in the block entity.

Create Mechanical Press recipes are a translation of PRESS definitions marked
`create_compatible`. They do not reimplement agricultural transfer.

Yeast is selected with `#alcoholic:yeast`. Fermentation data names that tag;
it does not hardcode `alcoholic:yeast`.

## Vessels

Register a vessel profile when an addon adds a new material or capacity.
The oak barrel shipped by Alcoholic is one adapter of
`alcoholic:oak_barrel`. Datapacks do not need a new profile to attach AGE
to an existing vessel.

```java
api.vessels().register(myClayCrockProfile);
```

`ProcessContext.vessel()` and `ProcessContext.environment()` are optional.
AGE uses oak + a temperate default when a machine omits them.

`LiquidBatchView.provenance()` exposes flattened origin and blend maps.
Do not persist a recursive parent tree.

`ProcessType.acceptsLiquid` lets AGE, FERMENT, and BLEND bind to an input
liquid without a hard-coded config decode in the resolver.

Recipe viewers consume `ProcessDisplaySpec`. Implement `ProcessDisplaying`
on the addon config (or pass a display function to `ProcessType.of`) so
JEI/REI do not need a core switch. Omit volumes you do not know; do not
invent 1000 mB.

## Bottling

`alcoholic:bottle` writes a consumer snapshot (definition, ethanol, sugar,
acidity, maturity, origin, quality). It is not runtime state. Right-click
a barrel, crock, or fermenter with `alcoholic:empty_bottle`.

