# ADR-038: Quality Operator DAG

- Status: Accepted
- Date: 2026-08-29
- Extends: [ADR-037](ADR-037-emergent-quality-profile.md),
  [ADR-007](ADR-007-production-dag-execution-model.md)
- Related: [ADR-016](ADR-016-bottled-beverage-snapshot.md)

## Context

ADR-037 made drink quality a derived `QualityProfile` and forbade a
persisted magic score. The first implementation folded wine and beer
chemistry in one Java method. That recreated drink-family logic the
production DAG had already eliminated.

Quality must stay evaluable from a batch snapshot (bottle, blend, inspect)
without replaying production. Wine and beer share FERMENT but interpret
different axes.

## Decision

Quality interpretation is a second DAG, same composition pattern as
process types:

- Java registers `QualityOperator` primitives on `AlcoholicApi` (frozen
  with processes and properties).
- Datapacks wire `data/<ns>/alcoholic/quality/*.json`.
- A beverage may name `"quality": "alcoholic:wine"`. Omitted means
  `alcoholic:generic`.
- `QualityEvaluator` runs the graph on the current `LiquidBatchView`.
- Resolution: beverage identity, then `baseLiquid` as beverage or graph
  id, then generic.

The production DAG still writes chemistry and stamps
`complexity_cap` / `purity_floor`. Ethanol is never a quality input.
Shipped operators extract the former `QualityProfile.derive` formula
(`harvest_complexity`, `distance_balance`, `weighted_present`,
`oxygen_curve`, `wood_sweet_spot`, `aging_maturity`, `stress`,
`cap_floor`, `fold_summary`).

Unknown operators, cycles, missing `outputs.profile`, invalid operator
config, and ethanol as a quality input are rejected when the catalog
loads. A graph may have at most 256 nodes. Fold inputs may use
`QualityInput.Sum` (quality-only combinator). Graphs are datapack-only;
there is no Java seed.

## Consequences

- Addons register operators; they do not patch a wine/beer switch.
- A datapack can retarget wine balance or omit hop axes without Java.
- Resolution follows the Decision: identity, then `baseLiquid` as a
  beverage or graph id, then `alcoholic:generic` from the catalog.
  `LiquidBatch.merge` clears identity but keeps `baseLiquid`, so a blend
  of the same wine still uses the wine graph. Missing generic throws.
- Bottle snapshot version stays 2; `Quality` remains the derived summary.
- Customizing a shipped graph means replacing its JSON (or a new id plus
  beverage `quality`). Duplicate datapack ids are rejected.
