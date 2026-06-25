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
