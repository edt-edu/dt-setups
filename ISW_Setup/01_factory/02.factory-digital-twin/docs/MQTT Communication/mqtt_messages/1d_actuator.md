# 1D Actuator

The 1D Actuator can move forward and backward and has a backward limit switch.
There is a position encoder increasing in the forward direction.

The command and status text should be choosen to reflect the pysical actuator properties.

## Command

Typical commands.

- `move-forward`  
  Move the actuator forward until stopped.
- `move-backward`  
  Move the actuator backward until stopped.
- `stop`  
  Stop all movements.
- `to=<int>`  
  Move to the given position.

Commands are usually disruptive.

## Status

Typical status report.

```typescript
{
    "state": "moving-forward" | "moving-backward" | "targeting" | "stopped" | "resetting",
    "reason": "cmd" | "target-reached" | "position-limit",
    "target"?: int,
    "limit-switch": bool,
    "position": int,
}
```

The reson indicates
- `cmd`  
  The state corresponds to a command or the initial reset.
- `target-reached`  
  A targeting operation succeded. State is `stopped`.
  The `target` is given.
- `position-limit`  
  The previous action was stopped because of a position limit.

Targeting:  
If the actuator is moving to a specific position `"state" == "targeting"`, the `target` field indicates the desired target position.
After the target was reached `"state" == "stopped", "reason" == "target-reached" or "position-limit"`, this field stays set until the next command.
