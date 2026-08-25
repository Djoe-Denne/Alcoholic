# ADR-028: Create MILL Executor Integration

- Status: Accepted
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

Create remains optional. Without Create, MILL still resolves in the process
engine and tests; world execution uses Create when present.
