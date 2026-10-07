# PLC1 Simulation Config

This folder contains the PLC-side simulation controller and its configuration.

## Files

- `SimulatedMachines_Controller.py`: controller that instantiates simulated machines from `config.yml`.
- `config.yml`: runtime configuration (connection, MQTT, machine instances).

## Machine Instance Configuration

The controller reads `machines` from `config.yml` and supports multiple instances of the same machine type (including multiple VGRs).

Supported machine types:

- `ConveyorBelt`
- `VacuumGripper`
- `MultiProcessing`
- `SortingLine`
- `HighBay`

If `machines` is missing, the controller falls back to one default instance per type.

## Add a New VGR

Edit `config.yml` and add a new ID under the `VacuumGripper` entry.

Example:

```yaml
machines:
  - type: VacuumGripper
    ids:
      - VacuumGripper01
      - VacuumGripper02
      - VacuumGripper03  # new VGR
```

No Python code changes are needed.

## Full Example

```yaml
machines:
  - type: ConveyorBelt
    id: ConveyorBelt01
  - type: VacuumGripper
    ids:
      - VacuumGripper01
      - VacuumGripper02
  - type: MultiProcessing
    id: MultiProcessing01
  - type: SortingLine
    id: SortingLine01
  - type: HighBay
    id: HighBay01
```

## Run

From this folder:

```bash
python3 SimulatedMachines_Controller.py
```

## VacuumGripper named positions

The `vacuumGrippers` section of `config.yml` gives each VGR its named positions and safety position (encoder counts).
The SCADA missions send positions by name (`NAMEDPOSITION` parameters, e.g. `pick SL_OUTPUT_BLUE`, `place CB`), and
the controller resolves them here, so the calibration lives in one place per setup. Names and values are those of the
real PLCs of the REN_1S1V_1C1V_1H1M_02 setup (`config/RevPi01/python/RevPi_Controller.py` = VGR1,
`config/RevPi02/python/RevPi_Controller.py` = VGR2), so the missions run unchanged on both setups. Keep them in sync
when the real PLCs are recalibrated.

**`VacuumGripper01`** (RevPi01), safety position `rot: 1900, vertical: 0, horizontal: 0`

| Name | rot | vertical | horizontal | Note |
|---|---|---|---|---|
| `CB` | 1870 | 1200 | 1460 | conveyor belt feed |
| `ALT_CB` | 1360 | 1050 | 1900 | |
| `SL_INPUT` | 2900 | 1050 | 1670 | |
| `SL_OUTPUT_WHITE` | 2400 | 1400 | 500 | |
| `SL_OUTPUT_RED` | 2270 | 1400 | 970 | |
| `SL_OUTPUT_BLUE` | 2165 | 1400 | 1500 | |

**`VacuumGripper02`** (RevPi02), safety position `rot: 1900, vertical: 0, horizontal: 0`

| Name | rot | vertical | horizontal | Note |
|---|---|---|---|---|
| `HBW` | 1010 | 200 | 740 | |
| `CB` | 1155 | 1050 | 1810 | |
| `ALT_CB` | 2660 | 1050 | 1230 | conveyor belt swap |
| `MPS_INPUT` | 2050 | 850 | 1890 | |
| `MPS_OUTPUT` | 1480 | 1000 | 1720 | |

The token simulation uses the same positions for its pick-up / drop slots (`sl-white`, `sl-red`, `sl-blue`, `cb-feed`
for VGR1; `cb-swap`, `mps-input` for VGR2), so a token is picked where the gripper actually goes.

## Placing tokens on the light barriers

The simple simulators have no notion of a token, so without help a mission stays stuck (e.g. a SortingLine
`EJECT` waits forever for its middle light barrier). The controller runs a `TokenInjector`
(`rppmcontroller/simulation/TokenInjector.py`) that places and removes tokens on the light barriers over MQTT.
The PLC publishes the changes like real sensor changes, so the gateway and the digital twin see them too.

Placed tokens then move with the machines (`rppmcontroller/simulation/TokenWorld.py`); the machine commands are
unchanged, the tokens follow the actuators they switch:

- **SortingLine**: while `sortingLineActMotorConveyor` is on, a token travels from the input past the colour
  sensor (which then reports its colour, for `EJECT` with color `AUTO`) to the middle barrier and on. An active
  ejector pushes the token in front of it into its chute (`sl-white` / `sl-red` / `sl-blue`), where it stays.
  The ejectors sit at "middle barrier + the machine's ejector delay", so a time-based `EJECT` hits the token.
  A token that is not ejected falls off the end of the line.
- **ConveyorBelt**: `conveyorActForward` carries tokens from `cb-feed` to `cb-swap` (3 s apart),
  `conveyorActBackward` the other way. `MOVE_TO_SENSOR` therefore stops with the token on the target barrier and
  `MOVE_OUT` drops it off the end of the belt.
- **MultiProcessing, HighBay**: no movement yet, a token stays on its barrier until removed. A token VGR2 places
  at its `MPS_INPUT` position lands on `mps-input` and breaks the oven light barrier.

Positions are in seconds of belt travel; the layout constants are at the top of `TokenWorld.py`.

| Sensor | Machine attribute |
|---|---|
| `sl-input`, `sl-middle`, `sl-white`, `sl-blue`, `sl-red` | `SortingLine01.sortingLineSens{Input,Middle,White,Blue,Red}LightBarrier` |
| `cb-feed`, `cb-swap` | `ConveyorBelt01.conveyorSens{Feed,Swap}` |
| `mps-input` (oven feeder, where VGR2 places the token), `mps-end` | `MultiProcessing01.multiProcessingSens{Oven,EndConveyor}` |
| `hb-inside`, `hb-outside` | `HighBay01.highbaySens{Inside,Outside}` |

Further instances of a machine type get a numbered prefix (`sl2-input`, ...). Light barriers are active-low: a
token sets the attribute to `false`.

From this folder, in the `rppmcontroller` conda env:

```bash
python -m rppmcontroller.simulation.tokens_cli --config config.yml list                    # sensors and tokens
python -m rppmcontroller.simulation.tokens_cli --config config.yml place sl-input --color red
python -m rppmcontroller.simulation.tokens_cli --config config.yml pulse sl-middle 1.5     # removed after 1.5 s
python -m rppmcontroller.simulation.tokens_cli --config config.yml remove sl-input
python -m rppmcontroller.simulation.tokens_cli --config config.yml clear                   # remove every token
```

`--color` (`white`, `red`, `blue`) is the token's colour, seen by the SortingLine colour sensor when the token
passes it. `list` also shows every token and where it is (on a barrier, or between barriers on a belt).
`remove` removes the tokens on a barrier; `pulse` removes its token after the duration, wherever it is by then.

The MQTT protocol, for other tools:

| Topic | Direction | Payload |
|---|---|---|
| `SIM/<plcId>/tokens/request` | to the PLC | `{"id": "...", "action": "place\|remove\|pulse\|clear", "sensor": "sl-input", "duration": 1.0, "color": "RED"}` |
| `SIM/<plcId>/tokens/response` | from the PLC | `{"id": "...", "ok": true, "message": "..."}` |
| `SIM/<plcId>/tokens/sensors` | from the PLC, retained, on each token arrival / departure | `{"sensors": [{"alias", "machine", "attribute", "tokenPresent"}], "tokens": [{"id", "color", "track", "position", "at", "pulseEndsAt"}], "timestamp"}` |
