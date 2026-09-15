# Vacuum Gripper

The Vacuum Gripper can rotate left/right, move vertical up/down and move the arm out/in.
To grip, the gripper must be in contact with an object the compressor activated and the valve closed.
To release an item, the valve must be opened.
The compressor can be active all the time.

## Sub components

See [1D Actutators](mqtt_messages/1d_actuator) for a description for the `rotor`, `arm` and `vertical` components.

## Status

```typescript
{
    "rotor-status": "rotating-right" | "rotating-left" | "stopped" | "targeting" | "resetting",
    "rotor-reason": "cmd" | "target-reached" | "position-limit",
    "rotor-target"?: int,
    "limit-switch-right": bool,
    "rotor-position": int,

    "arm-status": "moving-in" | "moving-out" | "stopped" | "targeting" | "resetting",
    "arm-reason": "cmd" | "target-reached" | "position-limit",
    "arm-target"?: int,
    "limit-switch-in": bool,
    "arm-position": int,

    "vertical-status": "moving-up" | "moving-left" | "stopped" | "targeting" | "resetting",
    "vertical-reason": "cmd" | "target-reached" | "position-limit",
    "vertical-target"?: int,
    "limit-switch-up": bool,
    "vertical-position": int,

    "compressor": bool,
    "valve": bool,
}
```

- `compressor`: Reflecting the `compressor` command
- `valve`: Reflecting the `valve` command


## Command

```typescript
{
    "rotate"?: "right" | "left" | "stop" | "to=<int>",
    "arm"?: "in" | "out" | "stop" | "to=<int>",
    "vertical"?: "up" | "down" | "stop" | "to=<int>",

    "compressor"?: bool,
    "valve"?: bool,
}
```

- `compressor`:
  - `True`: The compressor is active
  - `False`: The compressor is inactive
- `valve`:
  - `True`: The valve is closed, gripping object if in contact
  - `False`: The valve is opened, the object is released


## Setup

```JSON
{
  'type': 'vacuum_gripper',
  'ports': {
    "senseVerticalEndUp": "dio4_I_1",
    "senseArmEndIn": "dio4_I_2",
    "senseRotEndRight": "dio4_I_3",
    "encoderVertical": "dio4_Counter_5",
    "encoderArm": "dio4_Counter_7",
    "encoderRot": "dio4_Counter_9",

    "actVerticalUp": "dio4_O_1",
    "actVerticalDown": "dio4_O_2",
    "actArmIn": "dio4_O_3",
    "actArmOut": "dio4_O_4",
    "actRotRight": "dio4_O_5",
    "actRotLeft": "dio4_O_6",
    "actCompressorOn": "dio4_O_7",
    "actValve": "dio4_O_8",
  },
  'arm': {
    'limit_out': 1696,
    'accuracy': 5,
  },
  'vertical': {
      'limit_down': 1692,
      'accuracy': 5,
  },
  'rotor': {
    'limit_left': 2960,
    'accuracy': 5,
  },
}
```
