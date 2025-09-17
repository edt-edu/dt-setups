[Zurück zur Startseite](../../README.md)<br />

# Feedback

## Conveyor

```"state": <String>,```<br>
```"reason": <String>,```<br>
```"leftSensor": <Bool>,```<br>
```"rightSensor": <Bool>,```<br>
```"position": <int>```

## Gripper

```"rotorStatus": <String>,```<br>
```"rotorReason": <String>,```<br>
```"rotorLimitSwitchRight": <Bool>,```<br>
```"rotorPosition": <int>,```<br>

```"armStatus": <String>,```<br>
```"armReason": <String>,```<br>
```"armLimitSwitchHit": <Bool>,```<br>
```"armPosition": <int>,```<br>

```"verticalStatus": <String>,```<br>
```"verticalReason": <String>,```<br>
```"verticalLimitSwitchHit": <Bool>,```<br>
```"verticalPosition": <int>,```<br>

```"clawStatus": <String>,```<br>
```"clawReason": <String>,```<br>
```"clawLimitSwitchHit": <Bool>,```<br>
```"clawPosition": <int>,```

## Vacuum Gripper

```"rotorStatus": <String>,```<br>
```"rotorReason": <String>,```<br>
```"rotorLimitSwitchRight": <Bool>,```<br>
```"rotorPosition": <int>,```<br>

```"armStatus": <String>,```<br>
```"armReason": <String>,```<br>
```"armLimitSwitchHit": <Bool>,```<br>
```"armPosition": <int>,```<br>

```"verticalStatus": <String>,```<br>
```"verticalReason": <String>,```<br>
```"verticalLimitSwitchHit": <Bool>,```<br>
```"verticalPosition": <int>,```<br>

```"compressor": <Bool>,```<br>
```"valve": <Bool>,```

## Indexed Line

```"state": <String>,```<br>
```"reason": <String>,```<br>
```"package_at_drill": <Bool>,```<br>
```"package_at_mill": <Bool>,```<br>
```"package_at_slider": <Bool>,```<br>
```"package_at_end": <Bool>,```<br>
```"package_at_start": <Bool>```<br>

## Sorting Line

Das Sorting-Line-Feedback unterscheidet sich leicht je nach State.

### "State": "ready" oder "moving to ejectors"

```"state": <String>,```<br>
```"isWhiteStored": <Bool>,```<br>
```"isRedStored": <Bool>,```<br>
```"isBlueStored": <Bool>,```<br>
```"isAtStart": <Bool>```

### "State": "sorting" oder "package waiting at ejectors" oder "critical failure while sorting"

```"state": <String>,```<br>
```"color": <String>,```<br>
```"isWhiteStored": <Bool>,```<br>
```"isRedStored": <Bool>,```<br>
```"isBlueStored": <Bool>,```<br>
```"isAtStart": <Bool>```

## Warehouse

Das Warehouse-Feedback unterscheidet sich leicht je nach State.

### "State": "ready"

```"state": "ready",```<br>
```"pickup": <Bool>,```<br>
```"conveyor": <Bool>```

### "State": "storing" oder "retrieving" oder "at_pickup"

```{```<br>
```"state": <String>,```<br>
```"row": <int>,```<br>
```"column": <int>,```<br>
```"pickup": <Bool>,```<br>
```"conveyor": <Bool>```<br>
```}```

[Zurück zur Startseite](../../README.md)<br />