# Indexed Line

The indexed line consists of 4 conveyors, a drilling machine, a milling machine and 2 conveyor sliders.

## Command

```typescript
{
    "action": "mill" | "drill" | "transfer" 
    "transfer_from_to"?: "feed_to_mill" | "mill_to_drill" | "drill_to_end"
}
```

Actions:

- `"mill"/"drill"`\
  Activates the mill/drill for two seconds if a package is available at the milling/drilling gate.
- `"transfer"`\
  Transfers the package from start to finish specified in `"transfer_from_to"`. The machine only acts if no package is at the target or for `"mill_to_drill"` and `"drill_to_end"` if a package is at the starting point.
- `query`\
  Returns the current state

## Status

```typescript
{
    "state": "ready" | "drilling" | "milling" | "transferring",
    "reason": "reset" | "cmd" | "transferred to mill" | "transferred to drill" | "transferred to end" | "milling complete" | "drilling complete",
    "package_at_mill": boolean,
    "package_at_drill": boolean,
    "package_at_start": boolean,
    "package_at_end": boolean,
    "package_at_slider": boolean,
    "transfer_from_to"?: str
}
```

`"package_at_start"`\
Sensor at the start of the feeding conveyor\
`"package_at_drill"`\
Sensor right before slider one (slider that transfers from feed to mill)\
`"package_at_mill/drill"`\
Sensor at mill/drill\
`"package_at_end"`\
Sensor after slider two (slider that transfers from drill to end)

States:

- `ready`\
  Idle state
- `drilling/milling`\
  Currently drilling/milling
- `transferring`\
  Transferring a package between two conveyors specified in `"transfer_from_to"`

Reason:

- `reset`: initially or after an error
- `cmd`: in response to a request
- *: after the task has finished