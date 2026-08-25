# ADR-008: Process Executor Capability Model

- Status: Accepted
- Date: 2026-08-25

## Context

The first machines are an artisanal press and an artisanal fermenter. Create's
Mechanical Press plus Basin must also be able to run PRESS. A later industrial
screw press should not force beverage JSON to name a machine.

## Decision

Executors advertise process type IDs such as `alcoholic:press`. They do not
advertise machine IDs, and beverage graphs do not name `artisanal_press`.

```text
Generic PRESS definition
        |
        +--> ArtisanalPress executor
        |
        +--> Create compacting adapter
        |
        +--> industrial press executor
```

`ProcessExecutor.supportedProcesses()` is the capability set. Application code
asks whether that executor can run a `ProcessInvocation` with the offered
inputs, then applies the registered process type.

Create integration translates PRESS definitions marked `create_compatible`
into `create:compacting` recipes (Mechanical Press over a Basin). It does not
reimplement agricultural transfer or fermentation kinetics. Create remaining
optional is unchanged from ADR-003.

## Consequences

- Wine JSON and cider test JSON stay machine-agnostic.
- Vinery grapes enter the same PRESS definitions through semantic tags.
- A third-party mill executor can appear later without editing beverage files.
