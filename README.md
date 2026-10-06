# DT Setups

This repository contains **concrete experimental setups and deployment configurations** used to assemble digital-twin environments from physical systems, digital-twin components, communication infrastructure, and supporting services.

It brings together configuration files, deployment automation, models, documentation, simulation assets, and integration examples used to instantiate and reproduce different cyber-physical and digital-twin experiments.

This repository focuses on the **assembly and configuration of complete experimental environments**. The broader platform is organized across complementary repositories:

* [`cps-fischertechnik`](https://github.com/edt-edu/cps-fischertechnik) contains the Fischertechnik cyber-physical production system, including its controllers, SCADA software, hardware extensions, and related assets.
* [`dt-platform`](https://github.com/edt-edu/dt-platform) contains reusable software components and experimental implementations used to build and operate digital twins.

`dt-setups` complements these repositories by describing how physical systems, digital-twin components, infrastructure services, models, and configuration are combined into **concrete and reproducible experimental setups**.

The repository includes setups developed and operated by several project partners, as well as fully virtual configurations that can be used without access to the corresponding physical infrastructure.

See the [software and hardware allocation](docs/Software-Hardware-allocation.md) for an overview of which software runs on the RevPi PLCs, control PC, DT PC, and Arduino.

## Project structure

### `ISW_Setup/`

This directory contains setup assets developed at the **Institute for Control Engineering of Machine Tools and Manufacturing Units (ISW), University of Stuttgart**.

It includes factory documentation, digital-twin models, industrial communication components, SysML models, visualization and simulation assets, Asset Administration Shell (AAS) examples, and demonstration systems.

* `01_factory/` — Factory-related documentation, APIs, development assets, digital-twin model files, demos, and manufacturing examples.
* `02_opcuaServer/` — OPC UA mock server implementation and related configuration.
* `03_Img/` — Image and visual resources used by the setup.
* `04_SysML/` — SysML models and system architecture descriptions.
* `05_Unity/` — Unity-based visualization and simulation assets.
* `06_AAS/` — Asset Administration Shell examples and XML-based AAS definitions.
* `07_Turmdemo/` — Tower demonstration setup, Docker configuration, and backend services.
* `UpdateFischertechnikImpl/` — Implementation and documentation related to updates of the Fischertechnik setup, including Raspberry Pi instructions, Python project metadata, and test assets.

### `REN_Setups/`

This directory contains setups developed and operated at the **University of Rennes / IRISA, France**.

The setups combine physical and virtual infrastructure with configuration files, deployment automation, communication services, and digital-twin components.

* `REN_shared/` — Configuration, scripts, and infrastructure definitions shared by several Rennes setups.
* `REN_1S1V_1C1V_1H1M_01/`, `REN_1S1V_1C1V_1H1M_02/`, `REN_1V_01/` — Concrete experimental setup variants, including documentation, Ansible automation, and configuration files.
* `references/` — Mapping, architecture, and design reference material.

### `RWTH_Setup/`

This directory contains setup assets developed at **RWTH Aachen University**.

It describes a production-system configuration including controller and orchestration logic, equipment configuration, MQTT communication, and mission definitions.

* `scripts/` — Runtime scripts and factory-control software.
* `mission-models/` — Mission definitions and orchestration models.
* `mqtt/` — MQTT communication components.
* `references/` — Reference documents and setup instructions.

### `VIRTUAL_Setups/`

This directory contains fully or partially virtualized setup variants intended for experimentation, simulation, automated testing, and demonstrations without requiring access to the corresponding physical systems.

* `VIRTUAL_shared/` — Shared infrastructure components, including Kafka and Mosquitto services.
* `VIRTUAL_1C1H1M1S1V_01/`, `VIRTUAL_1C1H1M1S2V_01/` — Concrete virtual configurations and their associated documentation.

## Using the setups

This repository is a collection of **experimental environment definitions rather than a single application**.

Each setup may combine a different subset of physical systems, digital-twin components, middleware, models, and deployment infrastructure. Setup directories therefore contain the documentation and configuration required for their particular environment.

Depending on the experiment, using a setup may involve:

* deploying infrastructure and digital-twin services,
* configuring connections to physical or simulated systems,
* instantiating digital-twin models,
* running deployment or automation scripts,
* starting communication middleware such as MQTT, OPC UA, or Kafka,
* and configuring visualization, orchestration, or modeling tools.

Refer to the README and documentation contained in each setup directory for setup-specific instructions.

## License

This project is licensed under the Apache License, Version 2.0.

See the [LICENSE](LICENSE) file for details.
