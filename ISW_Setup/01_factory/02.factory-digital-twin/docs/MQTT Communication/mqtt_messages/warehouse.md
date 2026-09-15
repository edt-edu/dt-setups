# Warehouse

The _pick-up zone_ is where the vacuum gripper can grip or place items.

There are nine rack positions at rows 0, 1 and 2 and columns 0, 1 and 2.

## Command

```typescript
{
  "action": "retrieve" | "store",
  "row": int,
  "column": int,
}
```

Actions:
- `retrieve`  
  Move the container from rack position `(row, column)` to the _pick-up zone_.
  This can not be used if a container was detected in the _pick-up zone_ (see the `pickup` status property)
- `store`  
  Store the container currently at the _pick-up zone_ in the rack at `(row, column)`.
  The machine will perform this action, even if there is no known container at the _pick-up zone_.


## Status

```typescript
{
  "state": "ready" | "unknown_pickup" | "at_pickup" | "pickup_failed" | "retrieving" | "storing" | "failed",
  "row"?: int,
  "column"?: int,
  "pickup": bool,
  "conveyor": bool,
  "task"?: str,
}
```

States:
- `ready`  
  Reset position. No container position.
- `unknown_pickup`  
  The machine should be in the `ready` state but a container was detected in the _pick-up zone_.
  This is mainly useful to detect this condition after the machine initialisation.
- `at_pickup`  
  The container from the given rack position is at the _pick-up zone_.
- `pickup_failed`  
  Set if the outwards light barrier did not detect the container in time during a `retrieve` operation.
- `retrieving` and `storing`  
  These correspond to the commands `retrieve` and `store`. No actions can be performed in this states.
- `failed`  
  The previous task failed (for an unknown reason)
  In this case, the `task` property describes the failed task

`pickup`/`conveyor`:  
Indicate whether a container was detected at the outward/inward light barrier.
They are meant for debugging or error detection.

`task`:  
Only set if the status is `failed`.
It provides an description of the failed command.


## Setup

```JSON
{
  'type': 'warehouse',
  'ports': {
    "senseHorizontalEnd": "dio3_I_1",
    "senseLightBarrierIn": "dio3_I_2",
    "senseLightBarrierOut": "dio3_I_3",
    "senseVerticalEndUp": "dio3_I_4",
    "encoderHorizontal": "dio3_Counter_5",
    "encoderVertical": "dio3_Counter_7",
    "senseArmOut": "dio3_I_9",
    "senseArmIn": "dio3_I_10",

    "actConveyorOut": "dio3_O_1",
    "actConveyorIn": "dio3_O_2",
    "actHorizontalToRack": "dio3_O_3",
    "actHorizontalToConveyor": "dio3_O_4",
    "actVerticalDown": "dio3_O_5",
    "actVerticalUp": "dio3_O_6",
    "actArmOut": "dio3_O_7",
    "actArmIn": "dio3_O_8",
  },
  'rack': {
    'description': "Arrays in the format [horizontal][vertical]",
    'vertical_positions': [
      [66, 61, 70],
      [764, 764, 790],
      [1540, 1532, 1546]
    ],
    'horizontal_positions': [
      [1456, 2613, 3783],
      [1455, 2625, 3773],
      [1464, 2632, 3781]
    ]
  },
  'conveyor': {
    'horizontal': 15,
    'vertical_get': 1440,
    'vertical_put': 1309
  }
```
