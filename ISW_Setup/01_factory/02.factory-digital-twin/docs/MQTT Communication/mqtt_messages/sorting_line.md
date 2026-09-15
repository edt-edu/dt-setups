# Sorting Line

There are three storage cells titled "white", "red" and "blue"

## Command

```typescript
{
    "action": "move to ejectors" | "sort" | "resolve failure" 
    "color"?: "white" | "red" | "blue"
}
```

Actions:

- `move to ejectors`\
  Moves the current package to the middle light barrier.
- `sort`\
   Sorts the package waiting at the middle light barrier in the storage container specified by `"color"`. 
- `resolve failure`\
   Manually resolves critical failures. Calling this action needs to ensure that the issue is resolved
- `query`\
   Returns the current state

## Status

```typescript
{
    "state": "ready" | "moving to ejectors" | "package waiting at ejectors" | "sorting" |
    "critical failure  while sorting",
    "color"?: str
    "isWhiteStored": boolean,
    "isRedStored": boolean,
    "isBlueStored": boolean
    "isAtStart": boolean
}
```

States:

- `ready`\
  No package available. Ready to receive package
- `moving to ejectors`\
  Currently moving package to ejector light barrier.
- `package waiting at ejectors`\
  Package waiting at ejector light barrier. Ready to sort package.
- `sorting`\
  Sorts the package waiting at the middle light barrier in a storage container.
- `critical failure while sorting` Occurs when an ejector misfires and timeouts. This state can only be exited by calling `"resolve failure"`. There is no internal machine resolve for this. Therefore calling this action needs to ensure that the issue is resolved otherwise (e.g. via vacuum_gripper).


## Setup

```JSON
{
  'type': 'sorting_line',
  'ports': {
    "impulseCounter": "dio2_Counter_4",
    "senseInputLightBarrier": "dio2_I_5",
    "senseMiddleLightBarrier": "dio2_I_6",
    "senseWhiteLightBarrier": "dio2_I_7",
    "senseRedLightBarrier": "dio2_I_8",
    "senseBlueLightBarrier": "dio2_I_9",

    "actMotorConveyor": "dio2_O_3",
    "actCompressorOn": "dio2_O_4",
    "actWhiteEjector": "dio2_O_5",
    "actRedEjector": "dio2_O_6",
    "actBlueEjector": "dio2_O_7",
  }
}
```
