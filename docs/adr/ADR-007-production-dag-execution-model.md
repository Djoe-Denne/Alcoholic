# ADR-007: Production DAG Execution Model

- Status: Accepted
- Date: 2026-08-25

## Context

Phase 3 validated beverage production graphs but did not execute them. Phase 4
needs a runtime that can run PRESS and FERMENT without treating those steps as
mandatory for every beverage. Wine is only the first gameplay example.

ADR-006 already established that a beverage is a DAG of named process nodes.
This decision specifies how that graph is executed.

## Decision

The production DAG is the sole authority over which process nodes exist.

The generic execution layer asks two questions:

1. Can executor X run process type T with the offered inputs?
2. What is the result of applying the registered process type to those inputs?

It never injects PRESS, FERMENT, DISTILL, AGE, or any other node that the
graph omitted. Unknown process types, unknown nodes, invalid edges, duplicate
node IDs, and cycles are rejected when the datapack snapshot is loaded. Cycle
errors name the beverage path and the remaining node IDs.

Process types remain registered capabilities. A datapack may describe:

- PRESS then FERMENT (wine, cider)
- FERMENT only (a rum-style wash)
- INFUSE only (a fruit liqueur)
- MASH then FERMENT then DISTILL (a whisky-style fixture)

even when only PRESS and FERMENT have gameplay executors.

Transformation belongs to `ProcessType.apply`. Machines do not decode drink
families. Recipe binding walks catalog process definitions and graph nodes of
the requested process type and matches selectors or configured liquids.

## Consequences

- Adding a beverage must not require a switch on existing drink names.
- Fixtures for beer, whisky, rum, and liqueur continue to validate without
  gameplay machines.
- Execution errors can cite beverage ID and node ID from the invocation.
