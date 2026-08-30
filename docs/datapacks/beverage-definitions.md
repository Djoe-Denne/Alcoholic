# Beverage datapack format

Alcoholic loads beverage-framework files from every namespace under:

```text
data/<namespace>/alcoholic/ingredients/*.json
data/<namespace>/alcoholic/processes/*.json
data/<namespace>/alcoholic/beverages/*.json
data/<namespace>/alcoholic/liquids/*.json
data/<namespace>/alcoholic/quality/*.json
```

The core never inspects a finite list of drink families. A datapack that only
uses already registered process types, properties, and ingredient selectors
needs no Java code. Cider in the application test pack is the acceptance
example: apple → PRESS → apple must → FERMENT → young cider → AGE → aged
cider, with zero cider Java types. A second data-only slice is
`young_mead → AGE → aged_mead` using the same `alcoholic:age` engine.

## Identifiers

IDs use `namespace:path`. If `id` is omitted, the file name becomes the path
and the datapack namespace is reused.

## Ingredient definition

```json
{
  "id": "mypack:apple",
  "tags": ["alcoholic:fruits/apple"]
}
```

## Liquid definition

`LiquidDefinition` is the *kind* of liquid. Batch state (volume, sugar,
ethanol, temperature) lives on `LiquidBatch`, not in this file.

```json
{
  "id": "mypack:apple_must",
  "defaults": {
    "alcoholic:sugar": 0.62,
    "alcoholic:acidity": 0.40,
    "alcoholic:ethanol": 0.0,
    "alcoholic:temperature": 20.0
  }
}
```

Minecraft fluids are registered only for liquids that must travel in world
pipes. A data-only pack can name liquids the process engine understands
without adding Forge fluid entries.

## Process definition

A reusable transformation that names a registered process type. Graphs request
`alcoholic:press`, never a machine id such as `artisanal_press`.

### PRESS

```json
{
  "id": "mypack:press_fruit",
  "process": "alcoholic:press",
  "config": {
    "input": { "ingredient": "mypack:apple", "amount": 8 },
    "output": { "liquid": "mypack:apple_must", "volume": 1000 },
    "byproduct": { "item": "minecraft:apple", "amount": 1 },
    "processing_time": 40,
    "yield": 1.0,
    "create_compatible": true
  },
  "inputs": {
    "source": { "ingredient": "mypack:apple" }
  },
  "outputs": ["must"]
}
```

`input.ingredient` may be an item id, a defined ingredient, or a `#tag`.
`create_compatible` emits a Create Mechanical Press + Basin compacting recipe
when Create is loaded. Agricultural lot properties on the solid input are
copied onto the produced liquid batch by the generic PRESS handler.

### FERMENT

```json
{
  "id": "mypack:ferment_must",
  "process": "alcoholic:ferment",
  "config": {
    "input_liquid": "mypack:apple_must",
    "output": { "liquid": "mypack:young_cider" },
    "yeast": { "tag": "alcoholic:yeast" },
    "require_yeast": true,
    "preferred_temperature": { "min": 16, "max": 22 },
    "operating_temperature": { "min": 8, "max": 28 },
    "ticks_to_complete": 100,
    "kinetics": {
      "sugar_to_ethanol": 0.47,
      "completion_threshold": 0.02,
      "co2_per_sugar": 0.45
    }
  },
  "inputs": {
    "yeast": { "tag": "alcoholic:yeast" }
  },
  "outputs": ["cider"]
}
```

Fermentation is continuous: sugar falls, ethanol rises, CO2 is recorded and
vented by the artisanal vessel. Temperature outside the preferred band slows
the rate; outside the operating band stalls it.

Process definitions cannot point at graph nodes. They may be referenced from
beverage nodes through `definition`.

### AGE

```json
{
  "id": "mypack:age_young_mead",
  "process": "alcoholic:age",
  "config": {
    "input_liquid": "mypack:young_mead",
    "output": { "liquid": "mypack:aged_mead" },
    "preferred_temperature": { "min": 10, "max": 16 },
    "operating_temperature": { "min": 0, "max": 36 },
    "ticks_to_complete": 12000
  },
  "inputs": {},
  "outputs": ["finished"]
}
```

An empty `config` is valid: identity is unchanged and maturity still
progresses. If `output.liquid` is omitted, the batch keeps its definition
when aging completes. The oak barrel only runs AGE when this recipe matches
the stored liquid. There is no unload-time catch-up; see ADR-015.

AGE is optional. A beverage graph that stops after FERMENT is valid. Grain
beer ships without an AGE node; a later barrel-aged graph can add one.

### MALT

```json
{
  "id": "mypack:malt_pale",
  "process": "alcoholic:malt",
  "config": {
    "input": { "tag": "alcoholic:barley", "amount": 1 },
    "output": { "item": "mypack:malted_barley", "amount": 1 },
    "processing_time": 80,
    "moisture_requirement": 0.4,
    "kiln_profile": {
      "id": "mypack:pale",
      "color_potential": 0.12,
      "fermentable_potential": 0.85,
      "roast_intensity": 0.15
    }
  },
  "inputs": { "grain": { "tag": "alcoholic:barley" } },
  "outputs": ["malt"]
}
```

Solid-to-solid. Kiln profiles are data. The malting floor executes `MALT`.

### MILL

```json
{
  "id": "mypack:mill_malted_grain",
  "process": "alcoholic:mill",
  "config": {
    "input": { "tag": "alcoholic:malted_grain", "amount": 1 },
    "output": { "item": "mypack:grist", "amount": 1 },
    "processing_time": 80,
    "create_compatible": true
  },
  "inputs": { "malt": { "tag": "alcoholic:malted_grain" } },
  "outputs": ["grist"]
}
```

`create_compatible` emits Create millstone (`create:milling`) and crushing
wheels (`create:crushing`) recipes as optional extra executors. The native
Alcoholic Malt Mill always executes the same `MILL` definition. Create
must not be required for official progression.

### MASH

```json
{
  "id": "mypack:mash_wort",
  "process": "alcoholic:mash",
  "config": {
    "solid": { "tag": "alcoholic:grist", "amount": 1 },
    "liquid": { "fluid": "minecraft:water", "volume": 1000 },
    "output": { "liquid": "mypack:wort", "volume": 1000 },
    "byproduct": { "item": "mypack:spent_grain", "amount": 1 },
    "processing_time": 40,
    "preferred_temperature": { "min": 62, "max": 68 },
    "operating_temperature": { "min": 52, "max": 78 }
  },
  "inputs": {
    "grist": { "tag": "alcoholic:grist" },
    "water": { "item": "minecraft:water_bucket" }
  },
  "outputs": ["wort"]
}
```

Temperature changes extraction yield. Spent grain is extractable. The mash
tun fills water and drains wort through standard fluid capabilities.

### BOIL

```json
{
  "id": "mypack:boil_wort",
  "process": "alcoholic:boil",
  "config": {
    "input_liquid": "mypack:wort",
    "output": { "liquid": "mypack:hopped_wort" },
    "addition": { "tag": "alcoholic:hops", "amount": 1 },
    "processing_time": 40,
    "preferred_temperature": { "min": 98, "max": 105 },
    "operating_temperature": { "min": 90, "max": 110 },
    "hop_profile": {
      "bitterness_potential": 0.55,
      "aroma_potential": 0.40
    }
  },
  "inputs": { "hops": { "tag": "alcoholic:hops" } },
  "outputs": ["hopped_wort"]
}
```

Bitterness and aroma are typed liquid properties. `additions` with
`at_progress` is the extension point for later hop schedules; Phase 7A uses
a single start-of-process addition.

### BLEND

```json
{
  "id": "mypack:blend_mead",
  "process": "alcoholic:blend",
  "config": {
    "accepted_inputs": ["mypack:young_mead", "mypack:aged_mead"],
    "output": { "liquid": "mypack:cuvée_mead" },
    "min_inputs": 2
  },
  "inputs": {},
  "outputs": ["blended"]
}
```

Tanks never merge distinct definitions. The artisanal crock calls BLEND
from an explicit player action (sneak + empty hand).

## Beverage definition

A beverage is an identity, an optional category, a DAG, and the liquid
properties that identity may carry. **No process is mandatory.** The graph is
the recipe.

### Example: apple cider (PRESS then FERMENT)

```json
{
  "id": "mypack:cider",
  "category": "cider",
  "graph": {
    "nodes": [
      {
        "id": "press",
        "definition": "mypack:press_fruit"
      },
      {
        "id": "ferment",
        "definition": "mypack:ferment_must",
        "inputs": {
          "must": { "node": "press", "port": "must" },
          "yeast": { "tag": "alcoholic:yeast" }
        }
      }
    ],
    "outputs": {
      "result": { "node": "ferment", "port": "cider" }
    }
  },
  "properties": ["alcoholic:sugar", "alcoholic:ethanol"]
}
```

### Example: fruit liqueur (INFUSE only — no PRESS, no FERMENT)

```json
{
  "id": "mypack:fruit_liqueur",
  "graph": {
    "nodes": [
      {
        "id": "infuse",
        "process": "alcoholic:infuse",
        "inputs": {
          "spirit": { "beverage": "mypack:spirit" },
          "fruit": { "tag": "alcoholic:fruits/apple" },
          "sugar": { "item": "minecraft:sugar" }
        },
        "outputs": ["liqueur"]
      }
    ],
    "outputs": {
      "result": { "node": "infuse", "port": "liqueur" }
    }
  }
}
```

A rum-style wash may be FERMENT then DISTILL with no PRESS node. The engine
never injects missing steps.

### Node fields

- `id`: unique inside the graph
- `process`: registered process type, optional when `definition` supplies it
- `definition`: optional process definition to inherit inputs, outputs, and config
- `config`: decoded by the process type codec
- `inputs`: map of port name to selector
- `outputs`: port names produced by the node

Node fields override inherited definition fields.

### Input selectors

Exactly one kind:

| Kind | Example |
|------|---------|
| item | `{ "item": "minecraft:sugar" }` |
| tag | `{ "tag": "alcoholic:fruits/apple" }` |
| ingredient | `{ "ingredient": "mypack:apple" }` |
| beverage | `{ "beverage": "mypack:spirit" }` |
| node output | `{ "node": "press", "port": "must" }` |

### Validation rules

- IDs are unique across loaded files of the same kind
- Process types and properties must already be registered in Java
- Referenced ingredients, process definitions, beverages, liquids, nodes, and ports must exist
- The graph must be acyclic; cycle errors name the beverage id and remaining node ids
- Every node must expose at least one output after definition expansion
- PRESS/FERMENT configs that name a liquid must point at a loaded liquid definition
- A rejected reload keeps the previous snapshot

Category is free metadata. The engine does not require pressing, fermentation,
a crop, or any cultural production order.

## Quality graphs

Drink quality is a derived profile (purity, complexity, maturity, balance,
defects, summary). Processors write chemistry only. Optional field
`quality` on a beverage names a graph under
`data/<ns>/alcoholic/quality/*.json`.

Java registers operators (`alcoholic:harvest_complexity`,
`alcoholic:distance_balance`, `alcoholic:fold_summary`, …). The datapack
wires them. Shipped graphs: `alcoholic:wine`, `alcoholic:beer`,
`alcoholic:spirit`, `alcoholic:generic`. Omit `quality` to use generic
from the catalog (present axes only). Graphs are datapack-only, must
declare `outputs.profile` on a node that emits the six profile ports
(`purity`, `complexity`, `maturity`, `balance`, `defects`, `summary`),
and may have at most 256 nodes. Ethanol is rejected as a quality input
at load. A catalog that ships any quality graph must include
`alcoholic:generic`. See ADR-038.

Inspect and bottling resolve the graph from beverage identity, then
`baseLiquid`, then generic.

## Built-in process types

`alcoholic:press`, `alcoholic:mill`, `alcoholic:malt`, `alcoholic:mash`,
`alcoholic:boil`, `alcoholic:ferment`, `alcoholic:distill`, `alcoholic:age`,
`alcoholic:blend`, `alcoholic:infuse`, `alcoholic:bottle`.

PRESS, FERMENT, AGE, BLEND, and BOTTLE are executable. DISTILL and the
other stubs remain registered capabilities so datapacks can describe
graphs that the current machines do not yet run.

## Built-in properties

`alcoholic:sugar`, `alcoholic:ethanol`, `alcoholic:acidity`,
`alcoholic:tannin`, `alcoholic:bitterness`, `alcoholic:carbonation`,
`alcoholic:maturity`, `alcoholic:temperature`, `alcoholic:quality`,
`alcoholic:variety`, `alcoholic:fermentation_stress`,
`alcoholic:wood_exposure`, `alcoholic:oxidation_exposure`.

Numeric properties merge with a volume-weighted average unless a property
registers a different `PropertyMerge` (`SUM`, `MAX`, `MIN`, `FIRST`,
`IDENTICAL_OR_REJECT`, `COMBINE_SET`, or `CUSTOM` via
`LiquidProperty.aggregator()`). `variety` keeps a shared string or
becomes `"blended"`.
