# ADR-028: Create MILL Executor Integration

- Status: Superseded by [ADR-030](ADR-030-native-mechanical-executors.md)
- Date: 2026-08-25

## Context

Milling malted grain to grist should not add an Alcoholic mill block.
Create already provides Millstone and Crushing Wheels.

## Decision

`alcoholic:mill` is a generic solid transformation. Definitions marked
`create_compatible` are translated by `CreateMillRecipeTranslator` into:

- `create:milling` (millstone);
- `create:crushing` (crushing wheels, shorter `processingTime`).

The transformation (malted grain → grist, property copy) stays in
`MillProcessor`. Create only supplies execution. Throughput differences are
executor modifiers in the JSON recipes, not duplicated process definitions.

Identical output in Phase 7A is acceptable. Alcoholic does not register its
own mill.

## Consequences

Create remained optional at the process-engine layer, but world execution
of MILL had no native machine. ADR-030 adds the Malt Mill and a primitive
engine so official progression no longer depends on Create. Create
millstone and crushing recipes remain optional extra executors.
