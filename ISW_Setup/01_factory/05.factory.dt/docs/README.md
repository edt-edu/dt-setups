# Project Overview

This project implements a Digital Twin System (DTS) in Kotlin. It consists of several interconnected components designed to manage digital twin models, handle events, and communicate via MQTT protocols. Below is an overview of the main components in the `src` directory and their roles.

---

## File / Folder Structure + Describtion

### `main/kotlin`

#### **`dts.modelmanager`**
- **Purpose:** Manages digital twin models and their interactions.
- **Key Files:**
    - `ModelManager.kt`: Central manager for models, providing methods to register, update, and retrieve models.
    - `models/`: Contains specific implementations of models, such as:
        - `VacuumGripperModel.kt`
        - `WarehouseModel.kt`
        - `ClawGripperModel.kt`
        - `ConveyorModel.kt`
        - `IndexedLineModel.kt`
        - `SortingLineModel.kt`
    - `AbstractModel.kt`: Base class for all models.

#### **`dts.events`**
- **Purpose:** Handles the event-driven architecture.
- **Key Files:**
    - `DTEvent.kt`: Defines the base structure for events.
    - `NewDataPointEvent.kt`: Handles data point updates.
    - `ErrorEvent.kt`: Captures and processes error-related events.
    - `observer/`: Observers for event propagation, such as:
        - `GatewayObserver.kt`
        - `ServiceObserver.kt`

#### **`dts.services`**
- **Purpose:** Provides service layer components.
- **Key Files:**
    - `EngineServiceAPI.kt`: API for managing the engine services.
    - `AbstractServiceConnection.kt`: Base class for service connections.

#### **`dts.gateway`**
- **Purpose:** Manages communication gateways.
- **Key Files:**
    - `LifecycleGateway.kt`: Handles lifecycle events for the digital twin.
    - `IslandGateway.kt`: Manages isolated communication channels.

#### **`dts.connection`**
- **Purpose:** Synchronization and connection utilities.
- **Key Files:**
    - `SynchronizationDirection.kt`: Defines synchronization directions.
    - `Synchronizer.kt`: Synchronizes data between components.

#### **`dts.mqtt`**
- **Purpose:** Manages MQTT communication.
- **Key Files:**
    - `TopicParser.kt`: Parses and validates MQTT topics.

---

### `test/kotlin`
- **Purpose:** Contains unit and integration tests.
- **Key Files:**
    - `DataSourceTester.kt`: Tests for data source functionality.
    - `mqtt/TopicParserTest.kt`: Tests MQTT topic parsing.
    - `gateway/LifecycleGatewayTest.kt`: Tests gateway lifecycle events.

---

## Testing
- Run tests located in the `test/kotlin` folder to validate functionality.

---

## Additional Documentation

The following resources provide deeper insights into the project:

- **`Model-Manager.md`**: Detailed explanation of the model management system.
- **`MQTT-Message-Format.md`**: Defines the MQTT message structure.
- **`Digital-Twin-Engine.md`**: Overview of the digital twin engine architecture.
- **`Event.md`**: Documentation on the event-driven system.

Refer to these files in the documentation folder `/docs` for more information.
