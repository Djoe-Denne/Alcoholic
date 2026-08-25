# ADR-015: Passive Long-Running Process Update

- Status: Accepted
- Date: 2026-08-25

## Context

Fermenters step every tick with `deltaTicks = 1`. Barrels must not copy
that model: aging lasts far longer, and simulating unload time would let
players AFK outside the chunk and still finish a year of aging.

## Decision

No aging while the chunk is unloaded.

The barrel persists `lastProcessedGameTime`. On load it sets a skip-unload
flag so the first loaded tick records *now* and does **not** apply the
world-time gap. Catch-up applies only while the chunk stays loaded:

`delta = min(currentGameTime - lastProcessedGameTime, 24000)`

`AgingPhysics.step` is linear in `deltaTicks` so `step(N)` matches
`N × step(1)` within floating-point error.

Environment is sampled on place, every 200 ticks, and when a neighbour
changes. There is no cubic region scan.

## Consequences

- Reloading a world does not instantly finish aging.
- A loaded barrel that lags a few ticks still advances deterministically.
- Chunk-unload exploits are out of scope by design.
