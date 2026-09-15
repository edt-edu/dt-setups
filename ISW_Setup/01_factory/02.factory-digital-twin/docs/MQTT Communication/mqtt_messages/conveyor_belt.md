# Conveyor Belt

Note: When moving to the left, the first phototransistor that notices an object would be leftSensor.

## Computed Properties
```typescript
{
    "objectPassed": bool
}
```


## Status

```typescript
{
    "state": "moving-right" | "moving-left" | "stopped" | "targeting" | "reset",
    "reason": "cmd" | "target-reached" | "position-limit",
    "target"?: int,
    "leftSensor": bool,
    "rightSensor": bool,
    "position": int,
}
```

## Control

```typescript
{
    "motor"?: "right" | "left" | "right-to=<int>" | "left-to=<int>"| "stop",
}
```


## Setup

```JSON
{
  'type': 'conveyor',
  'ports': {
    "senseLeft": "dio2_I_1",
    "senseRight": "dio2_I_2",
    "senseImpulse": "dio2_Counter_3",

    "actRight": "dio2_O_1",
    "actLeft": "dio2_O_2",
  }
}
```
