# RWTH Setup Scripts

This folder contains launch scripts, controller entry points, and configuration files for the RWTH factory setup.

| File | Description |
| --- | --- |
| `control_software_overview.jsh` | Provides a JShell/Swing control panel for starting, stopping, monitoring, and searching output from the island controllers and FactorySCADA services. |
| `debug_controller_island1.sh` | Starts the island 1 controller on its Raspberry Pi through `debugpy` for remote Python debugging. |
| `factoryscada_rwth.yml` | Configures FactorySCADA with the RWTH MQTT endpoint, PLC hosts, ports, and the machines exposed by each island controller. |
| `island1_config.yml` | Defines the island 1 controller identity, socket ports, loop delay, and MQTT broker connection. |
| `island1_main.py` | Runs the RevPi controller for island 1 and maps its machines to RevPi I/O. |
| `island2_config.yml` | Defines the island 2 controller identity, socket ports, loop delay, and MQTT broker connection. |
| `island2_main.py` | Runs the RevPi controller for island 2 and maps its machines to RevPi I/O. |
| `island3_config.yml` | Defines the island 3 controller identity, socket ports, loop delay, and MQTT broker connection. |
| `island3_main.py` | Runs the RevPi controller for island 3 and maps its machines to RevPi I/O. |
| `missions_parallelized_rwth.yml` | Defines parallelized FactorySCADA mission graphs and raw machine commands for RWTH factory workflows. |
| `start_controller.sh` | Uploads the selected island controller and configuration to the matching Raspberry Pi and starts it, optionally installing dependencies first. |
| `start_orchestrator.sh` | Starts the FactorySCADA backend and frontend together and prefixes their combined console output. |
