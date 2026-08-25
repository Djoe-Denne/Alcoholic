# ADR-031: Crossroads Rotary Adapter and Electric Motor

- Status: Accepted
- Date: 2026-08-25
- Extends: [ADR-030](ADR-030-native-mechanical-executors.md)

## Context

Alcoholic machines already consume a loader-independent
`MechanicalDrivePort`. Create translates its kinetic network into that
port. Two further supplies were required without teaching machines about
foreign APIs:

- Crossroads 2.9.5 rotary power
- a generic Forge Energy electric motor (IE is one FE provider among others)

## Decision

```text
                    MechanicalDrivePort
                           ^
                           |
      +--------------------+--------------------+
      |                    |                    |
Primitive Engine       Electric Motor      external adapters
      |                    |                 /        \
 furnace fuel          Forge Energy       Create   Crossroads
```

### Electric Motor

`alcoholic:electric_motor` is native Alcoholic content. It stores FE in
an `EnergyBuffer` and implements `MechanicalDrivePort`. Platform Forge
exposes `ForgeCapabilities.ENERGY` so any FE connector — including an
Immersive Engineering Energy Connector — can charge it. The motor does
not implement `IImmersiveConnectable`; IE wires stay on IE's connector
block.

Default conversion:

- capacity 8000 FE, max input 80 FE/t
- output 32 Alcoholic speed, max load 8
- 20 FE per capacity-unit at 100% efficiency, 80% efficiency → 25 FE
  per 1.0 load tick
- idle neighbors do not call `consumeWork`, so the motor does not drain

An optional IE-shaped recipe (`electric_motor_ie`) unlocks when
`immersiveengineering:coil_lv` is present. There is one motor, not an
IE-specific duplicate.

### Crossroads

`integration-crossroads-1.19.2` attaches `Capabilities.AXLE_CAPABILITY`
to Alcoholic mechanical consumers (Malt Mill, kinetic ports). The
handler implements `IAxleHandler.propagate` and joins the axis network.
Alcoholic does **not** poll the neighboring Crossroads block.

Unit mapping (adapter only):

```text
RPM = rad/s * 60 / (2π)

1 Alcoholic capacity unit = 20 J of Crossroads rotary energy
availableCapacity         = axleEnergy / 20
work this tick            = requiredCapacity * 20 J   (addEnergy)
MoI presented to the axis = 1.25
```

Speed and direction come from the axis (`getBaseSpeed() * rotRatio`).
When a machine works, the adapter removes joules from the axle so the
network actually loses energy. A spinning axle with no remaining energy
is reported as stalled and does not satisfy `MechanicalRequirement`.

Alcoholic's core mechanical model is unchanged.

## Consequences

- Machines keep depending only on `MechanicalDrives.forMachine`.
- Removing Crossroads or IE does not prevent Alcoholic from loading.
- IE's Energy Connector is the supported IE wiring path.
