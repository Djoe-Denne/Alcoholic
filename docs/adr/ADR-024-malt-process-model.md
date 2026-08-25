# ADR-024: MALT Process Model

- Status: Accepted
- Date: 2026-08-25

## Context

Grain beverages need a steeping / germination / kilning transformation before
milling. Modelling that as a furnace smelt would collapse moisture and kiln
state into a vanilla cooking recipe and make later whisky reuse impossible.

## Decision

`alcoholic:malt` is a generic solid-to-solid process type. A definition carries:

- semantic solid input;
- solid output;
- duration;
- moisture requirement;
- temperature profile;
- kiln profile (`colorPotential`, `fermentablePotential`, `roastIntensity`).

Kiln intensity is data, not a Java subclass. Pale / amber / dark ship as
process definitions. Addons add another profile by adding JSON, not by editing
`MaltProcessor`. The artisanal malting floor executes `MALT`; it does not know
a drink family. Create is not the malt executor.

When several `MALT` definitions share the same semantic input, executors bind
to an explicit process definition id (default `alcoholic:malt_pale`) instead of
taking the first catalog match. Shift-using an empty malting floor cycles
definitions. This is generic overlapping-recipe selection, not a beverage branch.

## Consequences

Barley malt and future malted grain for spirits share one engine. Executor UX
can grow (malt house) without a second process type.
