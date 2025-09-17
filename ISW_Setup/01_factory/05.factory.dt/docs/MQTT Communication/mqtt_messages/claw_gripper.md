The claw_gripper utilzes 2 1d_actuator and 2 impulse_actuator. Rotor and Vertical consist of 1d_actuator. Claw and Arm utilizes impulse_actuator.

## Status
```typescript
{
    "rotor-status": "rotating-right" | "rotating-left" | "stopped" | "targeting" | "resetting",
    "rotor-reason": "cmd" | "target-reached" | "position-limit",
    "rotor-target"?: int,
    "limit-switch-right": bool,
    "rotor-position": int,

    "arm-status":  "moving-out" | "stopped" | "targeting" | "resetting",
    "arm-reason": "cmd" | "target-reached" | "position-limit",
    "limit-switch-in": bool,
    "arm-position": int,

    "vertical-status": "moving-up" | "moving-left" | "stopped" | "targeting" | "resetting",
    "vertical-reason": "cmd" | "target-reached" | "position-limit",
    "vertical-target"?: int,
    "limit-switch-up": bool,
    "vertical-position": int,

    "claw-status":  "closing" | "stopped" | "targeting" | "resetting",
    "claw-reason": "cmd" | "target-reached" | "position-limit",
    "limit-switch-open": bool,
    "claw-position": int,

}
```

## Command

```typescript
{
    "rotate"?: "right" | "left" | "stop" | "to=<int>",
    "arm"?: "in" | "reset" | "stop" | "to=<int>",
    "vertical"?: "up" | "down" | "stop" | "to=<int>",
    "claw"?: "close" | "reset" | "stop" | "to=<int>",   
}
```


## Setup

```JSON
{
  'type': 'claw_gripper',
  'ports': {
    "senseClawOpen": "dio1_I_5",
    "impulseCounterClaw": "dio1_Counter_6",
    "senseArmEndIn": "dio1_I_7",
    "impulseCounterArm": "dio1_Counter_8",
    "senseVerticalEndUp": "dio1_I_9",
    "senseRotEndRight": "dio1_I_10",
    "encoderVertical": "dio1_Counter_11",
    "encoderRot": "dio1_Counter_13",

    "actClawOpen": "dio1_O_3",
    "actClawClose": "dio1_O_4",
    "actArmOut": "dio1_O_5",
    "actArmIn": "dio1_O_6",
    "actVerticalDown": "dio1_O_7",
    "actVerticalUp": "dio1_O_8",
    "actRotRight": "dio1_O_9",
    "actRotLeft": "dio1_O_10",
  },
  'vertical': {
    'limit_down': 2400,
    'accuracy': 5,
  },
  'rotor': {
    'limit_left': 4000,
    'accuracy': 5,
  },
  'claw': {
    'limit_out': 20,
  },
  'arm': {
    'limit_out': 81,
  },
}
```
